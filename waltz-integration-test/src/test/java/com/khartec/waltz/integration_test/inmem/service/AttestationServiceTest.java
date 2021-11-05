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

package com.khartec.waltz.integration_test.inmem.service;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.OptionalUtilities;
import org.finos.waltz.common.exception.UpdateFailedException;
import com.khartec.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import com.khartec.waltz.integration_test.inmem.helpers.AppHelper;
import com.khartec.waltz.integration_test.inmem.helpers.InvolvementHelper;
import com.khartec.waltz.integration_test.inmem.helpers.PersonHelper;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdCommandResponse;
import com.khartec.waltz.service.attestation.AttestationInstanceService;
import com.khartec.waltz.service.attestation.AttestationRunService;
import org.finos.waltz.model.attestation.*;
import org.jooq.DSLContext;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.finos.waltz.common.CollectionUtilities.first;
import static org.finos.waltz.common.SetUtilities.asSet;
import static com.khartec.waltz.integration_test.inmem.helpers.NameHelper.mkName;
import static com.khartec.waltz.integration_test.inmem.helpers.NameHelper.mkUserId;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static com.khartec.waltz.schema.tables.AttestationInstance.ATTESTATION_INSTANCE;
import static org.junit.Assert.*;

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
                .sendEmailNotifications(false)
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
    @Ignore
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
                .sendEmailNotifications(false)
                .build();

        String user = mkUserId("ast");
        long pId = personHelper.createPerson(mkName("basicRetrieval"));
        involvementHelper.createInvolvement(pId, invId, appRef);
        IdCommandResponse response = arSvc.create(user, cmd);
        arSvc.issueInstancesForPendingRuns();

        System.out.println("-------------");
        dsl.selectFrom(ATTESTATION_INSTANCE).fetch().forEach(System.out::println);
        System.out.println("-------------");

        List<AttestationInstance> instances = aiSvc.findByIdSelector(mkOpts(mkRef(EntityKind.ORG_UNIT, ouIds.a)));
        assertFalse(instances.isEmpty());
    }


    @Test
    public void cannotAttestIfNotAssociated() {
        String user = mkUserId("cannotAttestIfNotAssociated");
        EntityReference appRef = mkNewAppRef();

        AttestEntityCommand cmd = ImmutableAttestEntityCommand.builder()
                .attestedEntityKind(EntityKind.LOGICAL_DATA_FLOW)
                .entityReference(appRef)
                .build();

        assertThrows(
                "Should not be able to attest as user not associated to app",
                UpdateFailedException.class,
                () -> aiSvc.attestForEntity(user, cmd));
    }


    @Test
    public void cannotAttestIfNoFlows() {
        long invId = involvementHelper.mkInvolvementKind(mkName("cannotAttestIfNoFlows"));
        String user = mkUserId("cannotAttestIfNotAssociated");
        EntityReference appRef = mkNewAppRef();

        involvementHelper.createInvolvement(
                personHelper.createPerson(user),
                invId,
                appRef);

        AttestEntityCommand cmd = ImmutableAttestEntityCommand
                .builder()
                .attestedEntityKind(EntityKind.LOGICAL_DATA_FLOW)
                .entityReference(appRef)
                .build();

        assertThrows(
                "Should not be able to attest as no flows",
                Exception.class,
                () -> aiSvc.attestForEntity(user, cmd));
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
                .sendEmailNotifications(false)
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
            assertEquals("expected only one instance", 1, instances.size());

            AttestationInstance instance = first(instances);
            assertEquals(app, instance.parentEntity());
            assertEquals(EntityKind.LOGICAL_DATA_FLOW, instance.attestedEntityKind());
            assertTrue("Should not have been attested", OptionalUtilities.isEmpty(instance.attestedAt()));
            assertTrue("Should not have been attested", OptionalUtilities.isEmpty(instance.attestedBy()));
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
            assertEquals("Can find runs via entity refs, e.g. for apps", 1, runsForApp.size());
            AttestationRun runForApp = first(runsForApp);
            assertEquals(Optional.of(runId), runForApp.id());
            assertEquals(runCreationUser, runForApp.issuedBy());
            assertEquals(AttestationStatus.ISSUED, runForApp.status());

            List<AttestationRun> runForUser = arSvc.findByRecipient(involvedUser);
            assertEquals(runsForApp, runForUser);
        });
    }


}