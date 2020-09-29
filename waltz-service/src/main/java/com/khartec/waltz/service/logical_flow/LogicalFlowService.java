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

package com.khartec.waltz.service.logical_flow;

import com.khartec.waltz.common.FunctionUtilities;
import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.data.DBExecutorPoolInterface;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.data_type.DataTypeIdSelectorFactory;
import com.khartec.waltz.data.logical_flow.LogicalFlowDao;
import com.khartec.waltz.data.logical_flow.LogicalFlowIdSelectorFactory;
import com.khartec.waltz.data.logical_flow.LogicalFlowStatsDao;
import com.khartec.waltz.data.datatype_decorator.LogicalFlowDecoratorDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.datatype.ImmutableDataTypeDecorator;
import com.khartec.waltz.model.logical_flow.*;
import com.khartec.waltz.model.rating.AuthoritativenessRating;
import com.khartec.waltz.model.tally.TallyPack;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.data_type.DataTypeService;
import com.khartec.waltz.service.usage_info.DataTypeUsageService;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.CollectionUtilities.isEmpty;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtc;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.model.EntityKind.DATA_TYPE;
import static com.khartec.waltz.model.EntityKind.LOGICAL_DATA_FLOW;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.jooq.lambda.tuple.Tuple.tuple;


@Service
public class LogicalFlowService {

    private final ChangeLogService changeLogService;
    private final DataTypeService dataTypeService;
    private final DataTypeUsageService dataTypeUsageService;
    private final DBExecutorPoolInterface dbExecutorPool;
    private final LogicalFlowDao logicalFlowDao;
    private final LogicalFlowStatsDao logicalFlowStatsDao;
    private final LogicalFlowDecoratorDao logicalFlowDecoratorDao;

    private final ApplicationIdSelectorFactory appIdSelectorFactory = new ApplicationIdSelectorFactory();
    private final LogicalFlowIdSelectorFactory logicalFlowIdSelectorFactory = new LogicalFlowIdSelectorFactory();
    private final DataTypeIdSelectorFactory dataTypeIdSelectorFactory = new DataTypeIdSelectorFactory();


    @Autowired
    public LogicalFlowService(ChangeLogService changeLogService,
                              DataTypeService dataTypeService,
                              DataTypeUsageService dataTypeUsageService,
                              DBExecutorPoolInterface dbExecutorPool,
                              LogicalFlowDao logicalFlowDao,
                              LogicalFlowStatsDao logicalFlowStatsDao,
                              LogicalFlowDecoratorDao logicalFlowDecoratorDao) {
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(dbExecutorPool, "dbExecutorPool cannot be null");
        checkNotNull(dataTypeService, "dataTypeService cannot be null");
        checkNotNull(dataTypeUsageService, "dataTypeUsageService cannot be null");
        checkNotNull(logicalFlowDao, "logicalFlowDao must not be null");
        checkNotNull(logicalFlowDecoratorDao, "logicalFlowDataTypeDecoratorDao cannot be null");
        checkNotNull(logicalFlowStatsDao, "logicalFlowStatsDao cannot be null");

        this.changeLogService = changeLogService;
        this.dataTypeService = dataTypeService;
        this.dataTypeUsageService = dataTypeUsageService;
        this.dbExecutorPool = dbExecutorPool;
        this.logicalFlowDao = logicalFlowDao;
        this.logicalFlowStatsDao = logicalFlowStatsDao;
        this.logicalFlowDecoratorDao = logicalFlowDecoratorDao;
    }


    public List<LogicalFlow> findByEntityReference(EntityReference ref) {
        return logicalFlowDao.findByEntityReference(ref);
    }


    public List<LogicalFlow> findBySourceAndTargetEntityReferences(List<Tuple2<EntityReference, EntityReference>> sourceAndTargets) {
        return logicalFlowDao.findBySourcesAndTargets(sourceAndTargets);
    }


    public Collection<LogicalFlow> findActiveByFlowIds(Collection<Long> ids) {
        return logicalFlowDao.findActiveByFlowIds(ids);

    }


