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

package com.khartec.waltz.service.data_flow_decorator;


import com.khartec.waltz.common.Checks;
import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.data_flow_decorator.LogicalFlowDecoratorDao;
import com.khartec.waltz.data.data_type.DataTypeDao;
import com.khartec.waltz.data.data_type.DataTypeIdSelectorFactory;
import com.khartec.waltz.data.logical_flow.LogicalFlowDao;
import com.khartec.waltz.data.logical_flow.LogicalFlowStatsDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.data_flow_decorator.*;
import com.khartec.waltz.model.datatype.DataType;
import com.khartec.waltz.model.logical_flow.ImmutableLogicalFlowMeasures;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.model.rating.AuthoritativenessRating;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.logical_flow.LogicalFlowService;
import com.khartec.waltz.service.usage_info.DataTypeUsageService;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.khartec.waltz.common.Checks.checkNotEmpty;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.CollectionUtilities.isEmpty;
import static com.khartec.waltz.common.CollectionUtilities.map;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtc;
import static com.khartec.waltz.common.ListUtilities.asList;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.common.MapUtilities.indexBy;
import static com.khartec.waltz.common.SetUtilities.asSet;
import static com.khartec.waltz.common.SetUtilities.union;
import static com.khartec.waltz.model.EntityKind.*;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.model.FlowDirection.*;
import static com.khartec.waltz.model.utils.IdUtilities.indexById;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.joining;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Service
public class LogicalFlowDecoratorService {

    private final LogicalFlowDecoratorDao logicalFlowDecoratorDao;
    private final LogicalFlowDecoratorRatingsCalculator ratingsCalculator;
    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();
    private final DataTypeIdSelectorFactory dataTypeIdSelectorFactory = new DataTypeIdSelectorFactory();
    private final DataTypeUsageService dataTypeUsageService;
    private final DataTypeDao dataTypeDao;
    private final LogicalFlowDao logicalFlowDao;
    private final ChangeLogService changeLogService;


