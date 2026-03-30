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

package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.OptionalUtilities;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdCommandResponse;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.attestation.AttestEntityCommand;
import org.finos.waltz.model.attestation.AttestationCreateSummary;
import org.finos.waltz.model.attestation.AttestationInstance;
import org.finos.waltz.model.attestation.AttestationRun;
import org.finos.waltz.model.attestation.AttestationRunCreateCommand;
import org.finos.waltz.model.attestation.AttestationStatus;
import org.finos.waltz.model.attestation.ImmutableAttestEntityCommand;
import org.finos.waltz.model.attestation.ImmutableAttestationRunCreateCommand;
import org.finos.waltz.schema.tables.records.PermissionGroupRecord;
import org.finos.waltz.service.attestation.AttestationInstanceService;
import org.finos.waltz.service.attestation.AttestationRunService;
import org.finos.waltz.test_common.helpers.AppHelper;
import org.finos.waltz.test_common.helpers.InvolvementHelper;
import org.finos.waltz.test_common.helpers.PermissionGroupHelper;
import org.finos.waltz.test_common.helpers.PersonHelper;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.finos.waltz.common.CollectionUtilities.first;
import static org.finos.waltz.common.CollectionUtilities.isEmpty;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.finos.waltz.schema.tables.AttestationInstance.ATTESTATION_INSTANCE;
import static org.finos.waltz.schema.tables.AttestationRun.ATTESTATION_RUN;
import static org.finos.waltz.schema.tables.Involvement.INVOLVEMENT;
import static org.finos.waltz.schema.tables.PermissionGroup.PERMISSION_GROUP;
import static org.finos.waltz.schema.tables.Person.PERSON;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.finos.waltz.test_common.helpers.NameHelper.mkUserId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AttestationServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private AttestationInstanceService aiSvc;

    @Autowired
    private AttestationRunService arSvc;

    @Autowired
    private InvolvementHelper involvementHelper;

    @Autowired
    private PersonHelper personHelper;

    @Autowired
    private AppHelper appHelper;

    @Autowired
    private DSLContext dsl;

    @Autowired
    private PermissionGroupHelper permissionHelper;

    @BeforeEach
    public void cleanup() {
        dsl.deleteFrom(ATTESTATION_INSTANCE).execute();
        dsl.deleteFrom(ATTESTATION_RUN).execute();
        dsl.deleteFrom(INVOLVEMENT).execute();
        dsl.deleteFrom(PERSON).execute();
    }

    @Test
    public void basicRunCreation() {

        long invId = involvementHelper.mkInvolvementKind(mkName("basicRunCreationInvolvement"));

        AttestationRunCreateCommand cmd = ImmutableAttestationRunCreateCommand.builder()
                .dueDate(DateTimeUtilities.today().plusMonths(1))
                .targetEntityKind(EntityKind.APPLICATION)
                .attestedEntityKind(EntityKind.LOGICAL_DATA_FLOW)
                .selectionOptions(mkOpts(mkRef(EntityKind.ORG_UNIT, ouIds.a)))
                .addInvolvementKindIds(invId)
                .name("basicRunCreation Name")
                .description("basicRunCreation Desc")
                .build();

        String user = mkUserId("ast");
        IdCommandResponse response = arSvc.create(user, cmd);

        assertTrue(response.id().isPresent());

        Long runId = response.id().get();
        AttestationRun run = arSvc.getById(runId);

        assertEquals("basicRunCreation Name", run.name());
        assertEquals("basicRunCreation Desc", run.description());
        assertEquals(asSet(invId), run.involvementKindIds());
        assertEquals(AttestationStatus.ISSUED, run.status());
        assertEquals(EntityKind.LOGICAL_DATA_FLOW, run.attestedEntityKind());
        assertEquals(user, run.issuedBy());
        assertEquals(EntityKind.APPLICATION, run.targetEntityKind());
    }

    @Test
    @Disabled
    public void basicRetrieval() {

        long invId = involvementHelper.mkInvolvementKind(mkName("basicRetrieval"));

        EntityReference appRef = appHelper.createNewApp(mkName("basicRetrieval"), ouIds.a);

        AttestationRunCreateCommand cmd = ImmutableAttestationRunCreateCommand.builder()
                .dueDate(DateTimeUtilities.today().plusMonths(1))
                .targetEntityKind(EntityKind.APPLICATION)
                .attestedEntityKind(EntityKind.LOGICAL_DATA_FLOW)
                .selectionOptions(mkOpts(mkRef(EntityKind.ORG_UNIT, ouIds.a)))
                .addInvolvementKindIds(invId)
                .name(mkName("basicRetrieval"))
                .description("basicRetrieval Desc")
                .build();

        String user = mkUserId("ast");
        long pId = personHelper.createPerson(mkName("basicRetrieval"));
        involvementHelper.createInvolvement(pId, invId, appRef);
        arSvc.create(user, cmd);
        arSvc.issueInstancesForPendingRuns();

        System.out.println("-------------");
        dsl.selectFrom(ATTESTATION_INSTANCE).fetch().forEach(System.out::println);
        System.out.println("-------------");

        List<AttestationInstance> instances = aiSvc.findByIdSelector(mkOpts(mkRef(EntityKind.ORG_UNIT, ouIds.a)));
        assertFalse(isEmpty(instances));
    }

    @Test
    public void cannotAttestIfNoFlows() {
        long invId = involvementHelper.mkInvolvementKind(mkName("cannotAttestIfNoFlows"));
        String user = mkUserId("cannotAttestIfNotAssociated");
        EntityReference appRef = appHelper.createNewApp(mkName("a"), ouIds.a);

        involvementHelper.createInvolvement(
                personHelper.createPerson(user),
                invId,
                appRef);

        AttestEntityCommand cmd = ImmutableAttestEntityCommand
                .builder()
                .attestedEntityKind(EntityKind.LOGICAL_DATA_FLOW)
                .entityReference(appRef)
                .build();

        PermissionGroupRecord defaultPg = permissionHelper.createGroup("default");
        dsl.update(PERMISSION_GROUP)
                .set(PERMISSION_GROUP.IS_DEFAULT, true)
                .where(PERMISSION_GROUP.ID.eq(defaultPg.getId()))
                .execute();

        permissionHelper.setupPermissionGroupInvolvement(
                null,
                defaultPg.getId(),
                EntityKind.LOGICAL_DATA_FLOW,
                EntityKind.APPLICATION,
                Operation.ATTEST,
                null);

        assertThrows(
                IllegalArgumentException.class,
                () -> aiSvc.attestForEntity(user, cmd, false),
                "Should not be able to attest as no flows");
    }


    @Test
    public void runCreationPreview() {

        EntityReference app = appHelper.createNewApp("a", ouIds.a);
        String involvementKindName = mkName("runCreationPreview");
        String involvedUser = mkName("runCreationPreviewUser");

        long invId = involvementHelper.mkInvolvementKind(involvementKindName);
        Long pId = personHelper.createPerson(involvedUser);
        involvementHelper.createInvolvement(pId, invId, app);

        AttestationRunCreateCommand cmd = ImmutableAttestationRunCreateCommand.builder()
                .dueDate(DateTimeUtilities.today().plusMonths(1))
                .targetEntityKind(EntityKind.APPLICATION)
                .attestedEntityKind(EntityKind.LOGICAL_DATA_FLOW)
                .selectionOptions(mkOpts(mkRef(EntityKind.ORG_UNIT, ouIds.a)))
                .addInvolvementKindIds(invId)
                .name("runCreationPreview Name")
                .description("runCreationPreview Desc")
                .build();

        AttestationCreateSummary summary = arSvc.getCreateSummary(cmd);
        assertEquals(1, summary.entityCount());
        assertEquals(1, summary.instanceCount());
        assertEquals(1, summary.recipientCount());

        String runCreationUser = mkUserId("runCreationUser");

        IdCommandResponse resp = arSvc.create(
                runCreationUser,
                cmd);

        resp.id().ifPresent(runId -> {
            List<AttestationInstance> instances = aiSvc.findByRunId(runId);
            assertEquals(1, instances.size(), "expected only one instance");

            AttestationInstance instance = first(instances);
            assertEquals(app, instance.parentEntity());
            assertEquals(EntityKind.LOGICAL_DATA_FLOW, instance.attestedEntityKind());
            assertTrue(OptionalUtilities.isEmpty(instance.attestedAt()),
                    "Should not have been attested");
            assertTrue(OptionalUtilities.isEmpty(instance.attestedBy()),
                    "Should not have been attested");
            assertEquals(runId, instance.attestationRunId());

            assertTrue(instance.id().isPresent());

            String attestor = mkUserId("attestor");
            boolean attestationResult = aiSvc.attestInstance(
                    instance.id().get(),
                    attestor);

            assertTrue(attestationResult);

            AttestationInstance attestedInstance = first(aiSvc.findByRunId(runId));
            assertEquals(Optional.of(attestor), attestedInstance.attestedBy());
            assertTrue(attestedInstance.attestedAt().isPresent());

            List<AttestationInstance> instancesForApp = aiSvc.findByEntityReference(app);
            assertEquals(1, instancesForApp.size());
            AttestationInstance instanceForApp = first(instancesForApp);
            assertEquals(instanceForApp, attestedInstance);

            List<AttestationRun> runsForApp = arSvc.findByEntityReference(app);
            assertEquals(1, runsForApp.size(), "Can find runs via entity refs, e.g. for apps");
            AttestationRun runForApp = first(runsForApp);
            assertEquals(Optional.of(runId), runForApp.id());
            assertEquals(runCreationUser, runForApp.issuedBy());
            assertEquals(AttestationStatus.ISSUED, runForApp.status());

            List<AttestationRun> runForUser = arSvc.findByRecipient(involvedUser);
            assertEquals(runsForApp, runForUser);
        });
    }


}