    public Collection<LogicalFlow> findAllByFlowIds(Collection<Long> ids) {
        return logicalFlowDao.findAllByFlowIds(ids);
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
     * @param options given to logical flow selector factory to determine in-scope flows
     * @return a list of logical flows matching the given options
     */
    public List<LogicalFlow> findBySelector(IdSelectionOptions options) {
        return logicalFlowDao.findBySelector(logicalFlowIdSelectorFactory.apply(options));
    }


    /**
     * Creates a logical flow and creates a default, 'UNKNOWN' data type decoration
     * if possible.
     *
     * If the flow already exists, but is inactive, the flow will be re-activated.
     *
     * @param addCmd Command object containing flow details
     * @param username who is creating the flow
     * @return the newly created logical flow
     * @throws IllegalArgumentException if a flow already exists
     */
    public LogicalFlow addFlow(AddLogicalFlowCommand addCmd, String username) {
        rejectIfSelfLoop(addCmd);

        LocalDateTime now = nowUtc();
        LogicalFlow flowToAdd = ImmutableLogicalFlow.builder()
                .source(addCmd.source())
                .target(addCmd.target())
                .lastUpdatedAt(now)
                .lastUpdatedBy(username)
                .created(UserTimestamp.mkForUser(username, now))
                .build();

        LogicalFlow logicalFlow = logicalFlowDao.addFlow(flowToAdd);
        attemptToAddUnknownDecoration(logicalFlow, username);

        changeLogService.writeChangeLogEntries(logicalFlow, username, "Added", Operation.ADD);

        return logicalFlow;
    }



    public List<LogicalFlow> addFlows(List<AddLogicalFlowCommand> addCmds, String username) {
        addCmds.forEach(this::rejectIfSelfLoop);

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

        LocalDateTime now = nowUtc();
        List<LogicalFlow> flowsToAdd = addCmds
                .stream()
                .map(addCmd -> ImmutableLogicalFlow.builder()
                        .source(addCmd.source())
                        .target(addCmd.target())
                        .lastUpdatedAt(now)
                        .lastUpdatedBy(username)
                        .created(UserTimestamp.mkForUser(username, now))
                        .provenance("waltz")
                        .build())
                .collect(toList());

        return logicalFlowDao.addFlows(flowsToAdd, username);
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
     * @param flowId  identifier of flow to be removed
     * @param username  who initiated the removal
     * @return number of flows removed
     */
    public int removeFlow(Long flowId, String username) {
        LogicalFlow logicalFlow = logicalFlowDao.getByFlowId(flowId);

        int deleted = logicalFlowDao.removeFlow(flowId, username);

        Set<EntityReference> affectedEntityRefs = SetUtilities.fromArray(logicalFlow.source(), logicalFlow.target());

        dataTypeUsageService.recalculateForApplications(affectedEntityRefs);

        changeLogService.writeChangeLogEntries(logicalFlow, username,
                "Removed : datatypes [" + getAssociatedDatatypeNamesAsCsv(flowId) + "]",
                Operation.REMOVE);

        return deleted;
    }


    /**
     * Calculate Stats by selector
     * @param options determines which flows are in-scope for this calculation
     * @return statistics about the in-scope flows
     */
    public LogicalFlowStatistics calculateStats(IdSelectionOptions options) {
        switch (options.entityReference().kind()) {
            case APP_GROUP:
            case CHANGE_INITIATIVE:
            case MEASURABLE:
            case ORG_UNIT:
            case PERSON:
            case SCENARIO:
            case DATA_TYPE:
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


    public boolean restoreFlow(long logicalFlowId, String username) {
        boolean result = logicalFlowDao.restoreFlow(logicalFlowId, username);
        if (result) {
            changeLogService.writeChangeLogEntries(mkRef(LOGICAL_DATA_FLOW, logicalFlowId), username, "Restored", Operation.ADD);
        }
        return result;
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



    private void attemptToAddUnknownDecoration(LogicalFlow logicalFlow, String username) {
        dataTypeService
                .getUnknownDataType()
                .flatMap(IdProvider::id)
                .flatMap(unknownDataTypeId -> logicalFlow.id()
                        .map(flowId -> tuple(
                                mkRef(DATA_TYPE, unknownDataTypeId),
                                flowId)))
                .map(t -> ImmutableDataTypeDecorator
                        .builder()
                        .decoratorEntity(t.v1)
                        .entityReference(mkRef(DATA_TYPE, t.v2))
                        .lastUpdatedBy(username)
                        .rating(AuthoritativenessRating.DISCOURAGED)
                        .build())
                .map(decoration -> logicalFlowDecoratorDao.addDecorators(newArrayList(decoration)));
    }

    private String getAssociatedDatatypeNamesAsCsv(Long flowId) {
        IdSelectionOptions idSelectionOptions = IdSelectionOptions.mkOpts(
                mkRef(LOGICAL_DATA_FLOW, flowId),
                HierarchyQueryScope.EXACT);

        return dataTypeService.findByIdSelector(dataTypeIdSelectorFactory.apply(idSelectionOptions))
                .stream()
                .map(EntityReference::name)
                .map(Optional::get)
                .collect(Collectors.joining(", "));
    }
}