    @Autowired
    public LogicalFlowDecoratorService(LogicalFlowDecoratorDao logicalFlowDecoratorDao,
                                       LogicalFlowDecoratorRatingsCalculator ratingsCalculator,
                                       DataTypeUsageService dataTypeUsageService,
                                       DataTypeDao dataTypeDao,
                                       LogicalFlowDao logicalFlowDao,
                                       LogicalFlowService logicalFlowService,
                                       LogicalFlowStatsDao logicalFlowStatsDao,
                                       ChangeLogService changeLogService) {

        checkNotNull(logicalFlowDecoratorDao, "logicalFlowDecoratorDao cannot be null");
        checkNotNull(ratingsCalculator, "ratingsCalculator cannot be null");
        checkNotNull(dataTypeUsageService, "dataTypeUsageService cannot be null");
        checkNotNull(dataTypeDao, "dataTypeDao cannot be null");
        checkNotNull(logicalFlowDao, "logicalFlowDao cannot be null");
        checkNotNull(logicalFlowService, "logicalFlowService cannot be null");
        checkNotNull(logicalFlowStatsDao, "logicalFlowStatsDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.logicalFlowDecoratorDao = logicalFlowDecoratorDao;
        this.ratingsCalculator = ratingsCalculator;
        this.dataTypeUsageService = dataTypeUsageService;
        this.dataTypeDao = dataTypeDao;
        this.logicalFlowDao = logicalFlowDao;
        this.changeLogService = changeLogService;
    }


    // --- FINDERS ---

    public LogicalFlowDecorator getByFlowIdAndDecoratorRef(long flowId, EntityReference decoratorRef) {
        checkNotNull(decoratorRef, "decoratorRef cannot be null");
        return logicalFlowDecoratorDao.getByFlowIdAndDecoratorRef(flowId, decoratorRef);
    }


    public List<LogicalFlowDecorator> findByFlowIds(Collection<Long> flowIds) {
        checkNotNull(flowIds, "flowIds cannot be null");
        return logicalFlowDecoratorDao.findByFlowIds(flowIds);
    }


    public List<LogicalFlowDecorator> findByIdSelectorAndKind(IdSelectionOptions options,
                                                              EntityKind decoratorEntityKind) {
        checkNotNull(options, "options cannot be null");
        checkNotNull(decoratorEntityKind, "decoratorEntityKind cannot be null");

        switch (options.entityReference().kind()) {
            case APPLICATION:
            case APP_GROUP:
            case ORG_UNIT:
            case PERSON:
                Select<Record1<Long>> selector = applicationIdSelectorFactory.apply(options);
                return logicalFlowDecoratorDao.findByEntityIdSelectorAndKind(
                        APPLICATION,
                        selector,
                        decoratorEntityKind);
            case ACTOR:
                long actorId = options.entityReference().id();
                Select<Record1<Long>> actorIdSelector = DSL.select(DSL.val(actorId));
                return logicalFlowDecoratorDao.findByEntityIdSelectorAndKind(
                        ACTOR,
                        actorIdSelector,
                        decoratorEntityKind);
            default:
                throw new UnsupportedOperationException("Cannot find decorators for selector kind: " + options.entityReference().kind());
        }
    }


    /**
     * Find decorators by selector.
     * @param options
     * @return
     */
    public Collection<LogicalFlowDecorator> findBySelector(IdSelectionOptions options) {
        switch (options.entityReference().kind()) {
            case APP_GROUP:
            case CHANGE_INITIATIVE:
            case MEASURABLE:
            case ORG_UNIT:
            case PERSON:
            case SCENARIO:
                return findByAppIdSelector(options);
            case DATA_TYPE:
                return findByDataTypeIdSelector(options);
            default:
                throw new UnsupportedOperationException("Cannot find decorators for selector kind: "+ options.entityReference().kind());
        }
    }


    // --- UPDATERS ---
    @Deprecated
    // Replace with a method that delete for a single flow id
    public int deleteAllDecoratorsForFlowIds(List<Long> flowIds) {
        return logicalFlowDecoratorDao.removeAllDecoratorsForFlowIds(flowIds);
    }


    public int deleteDecorators(long flowId,
                                  Collection<EntityReference> decoratorReferences,
                                  String username) {
        checkNotNull(decoratorReferences, "decoratorReferences cannot be null");
        LogicalFlow flow = logicalFlowDao.getByFlowId(flowId);
        int deleted = logicalFlowDecoratorDao.deleteDecorators(flowId, decoratorReferences);
        dataTypeUsageService.recalculateForApplications(newArrayList(flow.source(), flow.target()));

        changeLogService.writeChangeLogEntries(
                mkRef(LOGICAL_DATA_FLOW, flowId),
                username,
                mkAuditMessage(Operation.REMOVE, decoratorReferences),
                Operation.REMOVE);

        return deleted;
    }


    public int[] addDecorators(long flowId,
                               Set<EntityReference> decoratorReferences,
                               String username) {
        checkNotNull(decoratorReferences, "decoratorReferences cannot be null");
        if (decoratorReferences.isEmpty()) return new int[0];

        LogicalFlow flow = logicalFlowDao.getByFlowId(flowId);

        boolean requiresRating = flow.source().kind() == APPLICATION && flow.target().kind() == APPLICATION;

        Collection<LogicalFlowDecorator> unrated = map(
                decoratorReferences,
                ref -> ImmutableLogicalFlowDecorator.builder()
                        .rating(AuthoritativenessRating.NO_OPINION)
                        .provenance("waltz")
                        .dataFlowId(flowId)
                        .decoratorEntity(ref)
                        .lastUpdatedBy(username)
                        .lastUpdatedAt(nowUtc())
                        .build());

        Collection<LogicalFlowDecorator> decorators = requiresRating
                ? ratingsCalculator.calculate(unrated)
                : unrated;

        int[] added = logicalFlowDecoratorDao.addDecorators(decorators);
        dataTypeUsageService.recalculateForApplications(newArrayList(flow.source(), flow.target()));
        changeLogService.writeChangeLogEntries(
                mkRef(LOGICAL_DATA_FLOW, flowId),
                username,
                mkAuditMessage(Operation.ADD, decoratorReferences),
                Operation.ADD);

        return added;
    }


    public int[] addDecoratorsBatch(List<UpdateDataFlowDecoratorsAction> actions,
                                    String username) {
        checkNotNull(actions, "actions cannot be null");
        checkNotEmpty(username, "username must be provided");

        if (actions.isEmpty()) return new int[0];

        List<LogicalFlowDecorator> unrated = actions
                .stream()
                .flatMap(action -> action.addedDecorators()
                        .stream()
                        .map(ref -> ImmutableLogicalFlowDecorator.builder()
                                .rating(AuthoritativenessRating.NO_OPINION)
                                .provenance("waltz")
                                .dataFlowId(action.flowId())
                                .decoratorEntity(ref)
                                .lastUpdatedBy(username)
                                .lastUpdatedAt(nowUtc())
                                .build())
                )
                .collect(Collectors.toList());

        Collection<LogicalFlowDecorator> decorators = ratingsCalculator.calculate(unrated);
        int[] added = logicalFlowDecoratorDao.addDecorators(decorators);

        List<LogicalFlow> effectedFlows = logicalFlowDao.findActiveByFlowIds(
                map(actions, UpdateDataFlowDecoratorsAction::flowId));

        List<EntityReference> effectedEntities = effectedFlows
                .stream()
                .flatMap(f -> Stream.of(f.source(), f.target()))
                .collect(Collectors.toList());

        dataTypeUsageService.recalculateForApplications(effectedEntities);
        bulkAudit(actions, username, effectedFlows);

        return added;
    }


    public List<DecoratorRatingSummary> summarizeInboundForSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = applicationIdSelectorFactory.apply(options);
        return logicalFlowDecoratorDao.summarizeInboundForSelector(selector);
    }


