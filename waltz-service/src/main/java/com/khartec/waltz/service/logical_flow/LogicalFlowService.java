/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
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
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.logical_flow.*;
import com.khartec.waltz.model.tally.TallyPack;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.usage_info.DataTypeUsageService;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.CollectionUtilities.isEmpty;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtc;
import static com.khartec.waltz.model.EntityKind.LOGICAL_DATA_FLOW;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;


@Service
public class LogicalFlowService {



    private final ApplicationIdSelectorFactory appIdSelectorFactory;
    private final ChangeLogService changeLogService;
    private final DataTypeUsageService dataTypeUsageService;
    private final DBExecutorPoolInterface dbExecutorPool;
    private final LogicalFlowDao logicalFlowDao;
    private final LogicalFlowIdSelectorFactory logicalFlowIdSelectorFactory;
    private final LogicalFlowStatsDao logicalFlowStatsDao;

    @Autowired
    public LogicalFlowService(ApplicationIdSelectorFactory appIdSelectorFactory,
                              ChangeLogService changeLogService,
                              DataTypeUsageService dataTypeUsageService,
                              DBExecutorPoolInterface dbExecutorPool,
                              LogicalFlowDao logicalFlowDao,
                              LogicalFlowStatsDao logicalFlowStatsDao,
                              LogicalFlowIdSelectorFactory logicalFlowIdSelectorFactory) {
        checkNotNull(appIdSelectorFactory, "appIdSelectorFactory cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(dbExecutorPool, "dbExecutorPool cannot be null");
        checkNotNull(dataTypeUsageService, "dataTypeUsageService cannot be null");
        checkNotNull(logicalFlowDao, "logicalFlowDao must not be null");
        checkNotNull(logicalFlowStatsDao, "logicalFlowStatsDao cannot be null");
        checkNotNull(logicalFlowIdSelectorFactory, "logicalFlowIdSelectorFactory cannot be null");

        this.appIdSelectorFactory = appIdSelectorFactory;
        this.changeLogService = changeLogService;
        this.dataTypeUsageService = dataTypeUsageService;
        this.dbExecutorPool = dbExecutorPool;
        this.logicalFlowStatsDao = logicalFlowStatsDao;
        this.logicalFlowDao = logicalFlowDao;
        this.logicalFlowIdSelectorFactory = logicalFlowIdSelectorFactory;
    }


    public List<LogicalFlow> findByEntityReference(EntityReference ref) {
        return logicalFlowDao.findByEntityReference(ref);
    }


    public List<LogicalFlow> findBySourceAndTargetEntityReferences(List<Tuple2<EntityReference, EntityReference>> sourceAndTargets) {
        return logicalFlowDao.findBySourcesAndTargets(sourceAndTargets);
    }


    public LogicalFlow getById(long flowId) {
        return logicalFlowDao.getByFlowId(flowId);
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


    public List<LogicalFlow> addFlows(List<AddLogicalFlowCommand> addCmds, String username) {
        addCmds.forEach(cmd -> rejectIfSelfLoop(cmd));

        List<ChangeLog> logEntries = addCmds
                .stream()
                .flatMap(cmd -> {
                    ImmutableChangeLog addedSourceParent = ImmutableChangeLog.builder()
                            .parentReference(cmd.source())
                            .severity(Severity.INFORMATION)
                            .userId(username)
                            .message(String.format(
                                    "Flow %s between: %s and %s",
                                    "added",
                                    cmd.source().name().orElse(Long.toString(cmd.source().id())),
                                    cmd.target().name().orElse(Long.toString(cmd.target().id()))))
                            .childKind(LOGICAL_DATA_FLOW)
                            .operation(Operation.ADD)
                            .build();

                    ImmutableChangeLog addedTargetParent = addedSourceParent.withParentReference(cmd.target());
                    return Stream.of(addedSourceParent, addedTargetParent);
                })
                .collect(Collectors.toList());
        changeLogService.write(logEntries);

        List<LogicalFlow> flowsToAdd = addCmds
                .stream()
                .map(addCmd -> ImmutableLogicalFlow.builder()
                        .source(addCmd.source())
                        .target(addCmd.target())
                        .lastUpdatedAt(nowUtc())
                        .lastUpdatedBy(username)
                        .provenance("waltz")
                        .build())
                .collect(toList());

        List<LogicalFlow> logicalFlows = logicalFlowDao.addFlows(flowsToAdd, username);
        return logicalFlows;
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
     * todo: #WALTZ-1894 for cleanupOrphans task
     *
     * @param flowId
     * @param username
     * @return
     */
    public int removeFlow(Long flowId, String username) {
        LogicalFlow logicalFlow = logicalFlowDao.getByFlowId(flowId);

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
            case CHANGE_INITIATIVE:
            case MEASURABLE:
            case ORG_UNIT:
            case PERSON:
            case SCENARIO:
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


    public boolean restoreFlow(long logicalFlowId, String username) {
        return logicalFlowDao.restoreFlow(logicalFlowId, username);
    }


    public Integer cleanupOrphans() {
        return logicalFlowDao.cleanupOrphans();
    }


    public int cleanupSelfReferencingFlows() {
        return logicalFlowDao.cleanupSelfReferencingFlows();
    }


    public Collection<LogicalFlow> findUpstreamFlowsForEntityReferences(List<EntityReference> references) {
        if (isEmpty(references)) {
            return emptyList();
        }

        return logicalFlowDao.findUpstreamFlowsForEntityReferences(references);
    }
}
