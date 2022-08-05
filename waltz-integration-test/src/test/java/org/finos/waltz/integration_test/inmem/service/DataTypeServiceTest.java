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
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.datatype.DataType;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.finos.waltz.model.entity_search.ImmutableEntitySearchOptions;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.service.data_type.DataTypeService;
import org.finos.waltz.test_common_again.helpers.AppHelper;
import org.finos.waltz.test_common_again.helpers.DataTypeHelper;
import org.finos.waltz.test_common_again.helpers.LogicalFlowHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.SetUtilities.map;
import static org.junit.jupiter.api.Assertions.*;

public class DataTypeServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private DataTypeService dtSvc;

    @Autowired
    private LogicalFlowHelper lfHelper;

    @Autowired
    private AppHelper appHelper;

    @Autowired
    private DataTypeHelper dataTypeHelper;


    @Test
    public void findsAllDatatypes() {
        dataTypeHelper.clearAllDataTypes();

        List<DataType> dts = dtSvc.findAll();
        assertEquals(emptyList(), dts, "Should return empty list when no datatypes created");

        dataTypeHelper.createDataType(1L, "dt1", "DT1");
        dataTypeHelper.createDataType(2L, "dt2", "DT2");

        List<DataType> dataTypes = dtSvc.findAll();
        assertEquals(2, dataTypes.size(), "Returns all added datatypes");
        assertEquals(asSet(1L, 2L), map(dataTypes, d -> d.id().get()), "Returns the correct datatypes");
    }


    @Test
    public void getDataTypeById() {
        dataTypeHelper.clearAllDataTypes();

        DataType noDataTypesAdded = dtSvc.getDataTypeById(1L);
        assertNull(noDataTypesAdded, "When no datatypes created returns null");

        dataTypeHelper.createDataType(1L, "dt1", "DT1");
        dataTypeHelper.createDataType(2L, "dt2", "DT2");

        DataType dt1 = dtSvc.getDataTypeById(1L);
        assertEquals(1L, dt1.id().get().longValue(), "returns the datatype with that id");
        assertEquals(dt1.name(), "dt1", "retrieved data type has the correct name");

        DataType dt2 = dtSvc.getDataTypeById(2L);
        assertEquals(2L, dt2.id().get().longValue(), "returns the datatype with that id");
        assertEquals("dt2", dt2.name(), "retrieved data type has the correct name");
    }


    @Test
    public void getDataTypeByCode() {
        dataTypeHelper.clearAllDataTypes();

        DataType noDataTypesAdded = dtSvc.getDataTypeByCode("DT1");
        assertNull(noDataTypesAdded, "When no datatypes created returns null");

        dataTypeHelper.createDataType(1L, "dt1", "DT1");

        DataType dt1 = dtSvc.getDataTypeByCode("DT1");
        assertEquals(1L, dt1.id().get().longValue(), "retrieved data type has the correct name");
        assertEquals("dt1", dt1.name(), "retrieved data type has the correct name");
        assertEquals("DT1", dt1.code(), "retrieved data type has the correct code");
    }


    @Test
    public void findSuggestedBySourceEntityRef() {
        dataTypeHelper.clearAllDataTypes();

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);
        EntityReference c = appHelper.createNewApp("b", ouIds.b);

        Set<DataType> noDecoratorsOnFlow = dtSvc.findSuggestedByEntityRef(a);
        assertTrue(noDecoratorsOnFlow.isEmpty(), "if source app has no logical flows returns empty list");

        LogicalFlow ab = lfHelper.createLogicalFlow(a, b);
        assertTrue(noDecoratorsOnFlow.isEmpty(), "if source app has no flow decorators returns empty list");

        dataTypeHelper.createDataType(1L, "dt1", "DT1");
        lfHelper.createLogicalFlowDecorators(ab.entityReference(), asSet(1L));
        Set<Long> suggestedDtIds = map(dtSvc.findSuggestedByEntityRef(a), dtd -> dtd.entityReference().id());
        assertEquals(asSet(1L), suggestedDtIds, "returns data type associated to the source application");

        LogicalFlow bc = lfHelper.createLogicalFlow(b, c);
        dataTypeHelper.createDataType(2L, "dt2", "DT2");
        lfHelper.createLogicalFlowDecorators(bc.entityReference(), asSet(2L));
        Set<Long> onlySourceDts = map(dtSvc.findSuggestedByEntityRef(a), dtd -> dtd.entityReference().id());
        assertEquals(asSet(1L), onlySourceDts, "does not return dts associated to only the target app");

        lfHelper.createLogicalFlowDecorators(ab.entityReference(), asSet(2L));
        Set<Long> allSourceDts = map(dtSvc.findSuggestedByEntityRef(a), dtd -> dtd.entityReference().id());
        assertEquals(asSet(1L, 2L), allSourceDts, "returns all dts associated to source app");

        LogicalFlow ac = lfHelper.createLogicalFlow(a, c);
        lfHelper.createLogicalFlowDecorators(ac.entityReference(), asSet(2L));
        Set<Long> setOfDts = map(dtSvc.findSuggestedByEntityRef(a), dtd -> dtd.entityReference().id());
        assertEquals(asSet(1L, 2L), setOfDts, "returns all dts associated to source app");
    }


    @Test
    public void search() {
        dataTypeHelper.clearAllDataTypes();

        assertThrows(
                IllegalArgumentException.class,
                () -> dtSvc.search(null),
                "null search options throws exception");

        EntitySearchOptions dt1Search = mkDataTypeSearchOptions("dt1");
        EntitySearchOptions emptySearch = mkDataTypeSearchOptions("");
        EntitySearchOptions testSearch = mkDataTypeSearchOptions("TEST");

        assertEquals(emptyList(), dtSvc.search(dt1Search), "Search will return an empty list when no datatypes");

        dataTypeHelper.createDataType(1L, "dt1", "DT1");
        dataTypeHelper.createDataType(2L, "dt2", "DT2");
        dataTypeHelper.createDataType(3L, "dt10", "DT10");

        assertEquals(asSet(1L, 2L, 3L),
                map(dtSvc.search(emptySearch), dt -> dt.id().get()),
                "Empty search string will return all dts");

        assertEquals(emptyList(),
                dtSvc.search(testSearch),
                "Search will return an empty list when no match");

        assertEquals(asSet(1L, 3L),
                map(dtSvc.search(dt1Search), dt -> dt.id().get()),
                "Search will return all where matches to part of name");

        dataTypeHelper.createDataType(4L, "test", "TESTING");

        assertEquals(asSet(4L),
                map(dtSvc.search(testSearch), dt -> dt.id().get()),
                "search is case insensitive");
    }


    private ImmutableEntitySearchOptions mkDataTypeSearchOptions(String searchQry) {
        return ImmutableEntitySearchOptions.builder()
                .addEntityKinds(EntityKind.DATA_TYPE)
                .userId("admin")
                .limit(EntitySearchOptions.DEFAULT_SEARCH_RESULTS_LIMIT)
                .searchQuery(searchQry)
                .build();
    }

}