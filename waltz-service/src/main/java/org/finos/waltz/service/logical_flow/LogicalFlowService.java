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

package org.finos.waltz.service.logical_flow;

import org.finos.waltz.common.FunctionUtilities;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.DBExecutorPoolInterface;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.data.data_type.DataTypeIdSelectorFactory;
import org.finos.waltz.data.datatype_decorator.LogicalFlowDecoratorDao;
import org.finos.waltz.data.logical_flow.LogicalFlowDao;
import org.finos.waltz.data.logical_flow.LogicalFlowIdSelectorFactory;
import org.finos.waltz.data.logical_flow.LogicalFlowStatsDao;
import org.finos.waltz.model.*;
import org.finos.waltz.model.changelog.ChangeLog;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.datatype.DataType;
import org.finos.waltz.model.datatype.ImmutableDataTypeDecorator;
import org.finos.waltz.model.logical_flow.*;
import org.finos.waltz.model.permission_group.Permission;
import org.finos.waltz.model.rating.AuthoritativenessRatingValue;
import org.finos.waltz.model.tally.TallyPack;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.service.data_type.DataTypeService;
import org.finos.waltz.service.involvement.InvolvementService;
import org.finos.waltz.service.permission.PermissionGroupService;
import org.finos.waltz.service.usage_info.DataTypeUsageService;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.CollectionUtilities.isEmpty;
import static org.finos.waltz.common.DateTimeUtilities.nowUtc;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.common.SetUtilities.fromCollection;
import static org.finos.waltz.common.SetUtilities.union;
import static org.finos.waltz.model.EntityKind.DATA_TYPE;
import static org.finos.waltz.model.EntityKind.LOGICAL_DATA_FLOW;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.jooq.lambda.tuple.Tuple.tuple;


@Service
public class LogicalFlowService {

    private static final Logger LOG = LoggerFactory.getLogger(LogicalFlowService.class);

    private final ChangeLogService changeLogService;
    private final DataTypeService dataTypeService;
    private final DataTypeUsageService dataTypeUsageService;
    private final DBExecutorPoolInterface dbExecutorPool;
    private final LogicalFlowDao logicalFlowDao;
    private final LogicalFlowStatsDao logicalFlowStatsDao;
    private final LogicalFlowDecoratorDao logicalFlowDecoratorDao;
    private final InvolvementService involvementService;
    private final PermissionGroupService permissionGroupService;

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
                              LogicalFlowDecoratorDao logicalFlowDecoratorDao,
                              InvolvementService involvementService,
                              PermissionGroupService permissionGroupService) {

        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(dbExecutorPool, "dbExecutorPool cannot be null");
        checkNotNull(dataTypeService, "dataTypeService cannot be null");
        checkNotNull(dataTypeUsageService, "dataTypeUsageService cannot be null");
        checkNotNull(logicalFlowDao, "logicalFlowDao must not be null");
        checkNotNull(logicalFlowDecoratorDao, "logicalFlowDataTypeDecoratorDao cannot be null");
        checkNotNull(logicalFlowStatsDao, "logicalFlowStatsDao cannot be null");
        checkNotNull(involvementService, "involvementService cannot be null");
        checkNotNull(permissionGroupService, "permissionGroupService cannot be null");

        this.changeLogService = changeLogService;
        this.dataTypeService = dataTypeService;
        this.dataTypeUsageService = dataTypeUsageService;
        this.dbExecutorPool = dbExecutorPool;
        this.logicalFlowDao = logicalFlowDao;
        this.logicalFlowStatsDao = logicalFlowStatsDao;
        this.logicalFlowDecoratorDao = logicalFlowDecoratorDao;
        this.involvementService = involvementService;
        this.permissionGroupService = permissionGroupService;
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


    @Deprecated
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

        Set<AddLogicalFlowCommand> toAdd = fromCollection(addCmds);

        List<ChangeLog> logEntries = toAdd
                .stream()
                .flatMap(cmd -> {
                    ImmutableChangeLog addedSourceParent = ImmutableChangeLog.builder()
                            .parentReference(cmd.source())
                            .severity(Severity.INFORMATION)
                            .userId(username)
                            .message(format(
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
        List<LogicalFlow> flowsToAdd = toAdd
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

        if(logicalFlow == null){
            LOG.warn("Logical flow cannot be found, no flows will be updated");
            throw new IllegalArgumentException(format("Cannot find flow with id: %d, no logical flow removed", flowId));
        } else {
            int deleted = logicalFlowDao.removeFlow(flowId, username);

            Set<EntityReference> affectedEntityRefs = SetUtilities.fromArray(logicalFlow.source(), logicalFlow.target());

            dataTypeUsageService.recalculateForApplications(affectedEntityRefs);

            changeLogService.writeChangeLogEntries(logicalFlow, username,
                    "Removed : datatypes [" + getAssociatedDatatypeNamesAsCsv(flowId) + "]",
                    Operation.REMOVE);

            return deleted;
        }
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


    /**
     *
     * @param ref  app or actor ref
     * @param startingDataTypeId  optional id of the data type to use (i.e. restrict to flows below that data type)
     * @return
     */
    public LogicalFlowGraphSummary getFlowInfoByDirection(EntityReference ref, Long startingDataTypeId) {
        ImmutableLogicalFlowGraphSummary.Builder result = ImmutableLogicalFlowGraphSummary
                .builder()
                .flowInfoByDirection(logicalFlowStatsDao.getFlowInfoByDirection(ref, startingDataTypeId));

        if (startingDataTypeId != null) {
            DataType startingDataType = dataTypeService.getDataTypeById(startingDataTypeId);
            result.startingDataType(startingDataType);
            result.parentDataType(startingDataType
                    .parentId()
                    .map(dataTypeService::getDataTypeById)
                    .orElse(null));
        }

        return result.build();
    }


    public Set<Operation> findFlowPermissionsForParentEntity(EntityReference entityReference,
                                                             String username) {

        Set<Long> invsForUser = involvementService.findExistingInvolvementKindIdsForUser(entityReference, username);

        Set<Operation> operationsForEntityAssessment = permissionGroupService
                .findPermissionsForParentReference(entityReference, username)
                .stream()
                .filter(p -> p.subjectKind().equals(EntityKind.LOGICAL_DATA_FLOW)
                        && p.parentKind().equals(entityReference.kind()))
                .filter(p -> p.requiredInvolvementsResult().isAllowed(invsForUser))
                .map(Permission::operation)
                .collect(Collectors.toSet());

        return logicalFlowDao.calculateAmendedFlowOperations(
                operationsForEntityAssessment,
                entityReference,
                username);
    }


    public Set<Operation> findPermissionsForFlow(Long flowId,
                                                 String username) {

        LogicalFlow flow = logicalFlowDao.getByFlowId(flowId);

        if (flow.isReadOnly()) {
            return emptySet();
        } else {
            Set<Operation> sourcePermissions = findFlowPermissionsForParentEntity(flow.source(), username);
            Set<Operation> targetPermissions = findFlowPermissionsForParentEntity(flow.target(), username);
            return union(sourcePermissions, targetPermissions);
        }
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
                        .rating(AuthoritativenessRatingValue.DISCOURAGED)
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
