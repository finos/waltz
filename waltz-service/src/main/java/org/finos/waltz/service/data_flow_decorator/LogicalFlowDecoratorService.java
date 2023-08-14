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

package org.finos.waltz.service.data_flow_decorator;


import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.service.logical_flow.LogicalFlowService;
import org.finos.waltz.service.usage_info.DataTypeUsageService;
import org.finos.waltz.common.Checks;
import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.data.data_flow_decorator.LogicalFlowDecoratorSummaryDao;
import org.finos.waltz.data.data_type.DataTypeDao;
import org.finos.waltz.data.datatype_decorator.LogicalFlowDecoratorDao;
import org.finos.waltz.data.logical_flow.LogicalFlowDao;
import org.finos.waltz.data.logical_flow.LogicalFlowStatsDao;
import org.finos.waltz.model.*;
import org.finos.waltz.model.changelog.ChangeLog;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.data_flow_decorator.*;
import org.finos.waltz.model.datatype.DataType;
import org.finos.waltz.model.datatype.DataTypeDecorator;
import org.finos.waltz.model.datatype.ImmutableDataTypeDecorator;
import org.finos.waltz.model.logical_flow.ImmutableLogicalFlowMeasures;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.model.rating.AuthoritativenessRatingValue;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.joining;
import static org.finos.waltz.common.Checks.checkNotEmpty;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.CollectionUtilities.map;
import static org.finos.waltz.common.DateTimeUtilities.nowUtc;
import static org.finos.waltz.common.ListUtilities.asList;
import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.SetUtilities.union;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.FlowDirection.*;
import static org.finos.waltz.model.utils.IdUtilities.indexById;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Service
public class LogicalFlowDecoratorService {

    private final LogicalFlowDecoratorSummaryDao logicalFlowDecoratorSummaryDao;
    private final LogicalFlowDecoratorDao logicalFlowDecoratorDao;
    private final LogicalFlowDecoratorRatingsCalculator ratingsCalculator;
    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();
    private final DataTypeUsageService dataTypeUsageService;
    private final DataTypeDao dataTypeDao;
    private final LogicalFlowDao logicalFlowDao;
    private final ChangeLogService changeLogService;


    @Autowired
    public LogicalFlowDecoratorService(LogicalFlowDecoratorSummaryDao logicalFlowDecoratorSummaryDao,
                                       LogicalFlowDecoratorDao logicalFlowDecoratorDao,
                                       LogicalFlowDecoratorRatingsCalculator ratingsCalculator,
                                       DataTypeUsageService dataTypeUsageService,
                                       DataTypeDao dataTypeDao,
                                       LogicalFlowDao logicalFlowDao,
                                       LogicalFlowService logicalFlowService,
                                       LogicalFlowStatsDao logicalFlowStatsDao,
                                       ChangeLogService changeLogService) {

        checkNotNull(logicalFlowDecoratorSummaryDao, "logicalFlowDecoratorDao cannot be null");
        checkNotNull(ratingsCalculator, "ratingsCalculator cannot be null");
        checkNotNull(dataTypeUsageService, "dataTypeUsageService cannot be null");
        checkNotNull(dataTypeDao, "dataTypeDao cannot be null");
        checkNotNull(logicalFlowDao, "logicalFlowDao cannot be null");
        checkNotNull(logicalFlowService, "logicalFlowService cannot be null");
        checkNotNull(logicalFlowStatsDao, "logicalFlowStatsDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.logicalFlowDecoratorSummaryDao = logicalFlowDecoratorSummaryDao;
        this.ratingsCalculator = ratingsCalculator;
        this.dataTypeUsageService = dataTypeUsageService;
        this.dataTypeDao = dataTypeDao;
        this.logicalFlowDao = logicalFlowDao;
        this.changeLogService = changeLogService;
        this.logicalFlowDecoratorDao = logicalFlowDecoratorDao;
    }


    public int[] addDecoratorsBatch(List<UpdateDataFlowDecoratorsAction> actions,
                                    String username) {
        checkNotNull(actions, "actions cannot be null");
        checkNotEmpty(username, "username must be provided");

        if (actions.isEmpty()) return new int[0];

        List<DataTypeDecorator> unrated = actions
                .stream()
                .flatMap(action -> action.addedDecorators()
                        .stream()
                        .map(ref -> ImmutableDataTypeDecorator.builder()
                                .rating(AuthoritativenessRatingValue.NO_OPINION)
                                .provenance("waltz")
                                .entityReference(mkRef(EntityKind.LOGICAL_DATA_FLOW, action.flowId()))
                                .decoratorEntity(ref)
                                .lastUpdatedBy(username)
                                .lastUpdatedAt(nowUtc())
                                .build())
                )
                .collect(Collectors.toList());

        Collection<DataTypeDecorator> decorators = ratingsCalculator.calculate(unrated);
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
        return logicalFlowDecoratorSummaryDao.summarizeInboundForSelector(selector);
    }


    public List<DecoratorRatingSummary> summarizeOutboundForSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = applicationIdSelectorFactory.apply(options);
        return logicalFlowDecoratorSummaryDao.summarizeOutboundForSelector(selector);
    }


    public List<DecoratorRatingSummary> summarizeForAll() {
        return logicalFlowDecoratorSummaryDao.summarizeForAll();
    }


    // --- HELPERS ---
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
                .childId(flow.id())
                .operation(Operation.UPDATE)
                .build();

        ImmutableChangeLog targetCL = sourceCL.withParentReference(flow.target());
        return ListUtilities.newArrayList(sourceCL, targetCL);
    }


    public Set<LogicalFlowDecoratorStat> findFlowsByDatatypeForEntity(IdSelectionOptions selectionOptions) {

        Select<Record1<Long>> appIds = applicationIdSelectorFactory.apply(selectionOptions);

        Map<DataTypeDirectionKey, List<Long>> dataTypeIdAndFlowTypeKeyToLogicalFlowIdsMap =
                logicalFlowDecoratorSummaryDao.logicalFlowIdsByTypeAndDirection(appIds);

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
