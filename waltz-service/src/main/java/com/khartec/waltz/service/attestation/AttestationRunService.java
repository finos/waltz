package com.khartec.waltz.service.attestation;


import com.khartec.waltz.data.IdSelectorFactory;
import com.khartec.waltz.data.IdSelectorFactoryProvider;
import com.khartec.waltz.data.attestation.AttestationInstanceDao;
import com.khartec.waltz.data.attestation.AttestationInstanceRecipientDao;
import com.khartec.waltz.data.attestation.AttestationRunDao;
import com.khartec.waltz.data.involvement.InvolvementDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.attestation.*;
import com.khartec.waltz.model.email.ImmutableEmailNotification;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.service.email.EmailService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Stream;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
public class AttestationRunService {

    private final AttestationInstanceDao attestationInstanceDao;
    private final AttestationInstanceRecipientDao attestationInstanceRecipientDao;
    private final AttestationRunDao attestationRunDao;
    private final EmailService emailService;
    private final IdSelectorFactoryProvider idSelectorFactoryProvider;
    private final InvolvementDao involvementDao;

    @Value("${waltz.base.url}")
    private String baseUrl;

    @Autowired
    public AttestationRunService(AttestationInstanceDao attestationInstanceDao,
                                 AttestationInstanceRecipientDao attestationInstanceRecipientDao,
                                 AttestationRunDao attestationRunDao,
                                 EmailService emailService,
                                 IdSelectorFactoryProvider idSelectorFactoryProvider,
                                 InvolvementDao involvementDao) {
        checkNotNull(attestationInstanceRecipientDao, "attestationInstanceRecipientDao cannot be null");
        checkNotNull(attestationInstanceDao, "attestationInstanceDao cannot be null");
        checkNotNull(attestationRunDao, "attestationRunDao cannot be null");
        checkNotNull(emailService, "emailService cannot be null");
        checkNotNull(idSelectorFactoryProvider, "idSelectorFactoryProvider cannot be null");
        checkNotNull(involvementDao, "involvementDao cannot be null");

        this.attestationInstanceDao = attestationInstanceDao;
        this.attestationInstanceRecipientDao = attestationInstanceRecipientDao;
        this.attestationRunDao = attestationRunDao;
        this.emailService = emailService;
        this.idSelectorFactoryProvider = idSelectorFactoryProvider;
        this.involvementDao = involvementDao;
    }


    public AttestationRun getById(long attestationRunId) {
        return attestationRunDao.getById(attestationRunId);
    }


    public List<AttestationRun> findAll() {
        return attestationRunDao.findAll();
    }


    public List<AttestationRun> findByRecipient(String userId) {
        checkNotNull(userId, "userId cannot be null");

        return attestationRunDao.findByRecipient(userId);
    }


    public List<AttestationRunResponseSummary> findResponseSummaries() {
        return attestationRunDao.findResponseSummaries();
    }


    public List<AttestationRun> findByEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");

