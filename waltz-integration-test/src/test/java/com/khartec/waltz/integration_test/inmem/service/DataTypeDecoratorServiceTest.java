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

import com.khartec.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import com.khartec.waltz.integration_test.inmem.helpers.LogicalFlowHelper;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.datatype.DataTypeDecorator;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.service.data_type.DataTypeDecoratorService;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static com.khartec.waltz.common.CollectionUtilities.first;
import static com.khartec.waltz.common.ListUtilities.asList;
import static com.khartec.waltz.common.SetUtilities.asSet;
import static com.khartec.waltz.common.SetUtilities.map;
import static com.khartec.waltz.integration_test.inmem.helpers.NameHelper.mkName;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class DataTypeDecoratorServiceTest extends BaseInMemoryIntegrationTest {

    private DataTypeDecoratorService dtSvc;
    private LogicalFlowHelper lfHelper;

    @Before
    public void setupLogicalFlowDecoratorServiceTest() {
        dtSvc = services.dataTypeDecoratorService;
        lfHelper = helpers.logicalFlowHelper;
    }


    @Test
    public void findByFlowIds() {

        Collection<DataTypeDecorator> lfDecs = dtSvc.findByFlowIds(emptyList(), EntityKind.LOGICAL_DATA_FLOW);
        Collection<DataTypeDecorator> psDecs = dtSvc.findByFlowIds(emptyList(), EntityKind.PHYSICAL_SPECIFICATION);

        assertEquals("If empty id list provided returns empty list", emptyList(), lfDecs);
        assertEquals("If empty id list provided returns empty list", emptyList(), psDecs);

        Collection<DataTypeDecorator> invalidId = dtSvc.findByFlowIds(asList(-1L), EntityKind.LOGICAL_DATA_FLOW);
        assertEquals("If flow id doesn't exist returns empty list", emptyList(), invalidId);

        assertThrows("If unsupported kind id throws exception",
                IllegalArgumentException.class,
                () ->  dtSvc.findByFlowIds(asList(-1L), EntityKind.APPLICATION));

        EntityReference a = createNewApp("a", ouIds.a);
        EntityReference b = createNewApp("b", ouIds.a1);

        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);

        Collection<DataTypeDecorator> withNoDecorators = dtSvc.findByFlowIds(asList(flow.entityReference().id()), EntityKind.LOGICAL_DATA_FLOW);
        assertEquals("flow has no decorators", emptyList(), withNoDecorators);

        Long dtId = createDatatype("findByFlowIds");
        String username = mkName("findByFlowIds");

        dtSvc.updateDecorators(username, flow.entityReference(), asSet(dtId), emptySet());

        Collection<DataTypeDecorator> flowDecorators = dtSvc.findByFlowIds(asList(flow.entityReference().id()), EntityKind.LOGICAL_DATA_FLOW);
        assertEquals("Flow with one datatype associated returns a set with one decorator", 1, flowDecorators.size());
        assertEquals("Returns the correct datatype id on the decorator", dtId, Long.valueOf(first(flowDecorators).dataTypeId()));

        Long dtId2 = createDatatype("findByFlowIds2");
        Long dtId3 = createDatatype("findByFlowIds3");
        dtSvc.updateDecorators(username, flow.entityReference(), asSet(dtId2, dtId3), emptySet());

        Collection<DataTypeDecorator> multipleDecorators = dtSvc.findByFlowIds(asList(flow.entityReference().id()), EntityKind.LOGICAL_DATA_FLOW);
        assertEquals("Returns all decorators for the flow", 3, multipleDecorators.size());
        assertEquals("Returns all decorators for the flow", asSet(dtId, dtId2, dtId3), map(multipleDecorators, DataTypeDecorator::dataTypeId));
    }


//    @Test
//    public void addDecoratorsBatch() {
//
//        String user = mkUserId("addDecoratorsBatch");
//
//        EntityReference a = createNewApp("a", ouIds.a);
//        EntityReference b = createNewApp("b", ouIds.a1);
//
//        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);
//
//        ImmutableUpdateDataFlowDecoratorsAction.builder()
//                .addedDecorators(emptyList())
//                .removedDecorators(emptyList())
//                .flowId(flow.entityReference().id())
//                .build();
//
//        lfdSvc.addDecoratorsBatch()
//
//        assertEquals(EntityKind.APPLICATION, run.targetEntityKind());
//    }


}