    public List<DecoratorRatingSummary> summarizeOutboundForSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = applicationIdSelectorFactory.apply(options);
        return logicalFlowDecoratorDao.summarizeOutboundForSelector(selector);
    }


    public List<DecoratorRatingSummary> summarizeForAll() {
        return logicalFlowDecoratorDao.summarizeForAll();
    }


    // --- HELPERS ---

    private Collection<LogicalFlowDecorator> findByDataTypeIdSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = dataTypeIdSelectorFactory.apply(options);
        return logicalFlowDecoratorDao.findByDecoratorEntityIdSelectorAndKind(selector, DATA_TYPE);
    }


    private Collection<LogicalFlowDecorator> findByAppIdSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = applicationIdSelectorFactory.apply(options);
        return logicalFlowDecoratorDao.findByAppIdSelector(selector);
    }


    public Collection<LogicalFlowDecorator> findByFlowIdsAndKind(List<Long> ids, EntityKind decorationKind) {
        checkNotNull(decorationKind, "decorationKind cannot be null");
        if (isEmpty(ids)) {
            return Collections.emptyList();
        }
        return logicalFlowDecoratorDao.findByFlowIds(ids);
    }


    private void bulkAudit(List<UpdateDataFlowDecoratorsAction> actions, String username, List<LogicalFlow> affectedFlows) {
        Map<Long, LogicalFlow> affectedFlowsById = indexById(affectedFlows);

        List<ChangeLog> logEntries = actions
                .stream()
                .flatMap(action -> {
                    LogicalFlow flow = affectedFlowsById.get(action.flowId());

                    List<ChangeLog> addedLogEntries = action.addedDecorators().isEmpty()
                        ? emptyList()
                        : mkChangeLogEntries(mkAuditMessage(Operation.ADD, action.addedDecorators()), flow, username);

                    List<ChangeLog> removedLogEntries = action.removedDecorators().isEmpty()
                        ? emptyList()
                        : mkChangeLogEntries(mkAuditMessage(Operation.REMOVE, action.removedDecorators()), flow, username);

                    return Stream.concat(addedLogEntries.stream(), removedLogEntries.stream());
                })
                .collect(Collectors.toList());
        changeLogService.write(logEntries);
    }


    private String mkAuditMessage(Operation op, Collection<EntityReference> decoratorReferences) {
        String dtNames = dataTypeDao
                .findByIds(map(decoratorReferences, EntityReference::id))
                .stream()
                .map(NameProvider::name)
                .collect(joining(", ", "", ""));

        switch (op) {
            case ADD:
                return "Added data types: " + dtNames;
            case REMOVE:
                return "Removed data types: " + dtNames;
            default:
                return "Modified data types: " + dtNames;
        }
    }

    private List<ChangeLog> mkChangeLogEntries(String message,
                                               LogicalFlow flow,
                                               String username) {
        checkNotEmpty(message, "message cannot be empty");
        checkNotNull(flow, "flow cannot be null");
        checkNotEmpty(username, "username cannot be empty");

        ImmutableChangeLog sourceCL = ImmutableChangeLog.builder()
                .parentReference(flow.source())
                .userId(username)
                .severity(Severity.INFORMATION)
                .message(String.format(
                        "Logical flow between %s and %s: %s",
                        flow.source().name().orElse(Long.toString(flow.source().id())),
                        flow.target().name().orElse(Long.toString(flow.target().id())),
                        message))
                .childKind(EntityKind.LOGICAL_DATA_FLOW)
                .operation(Operation.UPDATE)
                .build();

        ImmutableChangeLog targetCL = sourceCL.withParentReference(flow.target());
        return ListUtilities.newArrayList(sourceCL, targetCL);
    }


    public Set<LogicalFlowDecoratorStat> findFlowsByDatatypeForEntity(IdSelectionOptions selectionOptions) {

        Select<Record1<Long>> appIds = applicationIdSelectorFactory.apply(selectionOptions);

        Map<DataTypeDirectionKey, List<Long>> dataTypeIdAndFlowTypeKeyToLogicalFlowIdsMap = logicalFlowDecoratorDao.logicalFlowIdsByTypeAndDirection(appIds);

        return findFlowIdsByDataTypeForParentsAndChildren(dataTypeIdAndFlowTypeKeyToLogicalFlowIdsMap);
    }


    private Set<LogicalFlowDecoratorStat> findFlowIdsByDataTypeForParentsAndChildren(Map<DataTypeDirectionKey, List<Long>> logicalFlowIdsByDataType) {

        List<DataType> dataTypes = dataTypeDao.findAll();
        Map<Optional<Long>, DataType> dataTypesById = indexBy(IdProvider::id, dataTypes);

        return dataTypes
                .stream()
                .map(dt -> {
                    DataType exactDataType = dataTypesById.get(dt.id());
                    Set<DataTypeDirectionKey> keysForDatatype = filterKeySetByDataTypeId(logicalFlowIdsByDataType, asSet(dt.id().get()));

                    Set<DataType> parentDataTypes = getParents(exactDataType, dataTypesById);
                    Set<DataTypeDirectionKey> keysForParents = filterKeySetByDataTypeId(logicalFlowIdsByDataType, getIds(parentDataTypes));

                    Set<DataType> childDataTypes = getChildren(exactDataType, dataTypesById);
                    Set<DataTypeDirectionKey> keysForChildren = filterKeySetByDataTypeId(logicalFlowIdsByDataType, getIds(childDataTypes));

                    Set<DataTypeDirectionKey> allKeys = union(keysForDatatype, keysForParents, keysForChildren);
                    Set<Long> allFlowIds = getFlowsFromKeys(logicalFlowIdsByDataType, allKeys);

                    Map<FlowDirection, Integer> typeToFlowCountMap = getTypeToFlowCountMap(logicalFlowIdsByDataType, allKeys);

                    return mkLogicalFlowDecoratorStat(dt, typeToFlowCountMap, allFlowIds);

                })
                .filter(r -> r.totalCount() > 0)
                .collect(Collectors.toSet());
    }


    private ImmutableLogicalFlowDecoratorStat mkLogicalFlowDecoratorStat(DataType dt,
                                                                         Map<FlowDirection, Integer> typeToFlowCountMap,
                                                                         Set<Long> allFlowIds) {
        return ImmutableLogicalFlowDecoratorStat
                .builder()
                .dataTypeId(dt.id().get())
                .logicalFlowMeasures(mkLogicalFlowMeasures(typeToFlowCountMap))
                .totalCount(allFlowIds.size())
                .build();
    }


    private ImmutableLogicalFlowMeasures mkLogicalFlowMeasures(Map<FlowDirection, Integer> typeToFlowCountMap) {
        return ImmutableLogicalFlowMeasures
                .builder()
                .inbound(typeToFlowCountMap.get(INBOUND))
                .outbound(typeToFlowCountMap.get(OUTBOUND))
                .intra(typeToFlowCountMap.get(INTRA))
                .build();
    }


    private static Set<Long> getIds(Set<DataType> dataTypes) {
        return SetUtilities.map(dataTypes, dataType -> dataType.id().get());
    }


    private Map<FlowDirection, Integer> getTypeToFlowCountMap(Map<DataTypeDirectionKey, List<Long>> logicalFlowIdsByDataType,
                                                              Set<DataTypeDirectionKey> allKeys) {

        List<FlowDirection> directions = asList(FlowDirection.values());

        return directions
                .stream()
                .map(type -> {

                    Set<Long> setOfFlows = allKeys
                            .stream()
                            .filter(k -> k.flowDirection().equals(type))
                            .flatMap(k -> logicalFlowIdsByDataType.get(k).stream())
                            .collect(Collectors.toSet());

                    return tuple(type, setOfFlows.size());
                })
                .collect(Collectors.toMap(k -> k.v1, v -> v.v2));
    }


    private Set<DataTypeDirectionKey> filterKeySetByDataTypeId(Map<DataTypeDirectionKey, List<Long>> logicalFlowIdsByDataType,
                                                               Set<Long> dataTypeIds) {
        return logicalFlowIdsByDataType
                .keySet()
                .stream()
                .filter(k -> dataTypeIds.contains(k.DatatypeId()))
                .collect(Collectors.toSet());
    }


    private static Set<Long> getFlowsFromKeys(Map<DataTypeDirectionKey, List<Long>> flowIdsByDataTypeId,
                                                 Set<DataTypeDirectionKey> keys) {
        Set<Long> flowIds = new HashSet<>();
        for (DataTypeDirectionKey key : keys) {
            List<Long> flowIdsForType = flowIdsByDataTypeId.getOrDefault(key, emptyList());
            flowIds.addAll(flowIdsForType);
        }
        return flowIds;
    }


    private static Set<DataType> getChildren(DataType dataType, Map<Optional<Long>, DataType> datatypesById) {

        Set<DataType> children = new HashSet<>(emptySet());

        Long parent = dataType.id().orElse(null);

        Set<DataType> descendants = getDescendants(datatypesById, parent);

        children.addAll(descendants);

        descendants
                .forEach(dt -> {
                    Set<DataType> grandchildren = getChildren(dt, datatypesById);
                    children.addAll(grandchildren);
                });

        return children;
    }


    private static Set<DataType> getDescendants(Map<Optional<Long>, DataType> dataTypesById, Long parentId) {
        return dataTypesById
                .values()
                .stream()
                .filter(dt -> dt.parentId().isPresent()
                        && dt.parentId().get().equals(parentId))
                .collect(Collectors.toSet());
    }


    private static Set<DataType> getParents(DataType dataType, Map<Optional<Long>, DataType> dataTypesById) {

        Checks.checkNotNull(dataType, "starting datatype cannot be null");

        Set<DataType> parents = new HashSet<>(emptySet());

        DataType parent = dataTypesById.get(dataType.parentId());

        while (parent != null) {
            parents.add(parent);
            parent = dataTypesById.get(parent.parentId());
        }

        return parents;
    }
}
