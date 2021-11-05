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
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.datatype.DataType;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.finos.waltz.model.entity_search.ImmutableEntitySearchOptions;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.service.data_type.DataTypeService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.SetUtilities.map;
import static org.junit.Assert.*;

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
        assertEquals("Should return empty list when no datatypes created", emptyList(), dts);

        dataTypeHelper.createDataType(1L, "dt1", "DT1");
        dataTypeHelper.createDataType(2L, "dt2", "DT2");

        List<DataType> dataTypes = dtSvc.findAll();
        assertEquals("Returns all added datatypes", 2, dataTypes.size());
        assertEquals("Returns the correct datatypes", asSet(1L, 2L), map(dataTypes, d -> d.id().get()));
    }


    @Test
    public void getDataTypeById() {
        dataTypeHelper.clearAllDataTypes();

        DataType noDataTypesAdded = dtSvc.getDataTypeById(1L);
        assertNull("When no datatypes created returns null", noDataTypesAdded);

        dataTypeHelper.createDataType(1L, "dt1", "DT1");
        dataTypeHelper.createDataType(2L, "dt2", "DT2");

        DataType dt1 = dtSvc.getDataTypeById(1L);
        assertEquals("returns the datatype with that id", 1L, dt1.id().get().longValue());
        assertEquals("retrieved data type has the correct name", "dt1", dt1.name());

        DataType dt2 = dtSvc.getDataTypeById(2L);
        assertEquals("returns the datatype with that id", 2L, dt2.id().get().longValue());
        assertEquals("retrieved data type has the correct name", "dt2", dt2.name());
    }


    @Test
    public void getDataTypeByCode() {
        dataTypeHelper.clearAllDataTypes();

        DataType noDataTypesAdded = dtSvc.getDataTypeByCode("DT1");
        assertNull("When no datatypes created returns null", noDataTypesAdded);

        dataTypeHelper.createDataType(1L, "dt1", "DT1");

        DataType dt1 = dtSvc.getDataTypeByCode("DT1");
        assertEquals("retrieved data type has the correct name", 1L, dt1.id().get().longValue());
        assertEquals("retrieved data type has the correct name", "dt1", dt1.name());
        assertEquals("retrieved data type has the correct code", "DT1", dt1.code());
    }


    @Test
    public void findSuggestedBySourceEntityRef() {
        dataTypeHelper.clearAllDataTypes();

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);
        EntityReference c = appHelper.createNewApp("b", ouIds.b);

        Collection<DataType> noDecoratorsOnFlow = dtSvc.findSuggestedBySourceEntityRef(a);
        assertEquals("if source app has no logical flows returns empty list", emptyList(), noDecoratorsOnFlow);

        LogicalFlow ab = lfHelper.createLogicalFlow(a, b);
        assertEquals("if source app has no flow decorators returns empty list", emptyList(), noDecoratorsOnFlow);

        dataTypeHelper.createDataType(1L, "dt1", "DT1");
        lfHelper.createLogicalFlowDecorators(ab.entityReference(), asSet(1L));
        Set<Long> suggestedDtIds = map(dtSvc.findSuggestedBySourceEntityRef(a), dtd -> dtd.entityReference().id());
        assertEquals("returns data type associated to the source application", asSet(1L), suggestedDtIds);

        LogicalFlow bc = lfHelper.createLogicalFlow(b, c);
        dataTypeHelper.createDataType(2L, "dt2", "DT2");
        lfHelper.createLogicalFlowDecorators(bc.entityReference(), asSet(2L));
        Set<Long> onlySourceDts = map(dtSvc.findSuggestedBySourceEntityRef(a), dtd -> dtd.entityReference().id());
        assertEquals("does not return dts associated to only the target app", asSet(1L), onlySourceDts);

        lfHelper.createLogicalFlowDecorators(ab.entityReference(), asSet(2L));
        Set<Long> allSourceDts = map(dtSvc.findSuggestedBySourceEntityRef(a), dtd -> dtd.entityReference().id());
        assertEquals("returns all dts associated to source app", asSet(1L, 2L), allSourceDts);

        LogicalFlow ac = lfHelper.createLogicalFlow(a, c);
        lfHelper.createLogicalFlowDecorators(ac.entityReference(), asSet(2L));
        Set<Long> setOfDts = map(dtSvc.findSuggestedBySourceEntityRef(a), dtd -> dtd.entityReference().id());
        assertEquals("returns all dts associated to source app", asSet(1L, 2L), setOfDts);
    }


    @Test
    public void search() {
        dataTypeHelper.clearAllDataTypes();

        assertThrows(
                "null search options throws exception",
                IllegalArgumentException.class,
                () -> dtSvc.search(null));

        EntitySearchOptions dt1Search = mkDataTypeSearchOptions("dt1");
        EntitySearchOptions emptySearch = mkDataTypeSearchOptions("");
        EntitySearchOptions testSearch = mkDataTypeSearchOptions("TEST");

        assertEquals("Search will return an empty list when no datatypes", emptyList(), dtSvc.search(dt1Search));

        dataTypeHelper.createDataType(1L, "dt1", "DT1");
        dataTypeHelper.createDataType(2L, "dt2", "DT2");
        dataTypeHelper.createDataType(3L, "dt10", "DT10");

        assertEquals("Empty search string will return all dts",
                asSet(1L, 2L, 3L),
                map(dtSvc.search(emptySearch), dt -> dt.id().get()));

        assertEquals("Search will return an empty list when no match",
                emptyList(),
                dtSvc.search(testSearch));

        assertEquals("Search will return all where matches to part of name",
                asSet(1L, 3L),
                map(dtSvc.search(dt1Search), dt -> dt.id().get()));

        dataTypeHelper.createDataType(4L, "test", "TESTING");

        assertEquals("search is case insensitive",
                asSet(4L),
                map(dtSvc.search(testSearch), dt -> dt.id().get()));
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