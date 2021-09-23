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

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import com.khartec.waltz.integration_test.inmem.helpers.InvolvementHelper;
import com.khartec.waltz.integration_test.inmem.helpers.PersonHelper;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdCommandResponse;
import com.khartec.waltz.model.attestation.*;
import com.khartec.waltz.service.attestation.AttestationInstanceService;
import com.khartec.waltz.service.attestation.AttestationRunService;
import org.junit.Before;
import org.junit.Test;

import static com.khartec.waltz.common.SetUtilities.asSet;
import static com.khartec.waltz.integration_test.inmem.helpers.NameHelper.mkName;
import static com.khartec.waltz.integration_test.inmem.helpers.NameHelper.mkUserId;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AttestationServiceTest extends BaseInMemoryIntegrationTest {

    private AttestationInstanceService aiSvc;
    private AttestationRunService arSvc;
    private InvolvementHelper involvementHelper;
    private PersonHelper personHelper;

    @Before
    public void setupAttestationServiceTest() {
        aiSvc = services.attestationInstanceService;
        arSvc = services.attestationRunService;
        involvementHelper = helpers.involvementHelper;
        personHelper = helpers.personHelper;
    }


    @Test
    public void basicRunCreation() {

        long invId = involvementHelper.mkInvolvement(mkName("basicRunCreationInvolvement"));

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
    public void runCreationPreview() {

        EntityReference app = createNewApp("a", ouIds.a);

        long invId = involvementHelper.mkInvolvement(mkName("runCreationPreview"));
        Long pId = personHelper.createPerson(mkName("runCreationPreviewUser"));
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
    }


}