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
import com.khartec.waltz.data.data_flow.LogicalDataFlowIdSelectorFactory;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.dataflow.DataFlow;
import com.khartec.waltz.model.dataflow.DataFlowMeasures;
import com.khartec.waltz.model.dataflow.DataFlowStatistics;
import com.khartec.waltz.model.dataflow.ImmutableDataFlowStatistics;
import com.khartec.waltz.model.tally.TallyPack;
import com.khartec.waltz.service.data_flow_decorator.DataFlowDecoratorService;
import com.khartec.waltz.service.usage_info.DataTypeUsageService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class DataFlowService {

    private final DataFlowDao dataFlowDao;
    private final DataFlowStatsDao dataFlowStatsDao;
    private final DataFlowDecoratorService dataFlowDecoratorService;
    private final ApplicationIdSelectorFactory appIdSelectorFactory;
    private final LogicalDataFlowIdSelectorFactory logicalFlowIdSelectorFactory;
    private DataTypeUsageService dataTypeUsageService;


    @Autowired
    public DataFlowService(DataFlowDao dataFlowDao,
                           DataFlowStatsDao dataFlowStatsDao,
                           DataFlowDecoratorService dataFlowDecoratorService,
                           ApplicationIdSelectorFactory appIdSelectorFactory,
                           LogicalDataFlowIdSelectorFactory logicalFlowIdSelectorFactory,
                           DataTypeUsageService dataTypeUsageService) {
        checkNotNull(dataFlowDao, "dataFlowDao must not be null");
        checkNotNull(dataFlowStatsDao, "dataFlowStatsDao cannot be null");
        checkNotNull(dataFlowDecoratorService, "dataFlowDecoratorService cannot be null");
        checkNotNull(appIdSelectorFactory, "appIdSelectorFactory cannot be null");
        checkNotNull(dataTypeUsageService, "dataTypeUsageService cannot be null");
        checkNotNull(logicalFlowIdSelectorFactory, "logicalFlowIdSelectorFactory cannot be null");

        this.appIdSelectorFactory = appIdSelectorFactory;
        this.dataFlowStatsDao = dataFlowStatsDao;
        this.dataFlowDao = dataFlowDao;
        this.dataFlowDecoratorService = dataFlowDecoratorService;
        this.dataTypeUsageService = dataTypeUsageService;
        this.logicalFlowIdSelectorFactory = logicalFlowIdSelectorFactory;
    }


    public List<DataFlow> findByEntityReference(EntityReference ref) {
        return dataFlowDao.findByEntityReference(ref);
    }


    public DataFlow getById(long flowId) {
        return dataFlowDao.findByFlowId(flowId);
    }


    /**
     * Find decorators by selector. Supported desiredKinds:
     * <ul>
     *     <li>DATA_TYPE</li>
     *     <li>APPLICATION</li>
     * </ul>
     * @param options
     * @return
     */
    public List<DataFlow> findBySelector(IdSelectionOptions options) {
        return dataFlowDao.findBySelector(logicalFlowIdSelectorFactory.apply(options));
    }


    public DataFlow findBySourceAndTarget(EntityReference source, EntityReference target) {
        return dataFlowDao.findBySourceAndTarget(source, target);
    }


    public DataFlow addFlow(DataFlow flow) {
        if (flow.source().equals(flow.target())) {
            throw new IllegalArgumentException("Cannot have a flow with same source and target");
        }

        return dataFlowDao.addFlow(flow);
    }


    public int removeFlows(List<Long> flowIds) {
        List<DataFlow> dataFlows = dataFlowDao.findByFlowIds(flowIds);
        int deleted = dataFlowDao.removeFlows(flowIds);
        dataFlowDecoratorService.deleteAllDecoratorsForFlowIds(flowIds);

        Set<EntityReference> affectedEntityRefs = dataFlows.stream()
                .flatMap(df -> Stream.of(df.source(), df.target()))
                .collect(Collectors.toSet());

        dataTypeUsageService.recalculateForApplications(affectedEntityRefs.toArray(new EntityReference[affectedEntityRefs.size()]));

        return deleted;
    }


    /**
     * Calculate Stats by selector. Supported desiredKinds:
     * <ul>
     *     <li>DATA_TYPE</li>
     *     <li>APPLICATION</li>
     * </ul>
     * @param options
     * @return
     */
    public DataFlowStatistics calculateStats(IdSelectionOptions options) {
        switch (options.entityReference().kind()) {
            case APP_GROUP:
            case CAPABILITY:
            case ORG_UNIT:
            case PERSON:
            case PROCESS:
                return calculateStatsForAppIdSelector(options);
            default:
                throw new UnsupportedOperationException("Cannot calculate stats for selector kind: "+ options.entityReference().kind());
        }
    }


    private DataFlowStatistics calculateStatsForAppIdSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");

        Select<Record1<Long>> appIdSelector = appIdSelectorFactory.apply(options);
        List<TallyPack<String>> dataTypeCounts = FunctionUtilities.time("DFS.dataTypes",
                () -> dataFlowStatsDao.tallyDataTypesByAppIdSelector(appIdSelector));

        DataFlowMeasures appCounts = FunctionUtilities.time("DFS.appCounts",
                () -> dataFlowStatsDao.countDistinctAppInvolvementByAppIdSelector(appIdSelector));

        DataFlowMeasures flowCounts = FunctionUtilities.time("DFS.flowCounts",
                () -> dataFlowStatsDao.countDistinctFlowInvolvementByAppIdSelector(appIdSelector));

        return ImmutableDataFlowStatistics.builder()
                .dataTypeCounts(dataTypeCounts)
                .appCounts(appCounts)
                .flowCounts(flowCounts)
                .build();
    }

}
