/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.service.logical_flow;

import com.khartec.waltz.common.FunctionUtilities;
import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.data.DBExecutorPoolInterface;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.logical_flow.LogicalFlowDao;
import com.khartec.waltz.data.logical_flow.LogicalFlowIdSelectorFactory;
import com.khartec.waltz.data.logical_flow.LogicalFlowStatsDao;
import com.khartec.waltz.data.physical_flow.PhysicalFlowDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.logical_flow.*;
import com.khartec.waltz.model.physical_flow.PhysicalFlow;
import com.khartec.waltz.model.tally.TallyPack;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.data_flow_decorator.LogicalFlowDecoratorService;
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

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtc;
import static com.khartec.waltz.model.EntityKind.LOGICAL_DATA_FLOW;


@Service
public class LogicalFlowService {

    private static final String PROVENANCE = "waltz";

    private final ApplicationIdSelectorFactory appIdSelectorFactory;
    private final ChangeLogService changeLogService;
    private final LogicalFlowDecoratorService logicalFlowDecoratorService;
    private final DataTypeUsageService dataTypeUsageService;
    private final DBExecutorPoolInterface dbExecutorPool;
    private final LogicalFlowDao logicalFlowDao;
    private final LogicalFlowIdSelectorFactory logicalFlowIdSelectorFactory;
    private final LogicalFlowStatsDao logicalFlowStatsDao;
    private final PhysicalFlowDao physicalFlowDao;


    @Autowired
    public LogicalFlowService(ApplicationIdSelectorFactory appIdSelectorFactory,
                              ChangeLogService changeLogService,
                              LogicalFlowDecoratorService logicalFlowDecoratorService,
                              DataTypeUsageService dataTypeUsageService,
                              DBExecutorPoolInterface dbExecutorPool,
                              LogicalFlowDao logicalFlowDao,
                              LogicalFlowStatsDao logicalFlowStatsDao,
                              LogicalFlowIdSelectorFactory logicalFlowIdSelectorFactory,
                              PhysicalFlowDao physicalFlowDao) {
        checkNotNull(appIdSelectorFactory, "appIdSelectorFactory cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(dbExecutorPool, "dbExecutorPool cannot be null");
        checkNotNull(logicalFlowDecoratorService, "dataFlowDecoratorService cannot be null");
        checkNotNull(dataTypeUsageService, "dataTypeUsageService cannot be null");
        checkNotNull(logicalFlowDao, "logicalFlowDao must not be null");
        checkNotNull(logicalFlowStatsDao, "logicalFlowStatsDao cannot be null");
        checkNotNull(logicalFlowIdSelectorFactory, "logicalFlowIdSelectorFactory cannot be null");
        checkNotNull(physicalFlowDao, "physicalFlowDao cannot be null");

        this.appIdSelectorFactory = appIdSelectorFactory;
        this.changeLogService = changeLogService;
        this.logicalFlowDecoratorService = logicalFlowDecoratorService;
        this.dataTypeUsageService = dataTypeUsageService;
        this.dbExecutorPool = dbExecutorPool;
        this.logicalFlowStatsDao = logicalFlowStatsDao;
        this.logicalFlowDao = logicalFlowDao;
        this.logicalFlowIdSelectorFactory = logicalFlowIdSelectorFactory;
        this.physicalFlowDao = physicalFlowDao;
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


    public LogicalFlow addFlow(AddLogicalFlowCommand addCmd, String username) {
        rejectIfSelfLoop(addCmd);

        auditFlowChange(
                "added",
                addCmd.source(),
                addCmd.target(),
                username,
                Operation.ADD);

        LogicalFlow flowToAdd = ImmutableLogicalFlow.builder()
                .source(addCmd.source())
                .target(addCmd.target())
                .lastUpdatedAt(nowUtc())
                .lastUpdatedBy(username)
                .build();

        LogicalFlow logicalFlow = logicalFlowDao.addFlow(flowToAdd);

        return logicalFlow;
    }


    private void rejectIfSelfLoop(AddLogicalFlowCommand addCmd) {
        boolean sameKind = addCmd.source().kind().equals(addCmd.target().kind());
        boolean sameId = addCmd.source().id() == addCmd.target().id();

        if (sameKind && sameId) {
            throw new IllegalArgumentException("Cannot have a flow with same source and target");
        }
    }


    /**
     * Removes the given logical flow and creates an audit log entry.
     * The removal is a soft removal. After the removal usage stats are recalculated
     *
     * todo: #WALTZ-1894 for cleanup task
     *
     * @param flowId
     * @param username
     * @return
     */
    public int removeFlow(Long flowId, String username) {
        LogicalFlow logicalFlow = logicalFlowDao.findByFlowId(flowId);

        int deleted = logicalFlowDao.removeFlow(flowId, username);

        Set<EntityReference> affectedEntityRefs = SetUtilities.fromArray(logicalFlow.source(), logicalFlow.target());

        auditFlowChange(
                "removed",
                logicalFlow.source(),
                logicalFlow.target(),
                username,
                Operation.REMOVE);

        dataTypeUsageService.recalculateForApplications(affectedEntityRefs);

        return deleted;
    }


    /**
     * Calculate Stats by selector
     * @param options
     * @return
     */
    public LogicalFlowStatistics calculateStats(IdSelectionOptions options) {
        switch (options.entityReference().kind()) {
            case APP_GROUP:
            case MEASURABLE:
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

        Future<List<TallyPack<String>>> dataTypeCounts = dbExecutorPool.submit(() ->
                FunctionUtilities.time("DFS.dataTypes",
                    () -> logicalFlowStatsDao.tallyDataTypesByAppIdSelector(appIdSelector)));

        Future<LogicalFlowMeasures> appCounts = dbExecutorPool.submit(() ->
                FunctionUtilities.time("DFS.appCounts",
                    () -> logicalFlowStatsDao.countDistinctAppInvolvementByAppIdSelector(appIdSelector)));

        Future<LogicalFlowMeasures> flowCounts = dbExecutorPool.submit(() ->
                FunctionUtilities.time("DFS.flowCounts",
                    () -> logicalFlowStatsDao.countDistinctFlowInvolvementByAppIdSelector(appIdSelector)));

        Supplier<ImmutableLogicalFlowStatistics> statSupplier = Unchecked.supplier(() -> ImmutableLogicalFlowStatistics.builder()
                .dataTypeCounts(dataTypeCounts.get())
                .appCounts(appCounts.get())
                .flowCounts(flowCounts.get())
                .build());

        return statSupplier.get();
    }


    private void ensureNoAssociatedPhysicalFlows(LogicalFlow logicalFlow) {
        List<PhysicalFlow> physicalFlows = physicalFlowDao.findByProducerAndConsumer(
                logicalFlow.source(),
                logicalFlow.target());

        if(physicalFlows.size() > 0) { throw new RuntimeException("Could not delete Logical Flow because it has associated Physical Flows"); }
    }


    private void auditFlowChange(String verb, EntityReference source, EntityReference target, String username, Operation operation) {
        ImmutableChangeLog logEntry = ImmutableChangeLog.builder()
                .parentReference(source)
                .severity(Severity.INFORMATION)
                .userId(username)
                .message(String.format(
                        "Flow %s between: %s and %s",
                        verb,
                        source.name().orElse(Long.toString(source.id())),
                        target.name().orElse(Long.toString(target.id()))))
                .childKind(LOGICAL_DATA_FLOW)
                .operation(operation)
                .build();

        changeLogService.write(logEntry);
        changeLogService.write(logEntry.withParentReference(target));
    }

}
