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
import com.khartec.waltz.integration_test.inmem.helpers.PhysicalSpecHelper;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.datatype.DataTypeDecorator;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.service.data_type.DataTypeDecoratorService;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.CollectionUtilities.first;
import static com.khartec.waltz.common.ListUtilities.asList;
import static com.khartec.waltz.common.SetUtilities.asSet;
import static com.khartec.waltz.common.SetUtilities.map;
import static com.khartec.waltz.integration_test.inmem.helpers.NameHelper.mkName;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.junit.Assert.*;

public class DataTypeDecoratorServiceTest extends BaseInMemoryIntegrationTest {

    private DataTypeDecoratorService dtdSvc;
    private LogicalFlowHelper lfHelper;
    private PhysicalSpecHelper psHelper;

    @Before
    public void setupLogicalFlowDecoratorServiceTest() {
        dtdSvc = services.dataTypeDecoratorService;
        lfHelper = helpers.logicalFlowHelper;
        psHelper = helpers.physicalSpecHelper;
    }


    @Test
    public void findByFlowIds() {

        Collection<DataTypeDecorator> lfDecs = dtdSvc.findByFlowIds(emptyList(), EntityKind.LOGICAL_DATA_FLOW);
        Collection<DataTypeDecorator> psDecs = dtdSvc.findByFlowIds(emptyList(), EntityKind.PHYSICAL_SPECIFICATION);

        assertEquals("If empty id list provided returns empty list", emptyList(), lfDecs);
        assertEquals("If empty id list provided returns empty list", emptyList(), psDecs);

        Collection<DataTypeDecorator> invalidId = dtdSvc.findByFlowIds(asList(-1L), EntityKind.LOGICAL_DATA_FLOW);
        assertEquals("If flow id doesn't exist returns empty list", emptyList(), invalidId);

        assertThrows("If unsupported kind id throws exception",
                IllegalArgumentException.class,
                () ->  dtdSvc.findByFlowIds(asList(-1L), EntityKind.APPLICATION));

        EntityReference a = createNewApp("a", ouIds.a);
        EntityReference b = createNewApp("b", ouIds.a1);

        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);

        Collection<DataTypeDecorator> withNoDecorators = dtdSvc.findByFlowIds(asList(flow.entityReference().id()), EntityKind.LOGICAL_DATA_FLOW);
        assertEquals("flow has no decorators", emptyList(), withNoDecorators);

        Long dtId = createDatatype("findByFlowIds");
        String username = mkName("findByFlowIds");

        dtdSvc.updateDecorators(username, flow.entityReference(), asSet(dtId), emptySet());

        Collection<DataTypeDecorator> flowDecorators = dtdSvc.findByFlowIds(asList(flow.entityReference().id()), EntityKind.LOGICAL_DATA_FLOW);
        assertEquals("Flow with one datatype associated returns a set with one decorator", 1, flowDecorators.size());
        assertEquals("Returns the correct datatype id on the decorator", dtId, Long.valueOf(first(flowDecorators).dataTypeId()));

        Long dtId2 = createDatatype("findByFlowIds2");
        Long dtId3 = createDatatype("findByFlowIds3");
        dtdSvc.updateDecorators(username, flow.entityReference(), asSet(dtId2, dtId3), emptySet());

        Collection<DataTypeDecorator> multipleDecorators = dtdSvc.findByFlowIds(asList(flow.entityReference().id()), EntityKind.LOGICAL_DATA_FLOW);
        assertEquals("Returns all decorators for the flow", 3, multipleDecorators.size());
        assertEquals("Returns all decorators for the flow", asSet(dtId, dtId2, dtId3), map(multipleDecorators, DataTypeDecorator::dataTypeId));

        assertThrows("Find by flow ids is only supported for logical flows",
                UnsupportedOperationException.class,
                () -> dtdSvc.findByFlowIds(asSet(dtId), EntityKind.PHYSICAL_SPECIFICATION));
    }


    @Test
    public void getByEntityRefAndDataTypeId() {

        String username = mkName("getByEntityRefAndDataTypeId");

        EntityReference a = createNewApp("a", ouIds.a);
        EntityReference b = createNewApp("b", ouIds.a1);

        assertThrows("If unsupported kind id throws exception",
                IllegalArgumentException.class,
                () ->  dtdSvc.getByEntityRefAndDataTypeId(a, 1L));

        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);

        DataTypeDecorator noDt = dtdSvc.getByEntityRefAndDataTypeId(flow.entityReference(), -1L);
        assertNull("Returns null no match for dt on flow", noDt);

        Long dtId = createDatatype("getByEntityRefAndDataTypeId");

        dtdSvc.updateDecorators(username, flow.entityReference(), asSet(dtId), emptySet());
        DataTypeDecorator dataTypeDecorator = dtdSvc.getByEntityRefAndDataTypeId(flow.entityReference(), dtId);
        assertEquals("Returns datatype if exists on flow", dtId, Long.valueOf(dataTypeDecorator.dataTypeId()));

        Long psId = psHelper.createPhysicalSpec(a, "getByEntityRefAndDataTypeId");
        EntityReference specRef = mkRef(EntityKind.PHYSICAL_SPECIFICATION, psId);
        dtdSvc.updateDecorators(username, specRef, asSet(dtId), emptySet());

        DataTypeDecorator specDtd = dtdSvc.getByEntityRefAndDataTypeId(specRef, dtId);
        assertEquals("Returns datatype if exists on spec", dtId, Long.valueOf(specDtd.dataTypeId()));
    }


    @Test
    public void findByEntityId(){

        String username = mkName("getByEntityRefAndDataTypeId");

        EntityReference a = createNewApp("a", ouIds.a);
        EntityReference b = createNewApp("b", ouIds.a1);

        assertThrows("If unsupported kind id throws exception",
                IllegalArgumentException.class,
                () ->  dtdSvc.findByEntityId(a));

        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);

        List<DataTypeDecorator> flowWithNoDts = dtdSvc.findByEntityId(flow.entityReference());
        assertEquals("If flow has no data types returns empty list", emptyList(), flowWithNoDts);

        Long psId = psHelper.createPhysicalSpec(a, "getByEntityRefAndDataTypeId");
        EntityReference specRef = mkRef(EntityKind.PHYSICAL_SPECIFICATION, psId);

        List<DataTypeDecorator> specWithNoDts = dtdSvc.findByEntityId(specRef);
        assertEquals("If spec has no data types returns empty list", emptyList(), specWithNoDts);

        Long dtId = createDatatype("getByEntityRefAndDataTypeId");
        Long dtId2 = createDatatype("getByEntityRefAndDataTypeId2");
        Long dtId3 = createDatatype("getByEntityRefAndDataTypeId3");

        dtdSvc.updateDecorators(username, flow.entityReference(), asSet(dtId, dtId2, dtId3), emptySet());
        List<DataTypeDecorator> flowDecorators = dtdSvc.findByEntityId(flow.entityReference());
        assertEquals("Returns all data types on flow", asSet(dtId, dtId2, dtId3), map(flowDecorators, DataTypeDecorator::dataTypeId));

        dtdSvc.updateDecorators(username, specRef, asSet(dtId, dtId2, dtId3), emptySet());
        List<DataTypeDecorator> specDecorators = dtdSvc.findByEntityId(specRef);
        assertEquals("Returns all data types on spec", asSet(dtId, dtId2, dtId3), map(specDecorators, DataTypeDecorator::dataTypeId));
    }


}