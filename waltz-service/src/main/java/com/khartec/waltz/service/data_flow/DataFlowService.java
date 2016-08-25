/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.service.data_flow;

import com.khartec.waltz.common.FunctionUtilities;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.data_flow.DataFlowDao;
import com.khartec.waltz.data.data_flow.DataFlowStatsDao;
import com.khartec.waltz.data.data_type.DataTypeIdSelectorFactory;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.dataflow.DataFlow;
import com.khartec.waltz.model.dataflow.DataFlowMeasures;
import com.khartec.waltz.model.dataflow.DataFlowStatistics;
import com.khartec.waltz.model.dataflow.ImmutableDataFlowStatistics;
import com.khartec.waltz.model.tally.StringTally;
import com.khartec.waltz.model.tally.Tally;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class DataFlowService {

    private final ApplicationIdSelectorFactory idSelectorFactory;
    private final DataFlowDao dataFlowDao;
    private final DataFlowStatsDao dataFlowStatsDao;
    private final DataTypeIdSelectorFactory dataTypeIdSelectorFactory;
    private DataFlowRatingsService dataFlowRatingsService;


    @Autowired
    public DataFlowService(ApplicationIdSelectorFactory idSelectorFactory,
                           DataFlowDao dataFlowDao,
                           DataFlowStatsDao dataFlowStatsDao,
                           DataTypeIdSelectorFactory dataTypeIdSelectorFactory,
                           DataFlowRatingsService dataFlowRatingsService) {
        checkNotNull(idSelectorFactory, "idSelectorFactory cannot be null");
        checkNotNull(dataFlowDao, "dataFlowDao must not be null");
        checkNotNull(dataFlowStatsDao, "dataFlowStatsDao cannot be null");
        checkNotNull(dataTypeIdSelectorFactory, "dataTypeIdSelectorFactory cannot be null");
        checkNotNull(dataFlowRatingsService, "dataFlowRatingsService cannot be null");

        this.idSelectorFactory = idSelectorFactory;
        this.dataTypeIdSelectorFactory = dataTypeIdSelectorFactory;
        this.dataFlowStatsDao = dataFlowStatsDao;
        this.dataFlowDao = dataFlowDao;
        this.dataFlowRatingsService = dataFlowRatingsService;
    }


    public List<DataFlow> findByEntityReference(EntityReference ref) {
        return dataFlowDao.findByEntityReference(ref);
    }


    public List<DataFlow> findByAppIdSelector(IdSelectionOptions options) {
        Select<Record1<Long>> appIdSelector = idSelectorFactory.apply(options);
        return dataFlowDao.findByApplicationIdSelector(appIdSelector);
    }


    public List<DataFlow> findConsumersBySelector(IdSelectionOptions options,
                                                  EntityReference source,
                                                  String dataType) {
        Select<Record1<Long>> appIdSelector = idSelectorFactory.apply(options);
        return dataFlowDao.findConsumersBySelector(appIdSelector, source, dataType);
    }


    public int[] storeFlows(List<DataFlow> flows) throws SQLException {
        List<DataFlow> flowsExceptSameSourceAndTarget = flows.stream()
                .filter(f -> !f.source().equals(f.target()))
                .collect(Collectors.toList());

        List<DataFlow> flowsToStore = dataFlowRatingsService.calculateRatings(flowsExceptSameSourceAndTarget);

        return dataFlowDao.storeFlows(flowsToStore);
    }


    public int[] removeFlows(List<DataFlow> flows) {
        return dataFlowDao.removeFlows(flows);
    }


    public DataFlowStatistics calculateStats(IdSelectionOptions options) {

        Select<Record1<Long>> appIdSelector = idSelectorFactory.apply(options);
        List<StringTally> dataTypeCounts = FunctionUtilities.time("DFS.dataTypes", () -> dataFlowStatsDao.tallyDataTypes(appIdSelector));
        DataFlowMeasures appCounts = FunctionUtilities.time("DFS.appCounts", () -> dataFlowStatsDao.countDistinctAppInvolvement(appIdSelector));
        DataFlowMeasures flowCounts = FunctionUtilities.time("DFS.flowCounts", () -> dataFlowStatsDao.countDistinctFlowInvolvement(appIdSelector));

        return ImmutableDataFlowStatistics.builder()
                .dataTypeCounts(dataTypeCounts)
                .appCounts(appCounts)
                .flowCounts(flowCounts)
                .build();
    }


    public DataFlowStatistics calculateStatsForDataType(IdSelectionOptions options) {

        Select<Record1<Long>> appIdSelector = idSelectorFactory.apply(options);
        Select<Record1<Long>> dataTypeIdSelector = dataTypeIdSelectorFactory.apply(options);

        List<StringTally> dataTypeCounts = FunctionUtilities.time("DFS.dataTypes", () -> dataFlowStatsDao.tallyDataTypes(appIdSelector, dataTypeIdSelector));
        DataFlowMeasures appCounts = FunctionUtilities.time("DFS.appCounts", () -> dataFlowStatsDao.countDistinctAppInvolvementByDataType(dataTypeIdSelector));
        DataFlowMeasures flowCounts = FunctionUtilities.time("DFS.flowCounts", () -> dataFlowStatsDao.countDistinctFlowInvolvementForDataTyoe(dataTypeIdSelector));

        return ImmutableDataFlowStatistics.builder()
                .dataTypeCounts(dataTypeCounts)
                .appCounts(appCounts)
                .flowCounts(flowCounts)
                .build();
    }


    public List<Tally<String>> tallyByDataType() {
        return dataFlowDao.tallyByDataType();
    }


    public Collection<DataFlow> findByDataTypeIdSelector(IdSelectionOptions options) {
        Select<Record1<Long>> dataTypeIdSelector = dataTypeIdSelectorFactory.apply(options);
        return dataFlowDao.findByDataTypeIdSelector(dataTypeIdSelector);
    }


    public void updateRatingsForConsumers(EntityReference parentRef, String dataType, Long appId) throws SQLException {
        // get app id selector based on parentRef
        ImmutableIdSelectionOptions options = ImmutableIdSelectionOptions.builder()
                .scope(HierarchyQueryScope.CHILDREN)
                .entityReference(parentRef)
                .build();


        List<DataFlow> consumingFlows = findConsumersBySelector(options,
                ImmutableEntityReference.builder()
                        .kind(EntityKind.APPLICATION)
                        .id(appId)
                        .build(),
                dataType);

        // update the rating
        storeFlows(consumingFlows);
    }




}
