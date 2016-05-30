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
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.application.ApplicationIdSelectionOptions;
import com.khartec.waltz.model.dataflow.DataFlow;
import com.khartec.waltz.model.dataflow.DataFlowMeasures;
import com.khartec.waltz.model.dataflow.DataFlowStatistics;
import com.khartec.waltz.model.dataflow.ImmutableDataFlowStatistics;
import com.khartec.waltz.model.tally.StringTally;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class DataFlowService {

    private final DataFlowDao dataFlowDao;
    private final DataFlowStatsDao dataFlowStatsDao;
    private final ApplicationIdSelectorFactory idSelectorFactory;


    @Autowired
    public DataFlowService(DataFlowDao dataFlowDao, DataFlowStatsDao dataFlowStatsDao, ApplicationIdSelectorFactory idSelectorFactory) {
        checkNotNull(dataFlowDao, "dataFlowDao must not be null");
        checkNotNull(dataFlowStatsDao, "dataFlowStatsDao cannot be null");
        checkNotNull(idSelectorFactory, "idSelectorFactory cannot be null");

        this.idSelectorFactory = idSelectorFactory;
        this.dataFlowStatsDao = dataFlowStatsDao;
        this.dataFlowDao = dataFlowDao;
    }


    public List<DataFlow> findByEntityReference(EntityReference ref) {
        return dataFlowDao.findByEntityReference(ref);
    }


    public List<DataFlow> findByAppIdSelector(ApplicationIdSelectionOptions options) {
        Select<Record1<Long>> appIdSelector = idSelectorFactory.apply(options);
        return dataFlowDao.findByApplicationIdSelector(appIdSelector);
    }


    public int[] addFlows(List<DataFlow> flows) {
        return dataFlowDao.addFlows(flows);
    }


    public int[] removeFlows(List<DataFlow> flows) {
        return dataFlowDao.removeFlows(flows);
    }


    public DataFlowStatistics calculateStats(ApplicationIdSelectionOptions options) {

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

}
