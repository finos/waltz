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

package org.finos.waltz.service.attestation;


import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.attestation.AttestationInstanceDao;
import org.finos.waltz.data.attestation.AttestationInstanceRecipientDao;
import org.finos.waltz.data.attestation.AttestationRunDao;
import org.finos.waltz.data.involvement.InvolvementDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdCommandResponse;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.ImmutableIdCommandResponse;
import org.finos.waltz.model.attestation.AttestEntityCommand;
import org.finos.waltz.model.attestation.AttestationCreateSummary;
import org.finos.waltz.model.attestation.AttestationInstance;
import org.finos.waltz.model.attestation.AttestationInstanceRecipient;
import org.finos.waltz.model.attestation.AttestationRun;
import org.finos.waltz.model.attestation.AttestationRunCreateCommand;
import org.finos.waltz.model.attestation.AttestationRunResponseSummary;
import org.finos.waltz.model.attestation.ImmutableAttestationCreateSummary;
import org.finos.waltz.model.attestation.ImmutableAttestationInstance;
import org.finos.waltz.model.attestation.ImmutableAttestationInstanceRecipient;
import org.finos.waltz.model.attestation.ImmutableAttestationRunCreateCommand;
import org.finos.waltz.model.involvement_group.ImmutableInvolvementGroup;
import org.finos.waltz.model.involvement_group.ImmutableInvolvementGroupCreateCommand;
import org.finos.waltz.model.involvement_group.InvolvementGroup;
import org.finos.waltz.model.involvement_group.InvolvementGroupCreateCommand;
import org.finos.waltz.model.person.Person;
import org.finos.waltz.service.involvement_group.InvolvementGroupService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.ListUtilities.asList;
import static org.finos.waltz.common.ListUtilities.isEmpty;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.finos.waltz.model.attestation.AttestationStatus.ISSUED;
import static org.finos.waltz.model.attestation.AttestationStatus.ISSUING;
import static org.finos.waltz.model.utils.IdUtilities.toIds;

@Service
public class AttestationRunService {

    private final AttestationInstanceDao attestationInstanceDao;
    private final AttestationInstanceRecipientDao attestationInstanceRecipientDao;
    private final AttestationRunDao attestationRunDao;
    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();
    private final InvolvementDao involvementDao;
    private final InvolvementGroupService involvementGroupService;

    @Autowired
    public AttestationRunService(AttestationInstanceDao attestationInstanceDao,
                                 AttestationInstanceRecipientDao attestationInstanceRecipientDao,
                                 AttestationRunDao attestationRunDao,
                                 InvolvementDao involvementDao, InvolvementGroupService involvementGroupService) {
        checkNotNull(attestationInstanceRecipientDao, "attestationInstanceRecipientDao cannot be null");
        checkNotNull(attestationInstanceDao, "attestationInstanceDao cannot be null");
        checkNotNull(attestationRunDao, "attestationRunDao cannot be null");
        checkNotNull(involvementDao, "involvementDao cannot be null");
        checkNotNull(involvementGroupService, "involvementGroupService cannot be null");

        this.attestationInstanceDao = attestationInstanceDao;
        this.attestationInstanceRecipientDao = attestationInstanceRecipientDao;
        this.attestationRunDao = attestationRunDao;
        this.involvementDao = involvementDao;
        this.involvementGroupService = involvementGroupService;
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
        createRecipientsGroup(runId, command.name(), command.involvementKindIds(), userId);

        // generate instances and recipients
        List<AttestationInstanceRecipient> instanceRecipients = generateAttestationInstanceRecipients(
                runId,
                command.attestedEntityKind(),
                userId);

        // store
        createAttestationInstancesAndRecipients(instanceRecipients);

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
        //Note: Changing the name of this AttestationRunCreateCommand will cause the attestation-run-list to break see #5159
        return ImmutableAttestationRunCreateCommand.builder()
                .name("Entity Attestation")
                .description("Attests that all flows are present and correct for this entity")
                .targetEntityKind(EntityKind.APPLICATION)
                .selectionOptions(mkOpts(createCommand.entityReference()))
                .involvementKindIds(asSet())
                .attestedEntityKind(createCommand.attestedEntityKind())
                .attestedEntityId(Optional.ofNullable(createCommand.attestedEntityId()))
                .issuedOn(LocalDate.now())
                .dueDate(LocalDate.now().plusMonths(6))
                .build();
    }


    public int issueInstancesForPendingRuns() {

        Set<AttestationRun> pendingRuns = attestationRunDao.findPendingRuns();

        List<AttestationInstanceRecipient> instanceRecipients = pendingRuns
                .stream()
                .flatMap(run -> generateAttestationInstanceRecipients(
                            run.id().get(),
                            run.attestedEntityKind(),
                            "admin")
                        .stream())
                .collect(toList());

        Set<Long> runsBeingIssued = toIds(pendingRuns);
        attestationRunDao.updateStatusForRunIds(runsBeingIssued, ISSUING);

        if (!isEmpty(instanceRecipients)) {
            createAttestationInstancesAndRecipients(instanceRecipients);
        }

        return attestationRunDao.updateStatusForRunIds(runsBeingIssued, ISSUED);
    }


    public void createRecipientsGroup(long runId, String runName, Set<Long> involvementKindIds, String userName) {

        InvolvementGroup group = ImmutableInvolvementGroup.builder()
                .name(format("Recipients for attestation run: %s", runName))
                .externalId(format("RECIPIENTS_ATTESTATION_RUN_%d", runId))
                .build();

        InvolvementGroupCreateCommand recipientsCmd = ImmutableInvolvementGroupCreateCommand
                .builder()
                .involvementGroup(group)
                .involvementKindIds(involvementKindIds)
                .build();

        long recipientInvGroupId = involvementGroupService.createGroup(recipientsCmd, userName);
        attestationRunDao.updateRecipientInvolvementGroupId(runId, recipientInvGroupId);
    }
}
