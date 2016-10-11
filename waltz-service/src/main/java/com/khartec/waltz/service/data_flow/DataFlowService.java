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
import com.khartec.waltz.model.EntityKind;
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
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.EntityKind.DATA_TYPE;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;


@Service
public class DataFlowService {

    private final DataFlowDao dataFlowDao;
    private final DataFlowStatsDao dataFlowStatsDao;
    private final DataFlowDecoratorService dataFlowDecoratorService;
    private final ApplicationIdSelectorFactory appIdSelectorFactory;
    private final DataTypeIdSelectorFactory dataTypeIdSelectorFactory;
    private DataTypeUsageService dataTypeUsageService;


    @Autowired
    public DataFlowService(DataFlowDao dataFlowDao,
                           DataFlowStatsDao dataFlowStatsDao,
                           DataFlowDecoratorService dataFlowDecoratorService,
                           ApplicationIdSelectorFactory appIdSelectorFactory,
                           DataTypeIdSelectorFactory dataTypeIdSelectorFactory,
                           DataTypeUsageService dataTypeUsageService) {
        checkNotNull(dataFlowDao, "dataFlowDao must not be null");
        checkNotNull(dataFlowStatsDao, "dataFlowStatsDao cannot be null");
        checkNotNull(dataFlowDecoratorService, "dataFlowDecoratorService cannot be null");
        checkNotNull(appIdSelectorFactory, "appIdSelectorFactory cannot be null");
        checkNotNull(dataTypeIdSelectorFactory, "dataTypeIdSelectorFactory cannot be null");
        checkNotNull(dataTypeUsageService, "dataTypeUsageService cannot be null");

        this.appIdSelectorFactory = appIdSelectorFactory;
        this.dataFlowStatsDao = dataFlowStatsDao;
        this.dataFlowDao = dataFlowDao;
        this.dataFlowDecoratorService = dataFlowDecoratorService;
        this.dataTypeIdSelectorFactory = dataTypeIdSelectorFactory;
        this.dataTypeUsageService = dataTypeUsageService;
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
        if (options.desiredKind() == DATA_TYPE) {
            return (List<DataFlow>) findByDataTypeIdSelector(options);
        }
        if (options.desiredKind() == EntityKind.APPLICATION) {
            return findByAppIdSelector(options);
        }
        throw new UnsupportedOperationException("Cannot find decorators for selector desiredKind: "+options.desiredKind());
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
        if (options.desiredKind() == EntityKind.APPLICATION) {
            return calculateStatsForAppIdSelector(options);
        }
        throw new UnsupportedOperationException("Cannot calculate stats for selector desiredKind: "+options.desiredKind());
    }


    public List<TallyPack<String>> tallyByDataType() {
        return dataFlowStatsDao.tallyDataTypesByAppIdSelector(DSL.select(APPLICATION.ID).from(APPLICATION));
    }


    private List<DataFlow> findByAppIdSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> appIdSelector = appIdSelectorFactory.apply(options);
        return dataFlowDao.findByApplicationIdSelector(appIdSelector);
    }


    private Collection<DataFlow> findByDataTypeIdSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> dataTypeIdSelector = dataTypeIdSelectorFactory.apply(options);
        return dataFlowDao.findByDataTypeIdSelector(dataTypeIdSelector);
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


    public Collection<DataFlow> findByPhysicalDataArticleId(long articleId) {
        return dataFlowDao.findByPhysicalDataArticleId(articleId);
    }

}
