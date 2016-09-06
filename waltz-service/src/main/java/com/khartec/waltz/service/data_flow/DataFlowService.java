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
import com.khartec.waltz.data.data_flow_decorator.DataFlowDecoratorDao;
import com.khartec.waltz.data.data_type.DataTypeIdSelectorFactory;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.dataflow.DataFlow;
import com.khartec.waltz.model.dataflow.DataFlowMeasures;
import com.khartec.waltz.model.dataflow.DataFlowStatistics;
import com.khartec.waltz.model.dataflow.ImmutableDataFlowStatistics;
import com.khartec.waltz.model.tally.Tally;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;


@Service
public class DataFlowService {

    private final DataFlowDao dataFlowDao;
    private final DataFlowStatsDao dataFlowStatsDao;
    private final DataFlowDecoratorDao dataFlowDecoratorDao;
    private final ApplicationIdSelectorFactory idSelectorFactory;
    private final DataTypeIdSelectorFactory dataTypeIdSelectorFactory;


    @Autowired
    public DataFlowService(DataFlowDao dataFlowDao, 
                           DataFlowStatsDao dataFlowStatsDao, 
                           DataFlowDecoratorDao dataFlowDecoratorDao, 
                           ApplicationIdSelectorFactory appIdSelectorFactory,
                           DataTypeIdSelectorFactory dataTypeIdSelectorFactory) {
        checkNotNull(dataFlowDao, "dataFlowDao must not be null");
        checkNotNull(dataFlowStatsDao, "dataFlowStatsDao cannot be null");
        checkNotNull(dataFlowDecoratorDao, "dataFlowDecoratorDao cannot be null");
        checkNotNull(appIdSelectorFactory, "idSelectorFactory cannot be null");
        checkNotNull(dataTypeIdSelectorFactory, "dataTypeIdSelectorFactory cannot be null");

        this.idSelectorFactory = appIdSelectorFactory;
        this.dataFlowStatsDao = dataFlowStatsDao;
        this.dataFlowDao = dataFlowDao;
        this.dataFlowDecoratorDao = dataFlowDecoratorDao;
        this.dataTypeIdSelectorFactory = dataTypeIdSelectorFactory;
    }


    public List<DataFlow> findByEntityReference(EntityReference ref) {
        return dataFlowDao.findByEntityReference(ref);
    }


    public List<DataFlow> findByAppIdSelector(IdSelectionOptions options) {
        Select<Record1<Long>> appIdSelector = idSelectorFactory.apply(options);
        return dataFlowDao.findByApplicationIdSelector(appIdSelector);
    }


    public DataFlow addFlow(DataFlow flow) {
        if (flow.source().equals(flow.target())) {
            throw new IllegalArgumentException("Cannot have a flow with same source and target");
        }

        return dataFlowDao.addFlow(flow);
    }


    public int removeFlows(List<Long> flowIds) {
        dataFlowDecoratorDao.removeAllDecoratorsForFlowIds(flowIds);
        return dataFlowDao.removeFlows(flowIds);
    }


    public DataFlowStatistics calculateStats(IdSelectionOptions options) {

        Select<Record1<Long>> appIdSelector = idSelectorFactory.apply(options);
        List<Tally<String>> dataTypeCounts = FunctionUtilities.time("DFS.dataTypes", () -> dataFlowStatsDao.tallyDataTypes(appIdSelector));
        DataFlowMeasures appCounts = FunctionUtilities.time("DFS.appCounts", () -> dataFlowStatsDao.countDistinctAppInvolvement(appIdSelector));
        DataFlowMeasures flowCounts = FunctionUtilities.time("DFS.flowCounts", () -> dataFlowStatsDao.countDistinctFlowInvolvement(appIdSelector));

        return ImmutableDataFlowStatistics.builder()
                .dataTypeCounts(dataTypeCounts)
                .appCounts(appCounts)
                .flowCounts(flowCounts)
                .build();
    }


    public List<Tally<String>> tallyByDataType() {
        return dataFlowStatsDao.tallyDataTypes(DSL.select(APPLICATION.ID).from(APPLICATION));
    }


    public Collection<DataFlow> findByDataTypeIdSelector(IdSelectionOptions options) {
        Select<Record1<Long>> dataTypeIdSelector = dataTypeIdSelectorFactory.apply(options);
        return dataFlowDao.findByDataTypeIdSelector(dataTypeIdSelector);
    }

}
