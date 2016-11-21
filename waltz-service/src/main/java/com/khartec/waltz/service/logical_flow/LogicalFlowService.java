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

package com.khartec.waltz.service.logical_flow;

import com.khartec.waltz.common.FunctionUtilities;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.logical_flow.LogicalFlowDao;
import com.khartec.waltz.data.logical_flow.LogicalFlowIdSelectorFactory;
import com.khartec.waltz.data.logical_flow.LogicalFlowStatsDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.logical_flow.ImmutableLogicalFlowStatistics;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.model.logical_flow.LogicalFlowMeasures;
import com.khartec.waltz.model.logical_flow.LogicalFlowStatistics;
import com.khartec.waltz.model.tally.TallyPack;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.data_flow_decorator.DataFlowDecoratorService;
import com.khartec.waltz.service.usage_info.DataTypeUsageService;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.data.JooqUtilities.DB_EXECUTOR_POOL;


@Service
public class LogicalFlowService {

    private final LogicalFlowDao logicalFlowDao;
    private final LogicalFlowStatsDao logicalFlowStatsDao;
    private final DataFlowDecoratorService dataFlowDecoratorService;
    private final ApplicationIdSelectorFactory appIdSelectorFactory;
    private final LogicalFlowIdSelectorFactory logicalFlowIdSelectorFactory;
    private final DataTypeUsageService dataTypeUsageService;
    private final ChangeLogService changeLogService;


    @Autowired
    public LogicalFlowService(LogicalFlowDao logicalFlowDao,
                              LogicalFlowStatsDao logicalFlowStatsDao,
                              DataFlowDecoratorService dataFlowDecoratorService,
                              ApplicationIdSelectorFactory appIdSelectorFactory,
                              LogicalFlowIdSelectorFactory logicalFlowIdSelectorFactory,
                              DataTypeUsageService dataTypeUsageService, 
                              ChangeLogService changeLogService) {
        checkNotNull(logicalFlowDao, "logicalFlowDao must not be null");
        checkNotNull(logicalFlowStatsDao, "logicalFlowStatsDao cannot be null");
        checkNotNull(dataFlowDecoratorService, "dataFlowDecoratorService cannot be null");
        checkNotNull(appIdSelectorFactory, "appIdSelectorFactory cannot be null");
        checkNotNull(dataTypeUsageService, "dataTypeUsageService cannot be null");
        checkNotNull(logicalFlowIdSelectorFactory, "logicalFlowIdSelectorFactory cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.appIdSelectorFactory = appIdSelectorFactory;
        this.logicalFlowStatsDao = logicalFlowStatsDao;
        this.logicalFlowDao = logicalFlowDao;
        this.dataFlowDecoratorService = dataFlowDecoratorService;
        this.dataTypeUsageService = dataTypeUsageService;
        this.logicalFlowIdSelectorFactory = logicalFlowIdSelectorFactory;
        this.changeLogService = changeLogService;
    }


    public List<LogicalFlow> findByEntityReference(EntityReference ref) {
        return logicalFlowDao.findByEntityReference(ref);
    }


    public LogicalFlow getById(long flowId) {
        return logicalFlowDao.findByFlowId(flowId);
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
    public List<LogicalFlow> findBySelector(IdSelectionOptions options) {
        return logicalFlowDao.findBySelector(logicalFlowIdSelectorFactory.apply(options));
    }


    public LogicalFlow findBySourceAndTarget(EntityReference source, EntityReference target) {
        return logicalFlowDao.findBySourceAndTarget(source, target);
    }


    public LogicalFlow addFlow(LogicalFlow flow, String username) {
        if (flow.source().equals(flow.target())) {
            throw new IllegalArgumentException("Cannot have a flow with same source and target");
        }

        auditFlowChange("added", flow, username);
        return logicalFlowDao.addFlow(flow);
    }


    public int removeFlows(List<Long> flowIds, String username) {
        List<LogicalFlow> logicalFlows = logicalFlowDao.findByFlowIds(flowIds);
        int deleted = logicalFlowDao.removeFlows(flowIds);
        dataFlowDecoratorService.deleteAllDecoratorsForFlowIds(flowIds);

        Set<EntityReference> affectedEntityRefs = logicalFlows.stream()
                .flatMap(df -> Stream.of(df.source(), df.target()))
                .collect(Collectors.toSet());

        logicalFlows.forEach(flow -> auditFlowChange("removed", flow, username));
        dataTypeUsageService.recalculateForApplications(affectedEntityRefs.toArray(new EntityReference[affectedEntityRefs.size()]));

        return deleted;
    }

    private void auditFlowChange(String verb, LogicalFlow flow, String username) {
        ImmutableChangeLog logEntry = ImmutableChangeLog.builder()
                .parentReference(flow.source())
                .severity(Severity.INFORMATION)
                .userId(username)
                .message(String.format(
                        "Flow %s between: %s and %s",
                        verb,
                        flow.source().name().orElse(Long.toString(flow.source().id())),
                        flow.target().name().orElse(Long.toString(flow.target().id()))))
                .build();

        changeLogService.write(logEntry);
        changeLogService.write(logEntry.withParentReference(flow.target()));
    }


    /**
     * Calculate Stats by selector
     * @param options
     * @return
     */
    public LogicalFlowStatistics calculateStats(IdSelectionOptions options) {
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


    private LogicalFlowStatistics calculateStatsForAppIdSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");

        Select<Record1<Long>> appIdSelector = appIdSelectorFactory.apply(options);

        Future<List<TallyPack<String>>> dataTypeCounts = DB_EXECUTOR_POOL.submit(() ->
                FunctionUtilities.time("DFS.dataTypes",
                    () -> logicalFlowStatsDao.tallyDataTypesByAppIdSelector(appIdSelector)));

        Future<LogicalFlowMeasures> appCounts = DB_EXECUTOR_POOL.submit(() ->
                FunctionUtilities.time("DFS.appCounts",
                    () -> logicalFlowStatsDao.countDistinctAppInvolvementByAppIdSelector(appIdSelector)));

        Future<LogicalFlowMeasures> flowCounts = DB_EXECUTOR_POOL.submit(() ->
                FunctionUtilities.time("DFS.flowCounts",
                    () -> logicalFlowStatsDao.countDistinctFlowInvolvementByAppIdSelector(appIdSelector)));

        Supplier<ImmutableLogicalFlowStatistics> statSupplier = Unchecked.supplier(() -> ImmutableLogicalFlowStatistics.builder()
                .dataTypeCounts(dataTypeCounts.get())
                .appCounts(appCounts.get())
                .flowCounts(flowCounts.get())
                .build());

        return statSupplier.get();
    }

}
