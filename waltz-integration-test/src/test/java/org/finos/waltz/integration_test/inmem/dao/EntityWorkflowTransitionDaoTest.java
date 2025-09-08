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

import org.finos.waltz.data.entity_workflow.EntityWorkflowTransitionDao;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.entity_workflow.EntityWorkflowTransition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.finos.waltz.model.EntityKind.PROPOSED_FLOW;
import static org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState.PENDING_APPROVALS;
import static org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState.PROPOSED_CREATE;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EntityWorkflowTransitionDaoTest extends BaseInMemoryIntegrationTest {

    @Autowired
    EntityWorkflowTransitionDao entityWorkflowTransitionDao;

    @Test
    public void testSearchByName() {
        entityWorkflowTransitionDao.createWorkflowTransition(1L, EntityReference.mkRef(PROPOSED_FLOW, 2L), "testUser",
                PROPOSED_CREATE.name(), PENDING_APPROVALS.name(), "");
        List<EntityWorkflowTransition> list = entityWorkflowTransitionDao.findForWorkflowId(2);
        assertNotNull(list);
    }

}
