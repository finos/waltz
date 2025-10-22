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

package org.finos.waltz.integration_test.inmem.dao;

import org.finos.waltz.data.entity_workflow.EntityWorkflowStateDao;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.entity_workflow.EntityWorkflowState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState.FULLY_APPROVED;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EntityWorkflowStateDaoTest extends BaseInMemoryIntegrationTest {

    @Autowired
    EntityWorkflowStateDao entityWorkflowStateDao;

    @Test
    public void testSearchByName() {
        EntityKind entityKind = EntityKind.PROPOSED_FLOW;
        String description = "testDescription";
        EntityReference ref = EntityReference.mkRef(entityKind, 2L);
        entityWorkflowStateDao.createWorkflowState(1L, ref, "testUser",
                FULLY_APPROVED.name(), description);
        EntityWorkflowState entityWorkflowState = entityWorkflowStateDao.getByEntityReferenceAndWorkflowId(1L, ref);
        assertNotNull(entityWorkflowState);
    }

}
