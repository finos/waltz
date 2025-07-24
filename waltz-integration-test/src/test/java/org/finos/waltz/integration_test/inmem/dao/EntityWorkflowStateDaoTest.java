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

import org.finos.waltz.data.entity_workflow.EntityWorkflowDefinitionDao;
import org.finos.waltz.data.entity_workflow.EntityWorkflowStateDao;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.ImmutableEntityReference;
import org.finos.waltz.model.entity_workflow.EntityWorkflowDefinition;
import org.finos.waltz.model.entity_workflow.EntityWorkflowState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EntityWorkflowStateDaoTest extends BaseInMemoryIntegrationTest {

    @Autowired
    EntityWorkflowStateDao entityWorkflowStateDao;

    @Test
    public void testSearchByName(){
        entityWorkflowStateDao.saveNewWorkflowState(1L, 2L, "testUser");
        EntityReference entityReference = ImmutableEntityReference.builder()
                .kind(EntityKind.PROPOSED_FLOW)
                .id(1L)
                .name(Optional.of("testName"))
                .description("testDescription")
                .externalId(Optional.of("externalId"))
                .build();
        EntityWorkflowState entityWorkflowState = entityWorkflowStateDao.getByEntityReferenceAndWorkflowId(2,entityReference);
        assertNotNull(entityWorkflowState);
    }

}