        return attestationRunDao.findByEntityReference(ref);
    }


    public AttestationCreateSummary getCreateSummary(AttestationRunCreateCommand command){

        Select<Record1<Long>> idSelector = mkIdSelector(command.targetEntityKind(), command.selectionOptions());

        Map<EntityReference, List<Person>> entityReferenceToPeople = getEntityReferenceToPeople(
                command.targetEntityKind(),
                command.selectionOptions(),
                command.involvementKindIds());

        int entityCount = attestationRunDao.getEntityCount(idSelector);

        int instanceCount = entityReferenceToPeople
                .keySet()
                .size();

        long recipientCount = entityReferenceToPeople.values()
                .stream()
                .flatMap(Collection::stream)
                .distinct()
                .count();

        return ImmutableAttestationCreateSummary.builder()
                .entityCount(entityCount)
                .instanceCount(instanceCount)
                .recipientCount(recipientCount)
                .build();

    }

    
    public IdCommandResponse create(String userId, AttestationRunCreateCommand command) {
        // create run
        Long runId = attestationRunDao.create(userId, command);

        // generate instances and recipients
        List<AttestationInstanceRecipient> instanceRecipients = generateAttestationInstanceRecipients(runId);

        // store
        createAttestationInstancesAndRecipients(instanceRecipients);

        // notify
        sendAttestationNotification(command, instanceRecipients);

        return ImmutableIdCommandResponse.builder()
                .id(runId)
                .build();
    }


    private List<AttestationInstanceRecipient> generateAttestationInstanceRecipients(long attestationRunId) {
        AttestationRun attestationRun = attestationRunDao.getById(attestationRunId);
        checkNotNull(attestationRun, "attestationRun " + attestationRunId + " not found");

        Map<EntityReference, List<Person>> entityRefToPeople = getEntityReferenceToPeople(
                attestationRun.targetEntityKind(),
                attestationRun.selectionOptions(),
                attestationRun.involvementKindIds());

        return entityRefToPeople.entrySet()
                .stream()
                .flatMap(e -> e.getValue().stream()
                                .flatMap(p -> mkInstanceRecipients(attestationRunId, e.getKey(), p.email())))
                .distinct()
                .collect(toList());
    }


    private Map<EntityReference, List<Person>> getEntityReferenceToPeople(EntityKind targetEntityKind,
                                                                          IdSelectionOptions selectionOptions,
                                                                          Set<Long> involvementKindIds) {
        Select<Record1<Long>> idSelector = mkIdSelector(targetEntityKind, selectionOptions);
        return involvementDao.findPeopleByEntitySelectorAndInvolvement(
                targetEntityKind,
                idSelector,
                involvementKindIds);
    }


    private Select<Record1<Long>> mkIdSelector(EntityKind targetEntityKind, IdSelectionOptions selectionOptions) {
        IdSelectorFactory idSelectorFactory = idSelectorFactoryProvider.getForKind(targetEntityKind);
        return idSelectorFactory.apply(selectionOptions);
    }


    private Stream<AttestationInstanceRecipient> mkInstanceRecipients(long attestationRunId, EntityReference ref, String userId) {
        switch (ref.kind()) {
            case APPLICATION:
                ArrayList<EntityKind> childKinds = newArrayList(
                        EntityKind.LOGICAL_DATA_FLOW,
                        EntityKind.PHYSICAL_FLOW
                );

                return childKinds.stream()
                        .map(ck -> ImmutableAttestationInstanceRecipient.builder()
                                .attestationInstance(ImmutableAttestationInstance.builder()
                                        .attestationRunId(attestationRunId)
                                        .parentEntity(ref)
                                        .childEntityKind(ck)
                                        .build())
                                .userId(userId)
                                .build());
            default:
                throw new IllegalArgumentException("Cannot create attestation instances for entity kind: " + ref.kind());
        }
    }


    private void createAttestationInstancesAndRecipients(List<AttestationInstanceRecipient> instanceRecipients) {
        Map<AttestationInstance, List<AttestationInstanceRecipient>> instancesAndRecipientsToSave = instanceRecipients.stream()
                .collect(groupingBy(
                        AttestationInstanceRecipient::attestationInstance,
                        toList()
                ));


        // insert new instances and recipients
        instancesAndRecipientsToSave.forEach(
                (k, v) -> {
                    // create instance
                    long instanceId = attestationInstanceDao.create(k);

                    // create recipients for the instance
                    v.stream()
                            .forEach(r -> attestationInstanceRecipientDao.create(instanceId, r.userId()));
                }
        );
    }


    private void sendAttestationNotification(AttestationRunCreateCommand attestationRunCreateCommand,
                                             List<AttestationInstanceRecipient> instanceRecipients) {
        final String MAIL_NEW_LINE = "<br/>";
        final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);

        List<String> recipientEmails = instanceRecipients.stream()
                .map(r -> r.userId())
                .distinct()
                .collect(toList());

        List<String> applications = instanceRecipients.stream()
                .map(r -> r.attestationInstance().parentEntity().name())
                .filter(r -> r.isPresent())
                .map(r -> r.get())
                .distinct()
                .collect(toList());

        String subject = "Waltz attestation: " + attestationRunCreateCommand.name();

        String attestationsUrl = baseUrl + "/attestation/instance/user";
        String body = "You are required to attest correctness of the applications listed below:"
                + MAIL_NEW_LINE + MAIL_NEW_LINE
                + "<strong>Application(s):</strong> " + String.join(", ", applications) +  MAIL_NEW_LINE + MAIL_NEW_LINE
                + "<strong>Due Date:</strong> " + attestationRunCreateCommand.dueDate().format(DATE_TIME_FORMATTER) +  MAIL_NEW_LINE +  MAIL_NEW_LINE
                + attestationRunCreateCommand.description() +  MAIL_NEW_LINE +  MAIL_NEW_LINE
                + "Please use this URL to view your pending attestations: " + attestationsUrl;

        emailService.sendEmailNotification(ImmutableEmailNotification
                .builder()
                .subject(subject)
                .body(body)
                .recipients(recipientEmails)
                .build());
    }

}
