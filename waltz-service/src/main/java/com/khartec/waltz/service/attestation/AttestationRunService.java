/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.service.attestation;


import com.khartec.waltz.data.GenericSelector;
import com.khartec.waltz.data.GenericSelectorFactory;
import com.khartec.waltz.data.attestation.AttestationInstanceDao;
import com.khartec.waltz.data.attestation.AttestationInstanceRecipientDao;
import com.khartec.waltz.data.attestation.AttestationRunDao;
import com.khartec.waltz.data.involvement.InvolvementDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.attestation.*;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.service.email.EmailService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.asList;
import static com.khartec.waltz.common.SetUtilities.asSet;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
public class AttestationRunService {

    private final AttestationInstanceDao attestationInstanceDao;
    private final AttestationInstanceRecipientDao attestationInstanceRecipientDao;
    private final AttestationRunDao attestationRunDao;
    private final EmailService emailService;
    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();
    private final InvolvementDao involvementDao;

    @Autowired
    public AttestationRunService(AttestationInstanceDao attestationInstanceDao,
                                 AttestationInstanceRecipientDao attestationInstanceRecipientDao,
                                 AttestationRunDao attestationRunDao,
                                 EmailService emailService,
                                 InvolvementDao involvementDao) {
        checkNotNull(attestationInstanceRecipientDao, "attestationInstanceRecipientDao cannot be null");
        checkNotNull(attestationInstanceDao, "attestationInstanceDao cannot be null");
        checkNotNull(attestationRunDao, "attestationRunDao cannot be null");
        checkNotNull(emailService, "emailService cannot be null");
        checkNotNull(involvementDao, "involvementDao cannot be null");

        this.attestationInstanceDao = attestationInstanceDao;
        this.attestationInstanceRecipientDao = attestationInstanceRecipientDao;
        this.attestationRunDao = attestationRunDao;
        this.emailService = emailService;
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
        List<AttestationInstanceRecipient> instanceRecipients = generateAttestationInstanceRecipients(
                runId,
                command.attestedEntityKind(),
                userId);

        // store
        createAttestationInstancesAndRecipients(instanceRecipients);

        emailService.sendEmailNotification(mkRef(EntityKind.ATTESTATION_RUN, runId));

        return ImmutableIdCommandResponse.builder()
                .id(runId)
                .build();
    }


    private List<AttestationInstanceRecipient> generateAttestationInstanceRecipients(long attestationRunId,
                                                                                     EntityKind attestedEntityKind,
                                                                                     String userId) {
        AttestationRun attestationRun = attestationRunDao.getById(attestationRunId);
        checkNotNull(attestationRun, "attestationRun " + attestationRunId + " not found");

        if(attestationRun.involvementKindIds().isEmpty()) {

            return asList(mkInstanceRecipient(attestationRunId, attestationRun.selectionOptions().entityReference(), userId, attestedEntityKind));

        } else {

            Map<EntityReference, List<Person>> entityRefToPeople = getEntityReferenceToPeople(
                    attestationRun.targetEntityKind(),
                    attestationRun.selectionOptions(),
                    attestationRun.involvementKindIds());

            return entityRefToPeople.entrySet()
                    .stream()
                    .flatMap(e -> e.getValue().stream()
                            .map(p -> mkInstanceRecipient(attestationRunId, e.getKey(), p.email(), attestedEntityKind)))
                    .distinct()
                    .collect(toList());
        }
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
        GenericSelector genericSelector = genericSelectorFactory.applyForKind(targetEntityKind, selectionOptions);
        return genericSelector.selector();
    }


    private AttestationInstanceRecipient mkInstanceRecipient(long attestationRunId,
                                                             EntityReference ref,
                                                             String userId,
                                                             EntityKind attestedKind) {
        switch (ref.kind()) {
            case APPLICATION:
                return ImmutableAttestationInstanceRecipient.builder()
                                .attestationInstance(ImmutableAttestationInstance.builder()
                                        .attestationRunId(attestationRunId)
                                        .parentEntity(ref)
                                        .attestedEntityKind(attestedKind)
                                        .build())
                                .userId(userId)
                                .build();
            default:
                throw new IllegalArgumentException("Cannot create attestation instances for entity kind: " + ref.kind());
        }
    }


    private void createAttestationInstancesAndRecipients(List<AttestationInstanceRecipient> instanceRecipients) {
        Map<AttestationInstance, List<AttestationInstanceRecipient>> instancesAndRecipientsToSave = instanceRecipients
                .stream()
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
                    v.forEach(r -> attestationInstanceRecipientDao.create(instanceId, r.userId()));
                }
        );
    }


    public IdCommandResponse createRunForEntity(String username, AttestEntityCommand entityRunCreateCommand) {
        return create(username, mkCreateCommand(entityRunCreateCommand));
    }


    public Collection<AttestationRun> findByIdSelector(IdSelectionOptions options) {
        Select<Record1<Long>> selector = mkIdSelector(EntityKind.ATTESTATION, options);
        return attestationRunDao.findByIdSelector(selector);
    }

    private ImmutableAttestationRunCreateCommand mkCreateCommand(AttestEntityCommand createCommand) {
        return ImmutableAttestationRunCreateCommand.builder()
                .name("Entity Attestation")
                .description("Attests that all flows are present and correct for this entity")
                .targetEntityKind(EntityKind.APPLICATION)
                .selectionOptions(mkOpts(createCommand.entityReference()))
                .involvementKindIds(asSet())
                .attestedEntityKind(createCommand.attestedEntityKind())
                .attestedEntityId(empty())
                .issuedOn(LocalDate.now())
                .dueDate(LocalDate.now().plusMonths(6))
                .build();
    }
}
