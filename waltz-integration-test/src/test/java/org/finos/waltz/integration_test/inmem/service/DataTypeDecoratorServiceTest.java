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

import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.integration_test.inmem.helpers.AppHelper;
import org.finos.waltz.integration_test.inmem.helpers.DataTypeHelper;
import org.finos.waltz.integration_test.inmem.helpers.LogicalFlowHelper;
import org.finos.waltz.integration_test.inmem.helpers.PhysicalFlowHelper;
import org.finos.waltz.integration_test.inmem.helpers.PhysicalSpecHelper;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.datatype.DataType;
import org.finos.waltz.model.datatype.DataTypeDecorator;
import org.finos.waltz.model.datatype.DataTypeUsageCharacteristics;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.service.data_type.DataTypeDecoratorService;
import org.finos.waltz.service.data_type.DataTypeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.finos.waltz.common.CollectionUtilities.first;
import static org.finos.waltz.common.ListUtilities.asList;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.integration_test.inmem.helpers.NameHelper.mkName;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataTypeDecoratorServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private DataTypeDecoratorService dtdSvc;

    @Autowired
    private DataTypeService dtSvc;

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

        assertEquals(emptyList(), lfDecs, "If empty id list provided returns empty list");
        assertEquals(emptyList(), psDecs, "If empty id list provided returns empty list");

        Collection<DataTypeDecorator> invalidId = dtdSvc.findByFlowIds(asList(-1L), EntityKind.LOGICAL_DATA_FLOW);
        assertEquals(emptyList(), invalidId, "If flow id doesn't exist returns empty list");

        assertThrows(IllegalArgumentException.class,
                () -> dtdSvc.findByFlowIds(asList(-1L), EntityKind.APPLICATION),
                "If unsupported kind id throws exception");

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);

        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);

        Collection<DataTypeDecorator> withNoDecorators = dtdSvc.findByFlowIds(asList(flow.entityReference().id()), EntityKind.LOGICAL_DATA_FLOW);
        assertEquals(emptyList(), withNoDecorators,
                "flow has no decorators");

        Long dtId = dataTypeHelper.createDataType("findByFlowIds");
        String username = mkName("findByFlowIds");

        dtdSvc.updateDecorators(username, flow.entityReference(), asSet(dtId), emptySet());

        Collection<DataTypeDecorator> flowDecorators = dtdSvc.findByFlowIds(asList(flow.entityReference().id()), EntityKind.LOGICAL_DATA_FLOW);
        assertEquals(1, flowDecorators.size(), "Flow with one datatype associated returns a set with one decorator");
        assertEquals(dtId, Long.valueOf(first(flowDecorators).dataTypeId()),
                "Returns the correct datatype id on the decorator");

        Long dtId2 = dataTypeHelper.createDataType("findByFlowIds2");
        Long dtId3 = dataTypeHelper.createDataType("findByFlowIds3");
        dtdSvc.updateDecorators(username, flow.entityReference(), asSet(dtId2, dtId3), emptySet());

        Collection<DataTypeDecorator> multipleDecorators = dtdSvc.findByFlowIds(asList(flow.entityReference().id()), EntityKind.LOGICAL_DATA_FLOW);
        assertEquals(3, multipleDecorators.size());
        assertEquals(asSet(dtId, dtId2, dtId3), map(multipleDecorators, DataTypeDecorator::dataTypeId),
                "Returns all decorators for the flow");

        assertThrows(
                UnsupportedOperationException.class,
                () -> dtdSvc.findByFlowIds(asSet(dtId), EntityKind.PHYSICAL_SPECIFICATION),
                "Find by flow ids is only supported for logical flows");
    }


    @Test
    public void getByEntityRefAndDataTypeId() {

        String username = mkName("getByEntityRefAndDataTypeId");

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);

        assertThrows(
                IllegalArgumentException.class,
                () -> dtdSvc.getByEntityRefAndDataTypeId(a, 1L),
                "If unsupported kind id throws exception");

        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);

        DataTypeDecorator noDt = dtdSvc.getByEntityRefAndDataTypeId(flow.entityReference(), -1L);
        assertNull(noDt, "Returns null no match for dt on flow");

        Long dtId = dataTypeHelper.createDataType("getByEntityRefAndDataTypeId");

        dtdSvc.updateDecorators(username, flow.entityReference(), asSet(dtId), emptySet());
        DataTypeDecorator dataTypeDecorator = dtdSvc.getByEntityRefAndDataTypeId(flow.entityReference(), dtId);
        assertEquals(dtId, Long.valueOf(dataTypeDecorator.dataTypeId()),
                "Returns datatype if exists on flow");

        Long psId = psHelper.createPhysicalSpec(a, "getByEntityRefAndDataTypeId");
        EntityReference specRef = mkRef(EntityKind.PHYSICAL_SPECIFICATION, psId);
        dtdSvc.updateDecorators(username, specRef, asSet(dtId), emptySet());

        DataTypeDecorator specDtd = dtdSvc.getByEntityRefAndDataTypeId(specRef, dtId);
        assertEquals(dtId, Long.valueOf(specDtd.dataTypeId()),
                "Returns datatype if exists on spec");
    }


    @Test
    public void findByEntityId() {

        String username = mkName("getByEntityRefAndDataTypeId");

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);

        assertThrows(IllegalArgumentException.class,
                () -> dtdSvc.findByEntityId(a),
                "If unsupported kind id throws exception");

        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);

        List<DataTypeDecorator> flowWithNoDts = dtdSvc.findByEntityId(flow.entityReference());
        assertEquals(emptyList(), flowWithNoDts, "If flow has no data types returns empty list");

        Long psId = psHelper.createPhysicalSpec(a, "getByEntityRefAndDataTypeId");
        EntityReference specRef = mkRef(EntityKind.PHYSICAL_SPECIFICATION, psId);

        List<DataTypeDecorator> specWithNoDts = dtdSvc.findByEntityId(specRef);
        assertEquals(emptyList(), specWithNoDts, "If spec has no data types returns empty list");

        Long dtId = dataTypeHelper.createDataType("getByEntityRefAndDataTypeId");
        Long dtId2 = dataTypeHelper.createDataType("getByEntityRefAndDataTypeId2");
        Long dtId3 = dataTypeHelper.createDataType("getByEntityRefAndDataTypeId3");

        dtdSvc.updateDecorators(username, flow.entityReference(), asSet(dtId, dtId2, dtId3), emptySet());
        List<DataTypeDecorator> flowDecorators = dtdSvc.findByEntityId(flow.entityReference());
        assertEquals(asSet(dtId, dtId2, dtId3), map(flowDecorators, DataTypeDecorator::dataTypeId), "Returns all data types on flow");

        dtdSvc.updateDecorators(username, specRef, asSet(dtId, dtId2, dtId3), emptySet());
        List<DataTypeDecorator> specDecorators = dtdSvc.findByEntityId(specRef);
        assertEquals(asSet(dtId, dtId2, dtId3), map(specDecorators, DataTypeDecorator::dataTypeId), "Returns all data types on spec");
    }


    @Test
    public void findByEntityIdSelector() {

        String username = mkName("findByEntityIdSelector");

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);

        IdSelectionOptions appOpts = mkOpts(a);

        assertThrows(IllegalArgumentException.class,
                () -> dtdSvc.findByEntityIdSelector(EntityKind.APPLICATION, appOpts),
                "If not logical flow kind or physical spec kind should throw exception");

        List<DataTypeDecorator> selectorForLfWhereNoDecorators = dtdSvc.findByEntityIdSelector(EntityKind.LOGICAL_DATA_FLOW, appOpts);
        assertEquals(emptyList(), selectorForLfWhereNoDecorators, "If no flows and decorators for selector returns empty list");

        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);
        Long dtId = dataTypeHelper.createDataType("findByEntityIdSelector");
        Long dtId2 = dataTypeHelper.createDataType("findByEntityIdSelector2");
        dtdSvc.updateDecorators(username, flow.entityReference(), asSet(dtId, dtId2), emptySet());

        List<DataTypeDecorator> selectorWithDecorators = dtdSvc.findByEntityIdSelector(EntityKind.LOGICAL_DATA_FLOW, appOpts);
        assertEquals(asSet(dtId, dtId2), map(selectorWithDecorators, DataTypeDecorator::dataTypeId), "Returns all data types for flow selector");

        IdSelectionOptions flowOpts = mkOpts(flow.entityReference());
        List<DataTypeDecorator> selectorForPsWhereNoDecorators = dtdSvc.findByEntityIdSelector(EntityKind.PHYSICAL_SPECIFICATION, flowOpts);
        assertEquals(emptyList(), selectorForPsWhereNoDecorators, "If no flows and decorators for selector returns empty list");

        Long psId = psHelper.createPhysicalSpec(a, "findByEntityIdSelector");
        EntityReference specRef = mkRef(EntityKind.PHYSICAL_SPECIFICATION, psId);

        pfHelper.createPhysicalFlow(flow.entityReference().id(), psId, "findByEntityIdSelector");
        dtdSvc.updateDecorators(username, specRef, asSet(dtId, dtId2), emptySet());
        List<DataTypeDecorator> selectorForPs = dtdSvc.findByEntityIdSelector(EntityKind.PHYSICAL_SPECIFICATION, flowOpts);
        assertEquals(asSet(dtId, dtId2), map(selectorForPs, DataTypeDecorator::dataTypeId), "Returns all data types for spec selector");
    }


    @Test
    public void updateDecorators() {

        String username = mkName("updateDecorators");

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);
        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);

        assertThrows(IllegalArgumentException.class,
                () -> dtdSvc.updateDecorators(null, flow.entityReference(), emptySet(), emptySet()),
                "Throws exception if no username provided");

        assertThrows(IllegalArgumentException.class,
                () -> dtdSvc.updateDecorators(username, null, emptySet(), emptySet()),
                "Throws exception if no ref provided");

        assertThrows(UnsupportedOperationException.class,
                () -> dtdSvc.updateDecorators(username, mkRef(EntityKind.APPLICATION, -1L), emptySet(), emptySet()),
                "Throws exception if no unsupported ref provided");

        Long dtId = dataTypeHelper.createDataType("updateDecorators");
        Long dtId2 = dataTypeHelper.createDataType("updateDecorators2");
        Long dtId3 = dataTypeHelper.createDataType("updateDecorators3");
        dtdSvc.updateDecorators(username, flow.entityReference(), asSet(dtId, dtId2), emptySet());

        Collection<DataTypeDecorator> flowDecorators = dtdSvc.findByFlowIds(asSet(flow.entityReference().id()), EntityKind.LOGICAL_DATA_FLOW);
        assertEquals(asSet(dtId, dtId2), map(flowDecorators, DataTypeDecorator::dataTypeId), "Adds data types that do not exist on flow");

        dtdSvc.updateDecorators(username, flow.entityReference(), emptySet(), asSet(dtId3));
        Collection<DataTypeDecorator> removeDtNotAssociated = dtdSvc.findByFlowIds(asSet(flow.entityReference().id()), EntityKind.LOGICAL_DATA_FLOW);
        assertEquals(asSet(dtId, dtId2), map(removeDtNotAssociated, DataTypeDecorator::dataTypeId), "Removing dt not associated does not change set of decorators");
        
        dtdSvc.updateDecorators(username, flow.entityReference(), emptySet(), asSet(dtId2));
        Collection<DataTypeDecorator> removedDatatype = dtdSvc.findByFlowIds(asSet(flow.entityReference().id()), EntityKind.LOGICAL_DATA_FLOW);
        assertEquals(asSet(dtId), map(removedDatatype, DataTypeDecorator::dataTypeId), "Removed associated datatype");
    }


    @Test
    public void findSuggestedByEntityRef() {
        String username = mkName("updateDecorators");
        EntityReference a = appHelper.createNewApp("a", ouIds.a);

        EntityReference b = appHelper.createNewApp("b", ouIds.a1);
        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);

        Set<DataType> suggestedWhenNoFlows = dtSvc.findSuggestedByEntityRef(flow.entityReference());
        assertTrue(suggestedWhenNoFlows.isEmpty(), "If no flows associated to entity should return empty list");

        EntityReference c = appHelper.createNewApp("b", ouIds.a1);
        LogicalFlow flow2 = lfHelper.createLogicalFlow(b, c);

        Long dtId = dataTypeHelper.createDataType("updateDecorators");
        Long dtId2 = dataTypeHelper.createDataType("updateDecorators2");
        dtdSvc.updateDecorators(username, flow.entityReference(), asSet(dtId), emptySet());
        dtdSvc.updateDecorators(username, flow2.entityReference(), asSet(dtId, dtId2), emptySet());

        Collection<DataType> suggestedWhenUpstream = dtSvc.findSuggestedByEntityRef(flow.entityReference());
        assertEquals(asSet(dtId), map(suggestedWhenUpstream, d -> d.id().get()), "Should return suggested data types based on the upstream app");

        Collection<DataType> suggestedWhenSrcHasUpstreamAndDownStream = dtSvc.findSuggestedByEntityRef(flow2.entityReference());
        assertEquals(asSet(dtId, dtId2),
                map(suggestedWhenSrcHasUpstreamAndDownStream, d -> d.id().get()),
                "Should return suggested data types based on up and down stream flows on the upstream app");

        Long specId = psHelper.createPhysicalSpec(a, "updateDecorators");
        pfHelper.createPhysicalFlow(flow.entityReference().id(), specId, "updateDecorators");

        Collection<DataType> suggestedForPsWhenUpstream = dtSvc.findSuggestedByEntityRef(mkRef(EntityKind.PHYSICAL_SPECIFICATION, specId));
        assertEquals(asSet(dtId), map(suggestedForPsWhenUpstream, d -> d.id().get()), "Should return suggested data types based on the upstream app");

        Long specId2 = psHelper.createPhysicalSpec(a, "updateDecorators");
        Collection<DataType> specNotInvolvedInFlows = dtSvc.findSuggestedByEntityRef(mkRef(EntityKind.PHYSICAL_SPECIFICATION, specId2));

        assertTrue(specNotInvolvedInFlows.isEmpty(), "Spec not involved in flows should return empty list");
    }


    @Test
    public void findDatatypeUsageCharacteristics() {
        String username = mkName("findDatatypeUsageCharacteristics");
        EntityReference a = appHelper.createNewApp("a", ouIds.a);

        assertThrows(
                IllegalArgumentException.class,
                () -> dtdSvc.findDatatypeUsageCharacteristics(a),
                "Throw exception for entities other than physical specs and logical flows");

        EntityReference b = appHelper.createNewApp("b", ouIds.a1);
        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);

        Collection<DataTypeUsageCharacteristics> noDecorators = dtdSvc.findDatatypeUsageCharacteristics(flow.entityReference());
        assertEquals(emptyList(), noDecorators, "If there are no decorators on a flow the list of usage characteristics should be empty");

        Long dtId = dataTypeHelper.createDataType("findDatatypeUsageCharacteristics");
        Long dtId2 = dataTypeHelper.createDataType("findDatatypeUsageCharacteristics");
        dtdSvc.updateDecorators(username, flow.entityReference(), asSet(dtId, dtId2), emptySet());

        Collection<DataTypeUsageCharacteristics> decoratorsOnFlow = dtdSvc.findDatatypeUsageCharacteristics(flow.entityReference());

        assertEquals(asSet(dtId, dtId2), map(decoratorsOnFlow, DataTypeUsageCharacteristics::dataTypeId), "Returns usage characteristics for each data type associated to a flow");
        assertEquals(asSet(true), map(decoratorsOnFlow, DataTypeUsageCharacteristics::isRemovable), "Characteristics suggest all flows are removable when there are no underlying physical flows");

        Long specId = psHelper.createPhysicalSpec(a, "findDatatypeUsageCharacteristics");
        pfHelper.createPhysicalFlow(flow.entityReference().id(), specId, "findDatatypeUsageCharacteristics");
        dtdSvc.updateDecorators(username, mkRef(EntityKind.PHYSICAL_SPECIFICATION, specId), asSet(dtId), emptySet());

        Collection<DataTypeUsageCharacteristics> decoratorsOnFlowWithUnderlyingSpec = dtdSvc.findDatatypeUsageCharacteristics(flow.entityReference());
        DataTypeUsageCharacteristics dtDecorator = decoratorsOnFlowWithUnderlyingSpec.stream().filter(d -> d.dataTypeId() == dtId).findFirst().get();
        DataTypeUsageCharacteristics dt2Decorator = decoratorsOnFlowWithUnderlyingSpec.stream().filter(d -> d.dataTypeId() == dtId2).findFirst().get();

        assertFalse(dtDecorator.isRemovable(), "When underlying physical with datatype should not be removable");
        assertTrue(dt2Decorator.isRemovable(), "When no underlying physical datatype should remain removable");

        Collection<DataTypeUsageCharacteristics> usageCharacteristicsForSpec = dtdSvc.findDatatypeUsageCharacteristics(mkRef(EntityKind.PHYSICAL_SPECIFICATION, specId));
        assertEquals(asSet(dtId), map(usageCharacteristicsForSpec, DataTypeUsageCharacteristics::dataTypeId), "Returns usage characteristics for each data type associated to a spec");
        assertTrue(first(usageCharacteristicsForSpec).isRemovable(), "Specs should always be flagged as removable");
    }
}
