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
import com.khartec.waltz.integration_test.inmem.helpers.*;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.datatype.DataType;
import org.finos.waltz.model.datatype.DataTypeDecorator;
import org.finos.waltz.model.datatype.DataTypeUsageCharacteristics;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.service.data_type.DataTypeDecoratorService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;

import static org.finos.waltz.common.CollectionUtilities.first;
import static org.finos.waltz.common.ListUtilities.asList;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.SetUtilities.map;
import static com.khartec.waltz.integration_test.inmem.helpers.NameHelper.mkName;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.junit.Assert.*;

public class DataTypeDecoratorServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private DataTypeDecoratorService dtdSvc;

    @Autowired
    private LogicalFlowHelper lfHelper;

    @Autowired
    private PhysicalSpecHelper psHelper;

    @Autowired
    private PhysicalFlowHelper pfHelper;

    @Autowired
    private AppHelper appHelper;

    @Autowired
    private DataTypeHelper dataTypeHelper;


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

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);

        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);

        Collection<DataTypeDecorator> withNoDecorators = dtdSvc.findByFlowIds(asList(flow.entityReference().id()), EntityKind.LOGICAL_DATA_FLOW);
        assertEquals("flow has no decorators", emptyList(), withNoDecorators);

        Long dtId = dataTypeHelper.createDataType("findByFlowIds");
        String username = mkName("findByFlowIds");

        dtdSvc.updateDecorators(username, flow.entityReference(), asSet(dtId), emptySet());

        Collection<DataTypeDecorator> flowDecorators = dtdSvc.findByFlowIds(asList(flow.entityReference().id()), EntityKind.LOGICAL_DATA_FLOW);
        assertEquals("Flow with one datatype associated returns a set with one decorator", 1, flowDecorators.size());
        assertEquals("Returns the correct datatype id on the decorator", dtId, Long.valueOf(first(flowDecorators).dataTypeId()));

        Long dtId2 = dataTypeHelper.createDataType("findByFlowIds2");
        Long dtId3 = dataTypeHelper.createDataType("findByFlowIds3");
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

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);

        assertThrows("If unsupported kind id throws exception",
                IllegalArgumentException.class,
                () ->  dtdSvc.getByEntityRefAndDataTypeId(a, 1L));

        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);

        DataTypeDecorator noDt = dtdSvc.getByEntityRefAndDataTypeId(flow.entityReference(), -1L);
        assertNull("Returns null no match for dt on flow", noDt);

        Long dtId = dataTypeHelper.createDataType("getByEntityRefAndDataTypeId");

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

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);

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

        Long dtId = dataTypeHelper.createDataType("getByEntityRefAndDataTypeId");
        Long dtId2 = dataTypeHelper.createDataType("getByEntityRefAndDataTypeId2");
        Long dtId3 = dataTypeHelper.createDataType("getByEntityRefAndDataTypeId3");

        dtdSvc.updateDecorators(username, flow.entityReference(), asSet(dtId, dtId2, dtId3), emptySet());
        List<DataTypeDecorator> flowDecorators = dtdSvc.findByEntityId(flow.entityReference());
        assertEquals("Returns all data types on flow", asSet(dtId, dtId2, dtId3), map(flowDecorators, DataTypeDecorator::dataTypeId));

        dtdSvc.updateDecorators(username, specRef, asSet(dtId, dtId2, dtId3), emptySet());
        List<DataTypeDecorator> specDecorators = dtdSvc.findByEntityId(specRef);
        assertEquals("Returns all data types on spec", asSet(dtId, dtId2, dtId3), map(specDecorators, DataTypeDecorator::dataTypeId));
    }


    @Test
    public void findByEntityIdSelector(){

        String username = mkName("findByEntityIdSelector");

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);

        IdSelectionOptions appOpts = mkOpts(a);

        assertThrows("If not logical flow kind or physical spec kind should throw exception",
                IllegalArgumentException.class,
                () -> dtdSvc.findByEntityIdSelector(EntityKind.APPLICATION, appOpts));

        List<DataTypeDecorator> selectorForLfWhereNoDecorators = dtdSvc.findByEntityIdSelector(EntityKind.LOGICAL_DATA_FLOW, appOpts);
        assertEquals("If no flows and decorators for selector returns empty list", emptyList(), selectorForLfWhereNoDecorators);

        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);
        Long dtId = dataTypeHelper.createDataType("findByEntityIdSelector");
        Long dtId2 = dataTypeHelper.createDataType("findByEntityIdSelector2");
        dtdSvc.updateDecorators(username, flow.entityReference(), asSet(dtId, dtId2), emptySet());

        List<DataTypeDecorator> selectorWithDecorators = dtdSvc.findByEntityIdSelector(EntityKind.LOGICAL_DATA_FLOW, appOpts);
        assertEquals("Returns all data types for flow selector", asSet(dtId, dtId2), map(selectorWithDecorators, DataTypeDecorator::dataTypeId));

        IdSelectionOptions flowOpts = mkOpts(flow.entityReference());
        List<DataTypeDecorator> selectorForPsWhereNoDecorators = dtdSvc.findByEntityIdSelector(EntityKind.PHYSICAL_SPECIFICATION, flowOpts);
        assertEquals("If no flows and decorators for selector returns empty list", emptyList(), selectorForPsWhereNoDecorators);

        Long psId = psHelper.createPhysicalSpec(a, "findByEntityIdSelector");
        EntityReference specRef = mkRef(EntityKind.PHYSICAL_SPECIFICATION, psId);

        pfHelper.createPhysicalFlow(flow.entityReference().id(), psId, "findByEntityIdSelector");
        dtdSvc.updateDecorators(username, specRef, asSet(dtId, dtId2), emptySet());
        List<DataTypeDecorator> selectorForPs = dtdSvc.findByEntityIdSelector(EntityKind.PHYSICAL_SPECIFICATION, flowOpts);
        assertEquals("Returns all data types for spec selector", asSet(dtId, dtId2), map(selectorForPs, DataTypeDecorator::dataTypeId));
    }


    @Test
    public void updateDecorators(){

        String username = mkName("updateDecorators");

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);
        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);

        assertThrows("Throws exception if no username provided",
                IllegalArgumentException.class,
                () -> dtdSvc.updateDecorators(null, flow.entityReference(), emptySet(), emptySet()));

        assertThrows("Throws exception if no ref provided",
                IllegalArgumentException.class,
                () -> dtdSvc.updateDecorators(username, null, emptySet(), emptySet()));

        assertThrows("Throws exception if no unsupported ref provided",
                UnsupportedOperationException.class,
                () -> dtdSvc.updateDecorators(username, mkRef(EntityKind.APPLICATION, -1L), emptySet(), emptySet()));

        Long dtId = dataTypeHelper.createDataType("updateDecorators");
        Long dtId2 = dataTypeHelper.createDataType("updateDecorators2");
        Long dtId3 = dataTypeHelper.createDataType("updateDecorators3");
        dtdSvc.updateDecorators(username, flow.entityReference(), asSet(dtId, dtId2), emptySet());

        Collection<DataTypeDecorator> flowDecorators = dtdSvc.findByFlowIds(asSet(flow.entityReference().id()), EntityKind.LOGICAL_DATA_FLOW);
        assertEquals("Adds data types that do not exist on flow", asSet(dtId, dtId2), map(flowDecorators, DataTypeDecorator::dataTypeId));

        dtdSvc.updateDecorators(username, flow.entityReference(), emptySet(), asSet(dtId3));
        Collection<DataTypeDecorator> removeDtNotAssociated = dtdSvc.findByFlowIds(asSet(flow.entityReference().id()), EntityKind.LOGICAL_DATA_FLOW);
        assertEquals("Removing dt not associated does not change set of decorators", asSet(dtId, dtId2), map(removeDtNotAssociated, DataTypeDecorator::dataTypeId));
        
        dtdSvc.updateDecorators(username, flow.entityReference(), emptySet(), asSet(dtId2));
        Collection<DataTypeDecorator> removedDatatype = dtdSvc.findByFlowIds(asSet(flow.entityReference().id()), EntityKind.LOGICAL_DATA_FLOW);
        assertEquals("Removed associated datatype", asSet(dtId), map(removedDatatype, DataTypeDecorator::dataTypeId));
    }


    @Test
    public void findSuggestedByEntityRef() {
        String username = mkName("updateDecorators");
        EntityReference a = appHelper.createNewApp("a", ouIds.a);

        assertThrows(
                "Throw exception if not a logical data flow or physical spec",
                UnsupportedOperationException.class,
                () -> dtdSvc.findSuggestedByEntityRef(a));

        EntityReference b = appHelper.createNewApp("b", ouIds.a1);
        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);

        Collection<DataType> suggestedWhenNoFlows = dtdSvc.findSuggestedByEntityRef(flow.entityReference());
        assertEquals("If no flows associated to entity should return empty list", emptyList(), suggestedWhenNoFlows);

        EntityReference c = appHelper.createNewApp("b", ouIds.a1);
        LogicalFlow flow2 = lfHelper.createLogicalFlow(b, c);

        Long dtId = dataTypeHelper.createDataType("updateDecorators");
        Long dtId2 = dataTypeHelper.createDataType("updateDecorators2");
        dtdSvc.updateDecorators(username, flow.entityReference(), asSet(dtId), emptySet());
        dtdSvc.updateDecorators(username, flow2.entityReference(), asSet(dtId, dtId2), emptySet());

        Collection<DataType> suggestedWhenUpstream = dtdSvc.findSuggestedByEntityRef(flow.entityReference());
        assertEquals("Should return suggested data types based on the upstream app", asSet(dtId), map(suggestedWhenUpstream, d -> d.id().get()));

        Collection<DataType> suggestedWhenSrcHasUpstreamAndDownStream = dtdSvc.findSuggestedByEntityRef(flow2.entityReference());
        assertEquals("Should return suggested data types based on up and down stream flows on the upstream app",
                asSet(dtId, dtId2),
                map(suggestedWhenSrcHasUpstreamAndDownStream, d -> d.id().get()));

        Long specId = psHelper.createPhysicalSpec(a, "updateDecorators");
        pfHelper.createPhysicalFlow(flow.entityReference().id(), specId, "updateDecorators");

        Collection<DataType> suggestedForPsWhenUpstream = dtdSvc.findSuggestedByEntityRef(mkRef(EntityKind.PHYSICAL_SPECIFICATION, specId));
        assertEquals("Should return suggested data types based on the upstream app", asSet(dtId), map(suggestedForPsWhenUpstream, d -> d.id().get()));

        Long specId2 = psHelper.createPhysicalSpec(a, "updateDecorators");
        Collection<DataType> specNotInvolvedInFlows = dtdSvc.findSuggestedByEntityRef(mkRef(EntityKind.PHYSICAL_SPECIFICATION, specId2));

        assertEquals("Spec not involved in flows should return empty list", emptyList(), specNotInvolvedInFlows);
    }


    @Test
    public void findDatatypeUsageCharacteristics() {
        String username = mkName("findDatatypeUsageCharacteristics");
        EntityReference a = appHelper.createNewApp("a", ouIds.a);

        assertThrows(
                "Throw exception for entities other than physical specs and logical flows",
                IllegalArgumentException.class,
                () -> dtdSvc.findDatatypeUsageCharacteristics(a));

        EntityReference b = appHelper.createNewApp("b", ouIds.a1);
        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);

        Collection<DataTypeUsageCharacteristics> noDecorators = dtdSvc.findDatatypeUsageCharacteristics(flow.entityReference());
        assertEquals("If there are no decorators on a flow the list of usage characteristics should be empty", emptyList(), noDecorators);

        Long dtId = dataTypeHelper.createDataType("findDatatypeUsageCharacteristics");
        Long dtId2 = dataTypeHelper.createDataType("findDatatypeUsageCharacteristics");
        dtdSvc.updateDecorators(username, flow.entityReference(), asSet(dtId, dtId2), emptySet());

        Collection<DataTypeUsageCharacteristics> decoratorsOnFlow = dtdSvc.findDatatypeUsageCharacteristics(flow.entityReference());

        assertEquals("Returns usage characteristics for each data type associated to a flow", asSet(dtId, dtId2), map(decoratorsOnFlow, DataTypeUsageCharacteristics::dataTypeId));
        assertEquals("Characteristics suggest all flows are removable when there are no underlying physical flows", asSet(true), map(decoratorsOnFlow, DataTypeUsageCharacteristics::isRemovable));

        Long specId = psHelper.createPhysicalSpec(a, "findDatatypeUsageCharacteristics");
        pfHelper.createPhysicalFlow(flow.entityReference().id(), specId, "findDatatypeUsageCharacteristics");
        dtdSvc.updateDecorators(username, mkRef(EntityKind.PHYSICAL_SPECIFICATION, specId), asSet(dtId), emptySet());

        Collection<DataTypeUsageCharacteristics> decoratorsOnFlowWithUnderlyingSpec = dtdSvc.findDatatypeUsageCharacteristics(flow.entityReference());
        DataTypeUsageCharacteristics dtDecorator = decoratorsOnFlowWithUnderlyingSpec.stream().filter(d -> d.dataTypeId() == dtId).findFirst().get();
        DataTypeUsageCharacteristics dt2Decorator = decoratorsOnFlowWithUnderlyingSpec.stream().filter(d -> d.dataTypeId() == dtId2).findFirst().get();

        assertFalse("When underlying physical with datatype should not be removable", dtDecorator.isRemovable());
        assertTrue("When no underlying physical datatype should remain removable", dt2Decorator.isRemovable());

        Collection<DataTypeUsageCharacteristics> usageCharacteristicsForSpec = dtdSvc.findDatatypeUsageCharacteristics(mkRef(EntityKind.PHYSICAL_SPECIFICATION, specId));
        assertEquals("Returns usage characteristics for each data type associated to a spec", asSet(dtId), map(usageCharacteristicsForSpec, DataTypeUsageCharacteristics::dataTypeId));
        assertTrue("Specs should always be flagged as removable", first(usageCharacteristicsForSpec).isRemovable());
    }
}
