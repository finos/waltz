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

package org.finos.waltz.service.flow_diagram;

import org.finos.waltz.data.end_user_app.EndUserAppDao;
import org.finos.waltz.model.enduserapp.EndUserApplication;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.service.data_type.DataTypeService;
import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.common.RandomUtilities;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.actor.ActorDao;
import org.finos.waltz.data.application.ApplicationDao;
import org.finos.waltz.data.change_initiative.ChangeInitiativeDao;
import org.finos.waltz.data.flow_diagram.FlowDiagramAnnotationDao;
import org.finos.waltz.data.flow_diagram.FlowDiagramDao;
import org.finos.waltz.data.flow_diagram.FlowDiagramEntityDao;
import org.finos.waltz.data.flow_diagram.FlowDiagramIdSelectorFactory;
import org.finos.waltz.data.logical_flow.LogicalFlowDao;
import org.finos.waltz.data.logical_flow.LogicalFlowIdSelectorFactory;
import org.finos.waltz.data.measurable.MeasurableDao;
import org.finos.waltz.data.physical_flow.PhysicalFlowDao;
import org.finos.waltz.data.physical_specification.PhysicalSpecificationDao;
import org.finos.waltz.model.*;
import org.finos.waltz.model.actor.Actor;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.change_initiative.ChangeInitiative;
import org.finos.waltz.model.changelog.ChangeLog;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.datatype.DataType;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.finos.waltz.model.flow_diagram.*;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.model.physical_flow.PhysicalFlow;
import org.finos.waltz.model.physical_specification.PhysicalSpecification;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.exception.InvalidResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.DateTimeUtilities.nowUtc;
import static org.finos.waltz.common.ListUtilities.map;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.common.StringUtilities.isEmpty;
import static org.finos.waltz.model.EntityKind.FLOW_DIAGRAM;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;


@Service
public class FlowDiagramService {

    private final ChangeLogService changeLogService;
    private final DataTypeService dataTypeService;
    private final FlowDiagramDao flowDiagramDao;
    private final FlowDiagramEntityDao flowDiagramEntityDao;
    private final FlowDiagramAnnotationDao flowDiagramAnnotationDao;
    private final FlowDiagramOverlayGroupService flowDiagramOverlayGroupService;
    private final ApplicationDao applicationDao;
    private final LogicalFlowDao logicalFlowDao;
    private final PhysicalFlowDao physicalFlowDao;
    private final PhysicalSpecificationDao physicalSpecificationDao;
    private final ActorDao actorDao;
    private final EndUserAppDao endUserAppDao;
    private final Random rnd = RandomUtilities.getRandom();
    private final FlowDiagramIdSelectorFactory flowDiagramIdSelectorFactory = new FlowDiagramIdSelectorFactory();
    private final LogicalFlowIdSelectorFactory logicalFlowIdSelectorFactory = new LogicalFlowIdSelectorFactory();
    private final MeasurableDao measurableDao;
    private final ChangeInitiativeDao changeInitiativeDao;


    @Autowired
    public FlowDiagramService(ChangeLogService changeLogService,
                              DataTypeService dataTypeService,
                              FlowDiagramDao flowDiagramDao,
                              FlowDiagramEntityDao flowDiagramEntityDao,
                              FlowDiagramAnnotationDao flowDiagramAnnotationDao,
                              FlowDiagramOverlayGroupService flowDiagramOverlayGroupService,
                              ApplicationDao applicationDao,
                              LogicalFlowDao logicalFlowDao,
                              PhysicalFlowDao physicalFlowDao,
                              PhysicalSpecificationDao physicalSpecificationDao,
                              ActorDao actorDao,
                              EndUserAppDao endUserAppDao,
                              MeasurableDao measurableDao,
                              ChangeInitiativeDao changeInitiativeDao) {
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(dataTypeService, "        checkNotNull(changeInitiativeDao, \"changeInitiativeDao cannot be null\");\n cannot be null");
        checkNotNull(flowDiagramDao, "flowDiagramDao cannot be null");
        checkNotNull(flowDiagramEntityDao, "flowDiagramEntityDao cannot be null");
        checkNotNull(flowDiagramAnnotationDao, "flowDiagramAnnotationDao cannot be null");
        checkNotNull(flowDiagramOverlayGroupService, "flowDiagramOverlayGroupService cannot be null");
        checkNotNull(applicationDao, "applicationDao cannot be null");
        checkNotNull(logicalFlowDao, "logicalFlowDao cannot be null");
        checkNotNull(physicalFlowDao, "physicalFlowDao cannot be null");
        checkNotNull(physicalSpecificationDao, "physicalSpecificationDao cannot be null");
        checkNotNull(actorDao, "actorDao cannot be null");
        checkNotNull(endUserAppDao, "endUserAppDao cannot be null");
        checkNotNull(measurableDao, "measurableDao cannot be null");
        checkNotNull(changeInitiativeDao, "changeInitiativeDao cannot be null");

        this.changeLogService = changeLogService;
        this.dataTypeService = dataTypeService;

        this.flowDiagramDao = flowDiagramDao;
        this.flowDiagramEntityDao = flowDiagramEntityDao;
        this.flowDiagramAnnotationDao = flowDiagramAnnotationDao;
        this.flowDiagramOverlayGroupService = flowDiagramOverlayGroupService;
        this.applicationDao = applicationDao;
        this.logicalFlowDao = logicalFlowDao;
        this.physicalFlowDao = physicalFlowDao;
        this.physicalSpecificationDao = physicalSpecificationDao;
        this.actorDao = actorDao;
        this.endUserAppDao = endUserAppDao;
        this.measurableDao = measurableDao;
        this.changeInitiativeDao = changeInitiativeDao;
    }


    public FlowDiagram getById(long diagramId) {
        return flowDiagramDao.getById(diagramId);
    }


    public Long cloneDiagram(long diagramId, String newName, String userId) {
        Long clonedDiagramId = flowDiagramDao.clone(diagramId, newName, userId);
        flowDiagramEntityDao.clone(diagramId, clonedDiagramId);
        flowDiagramAnnotationDao.clone(diagramId, clonedDiagramId);
        return clonedDiagramId;
    }

    public List<FlowDiagram> findByEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return flowDiagramDao.findByEntityReference(ref);
    }


    public List<FlowDiagram> findForSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = flowDiagramIdSelectorFactory.apply(options);
        return flowDiagramDao.findForSelector(selector);
    }


    public long save(SaveDiagramCommand command, String username) {
        checkNotNull(command, "command cannot be null");
        checkNotNull(username, "username cannot be null");

        FlowDiagram diagram = ImmutableFlowDiagram.builder()
                .id(command.diagramId())
                .name(command.name())
                .description(command.description())
                .layoutData(command.layoutData())
                .lastUpdatedBy(username)
                .lastUpdatedAt(nowUtc())
                .build();

        Long diagramId;

        List<EntityReference> existingEntities = newArrayList();

        if (diagram.id().isPresent()) {
            // update
            diagramId = diagram.id().get();

            if(!flowDiagramDao.update(diagram)) {
                throw new InvalidResultException("Could not update diagram with Id: " + diagramId);
            }

            existingEntities = map(flowDiagramEntityDao.findForDiagram(diagramId), fde -> fde.entityReference());
            auditChange("updated", mkRef(FLOW_DIAGRAM, diagramId), username, Operation.UPDATE);
            flowDiagramEntityDao.deleteForDiagram(diagramId);
            flowDiagramAnnotationDao.deleteForDiagram(diagramId);
        } else {
            // create
            diagramId = flowDiagramDao.create(diagram);
            auditChange("added", mkRef(FLOW_DIAGRAM, diagramId), username, Operation.ADD);
        }

        createEntities(diagramId, command.entities());
        createAnnotations(diagramId, command.annotations());

        flowDiagramOverlayGroupService.updateOverlaysForDiagram(diagramId, command.overlays(), username);

        List<EntityReference> newEntities = map(command.entities(), FlowDiagramEntity::entityReference);
        auditEntityChange(mkRef(FLOW_DIAGRAM, diagramId), existingEntities, newEntities, username);
        return diagramId;
    }


    public boolean updateName(long id, UpdateNameCommand command, String username) {
        checkNotNull(command, "command cannot be null");
        checkNotNull(username, "username cannot be null");

        return flowDiagramDao.updateName(id, command.newName());
    }

    public boolean updateDescription(long id, UpdateDescriptionCommand command, String username) {
        checkNotNull(command, "command cannot be null");
        checkNotNull(username, "username cannot be null");

        return flowDiagramDao.updateDescription(id, command.newDescription());
    }


    private int[] createEntities(long diagramId,
                                 List<FlowDiagramEntity> entities) {
        entities = entities
                .stream()
                .map(e -> ImmutableFlowDiagramEntity
                        .copyOf(e)
                        .withDiagramId(diagramId))
                .collect(toList());
        return flowDiagramEntityDao.createEntities(entities);
    }


    private int[] createAnnotations(long diagramId,
                                    List<FlowDiagramAnnotation> annotations) {
        annotations = annotations
                .stream()
                .map(a -> ImmutableFlowDiagramAnnotation
                        .copyOf(a)
                        .withDiagramId(diagramId))
                .collect(toList());

        return flowDiagramAnnotationDao.createAnnotations(annotations);
    }


    public boolean deleteById(long id, String username) {
        auditChange("removed", mkRef(FLOW_DIAGRAM, id), username, Operation.REMOVE);
        return flowDiagramDao.deleteById(id, username);
    }


    private void auditChange(String verb, EntityReference diagramRef, String username, Operation operation) {
        ImmutableChangeLog logEntry = ImmutableChangeLog.builder()
                .parentReference(diagramRef)
                .severity(Severity.INFORMATION)
                .userId(username)
                .message(format(
                        "Diagram %s",
                        verb))
                .childKind(diagramRef.kind())
                .operation(operation)
                .build();

        changeLogService.write(logEntry);
    }


    private void auditEntityChange(EntityReference diagramRef,
                                   List<EntityReference> previousEntities,
                                   List<EntityReference> newEntities,
                                   String username) {

        // get added entities
        List<EntityReference> addedEntities = new ArrayList<>(newEntities);
        addedEntities.removeAll(previousEntities);

        List<ChangeLog> addLogEntries = addedEntities.stream()
                .map(ref -> ImmutableChangeLog.builder()
                        .parentReference(diagramRef)
                        .severity(Severity.INFORMATION)
                        .userId(username)
                        .message(format(
                                "Added entity to diagram: %s",
                                ref))
                        .childKind(ref.kind())
                        .childId(ref.id())
                        .operation(Operation.ADD)
                        .build())
                .collect(toList());

        // get removed entities
        List<EntityReference> removedEntities = new ArrayList<>(previousEntities);
        removedEntities.removeAll(newEntities);

        List<ChangeLog> removeLogEntries = removedEntities.stream()
                .map(ref -> ImmutableChangeLog.builder()
                        .parentReference(diagramRef)
                        .severity(Severity.INFORMATION)
                        .userId(username)
                        .message(format(
                                "Removed entity from diagram: %s",
                                ref))
                        .childKind(ref.kind())
                        .childId(ref.id())
                        .operation(Operation.REMOVE)
                        .build())
                .collect(toList());

        List<ChangeLog> allLogEntries = new ArrayList<>(addLogEntries);
        allLogEntries.addAll(removeLogEntries);
        changeLogService.write(allLogEntries);
    }


    public Long makeNewDiagramForEntity(EntityReference ref, String userId, String title) {
        switch (ref.kind()) {
            case APPLICATION:
                return makeForApplication(ref, userId, title);
            case ACTOR:
                return makeForActor(ref, userId, title);
            case END_USER_APPLICATION:
                return makeForEndUserApplication(ref, userId, title);
            case LOGICAL_DATA_FLOW:
                return makeForLogicalFlow(ref, userId, title);
            case PHYSICAL_FLOW:
                return makeForPhysicalFlow(ref, userId, title);
            case PHYSICAL_SPECIFICATION:
                return makeForPhysicalSpecification(ref, userId, title);
            case MEASURABLE:
                return makeForMeasurable(ref, userId, title);
            case CHANGE_INITIATIVE:
                return makeForChangeInitiative(ref, userId, title);
            case DATA_TYPE:
                return makeForDataType(ref, userId, title);
            default:
                throw new UnsupportedOperationException("Cannot make diagram for entity: "+ref);
        }
    }


    private Long makeForEndUserApplication(EntityReference ref,
                                           String userId,
                                           String providedTitle) {

        EndUserApplication endUserApp = endUserAppDao.getById(ref.id());

        String title = isEmpty(providedTitle)
                ? endUserApp.name() + " flows"
                : providedTitle;

        ArrayList<FlowDiagramEntity> entities = newArrayList(mkDiagramEntity(endUserApp));
        ArrayList<FlowDiagramAnnotation> annotations = newArrayList(mkDiagramAnnotation(endUserApp.entityReference(), title));

        return mkNewFlowDiagram(title, userId, entities, annotations);

    }


    private Long makeForLogicalFlow(EntityReference ref, String userId, String providedTitle) {
        LogicalFlow logicalFlow = logicalFlowDao.getByFlowId(ref.id());

        String title = isEmpty(providedTitle)
                ? format("%s -> %s flow diagram", logicalFlow.source().name(), logicalFlow.target().name())
                : providedTitle;

        ArrayList<FlowDiagramEntity> entities = newArrayList(
                mkDiagramEntity(logicalFlow),
                mkDiagramEntity(logicalFlow.source()),
                mkDiagramEntity(logicalFlow.target()));

        return mkNewFlowDiagram(title, userId, entities, emptyList());
    }


    private Long makeForDataType(EntityReference ref, String userId, String providedTitle) {
        DataType dataType = dataTypeService.getDataTypeById(ref.id());

        String title = isEmpty(providedTitle)
                ? dataType.name() + " flows"
                : providedTitle;

        return mkNewFlowDiagram(title, userId, newArrayList(mkDiagramEntity(dataType)), emptyList());
    }


    private Long makeForChangeInitiative(EntityReference ref, String userId, String providedTitle) {
        ChangeInitiative changeInitiative = changeInitiativeDao.getById(ref.id());

        String title = isEmpty(providedTitle)
                ? changeInitiative.name() + " flows"
                : providedTitle;

        return mkNewFlowDiagram(title, userId, newArrayList(mkDiagramEntity(changeInitiative)), emptyList());
    }


    private Long makeForMeasurable(EntityReference ref, String userId, String providedTitle) {
        Measurable measurable = measurableDao.getById(ref.id());

        String title = isEmpty(providedTitle)
                ? measurable.name() + " flows"
                : providedTitle;

        return mkNewFlowDiagram(title, userId, newArrayList(mkDiagramEntity(measurable)), emptyList());
    }


    private Long makeForPhysicalSpecification(EntityReference ref, String userId, String providedTitle) {
        PhysicalSpecification spec = physicalSpecificationDao.getById(ref.id());

        Select<Record1<Long>> logicalFlowSelector = logicalFlowIdSelectorFactory.apply(mkOpts(ref, HierarchyQueryScope.EXACT));
        List<LogicalFlow> logicalFlows = logicalFlowDao.findBySelector(logicalFlowSelector);
        List<PhysicalFlow> physicalFlows = physicalFlowDao.findBySpecificationId(ref.id());
        List<EntityReference> nodes = logicalFlows.stream().flatMap(f -> Stream.of(f.source(), f.target())).distinct().collect(toList());

        List<FlowDiagramEntity> entities = ListUtilities.concat(
                map(logicalFlows, d -> mkDiagramEntity(d)),
                map(physicalFlows, d -> mkDiagramEntity(d)),
                newArrayList(mkDiagramEntity(spec)),
                map(nodes, d -> mkDiagramEntity(d)));

        String title = isEmpty(providedTitle)
                ? spec.name() + " flows"
                : providedTitle;

        return mkNewFlowDiagram(title, userId, entities, emptyList());
    }


    private Long makeForPhysicalFlow(EntityReference ref, String userId, String providedTitle) {
        PhysicalFlow physFlow = physicalFlowDao.getById(ref.id());
        LogicalFlow logicalFlow = logicalFlowDao.getByFlowId(physFlow.logicalFlowId());
        PhysicalSpecification spec = physicalSpecificationDao.getById(physFlow.specificationId());

        String title = isEmpty(providedTitle)
                ? spec.name() + " flows"
                : providedTitle;

        ArrayList<FlowDiagramEntity> entities = newArrayList(
                mkDiagramEntity(logicalFlow),
                mkDiagramEntity(physFlow),
                mkDiagramEntity(spec),
                mkDiagramEntity(logicalFlow.source()),
                mkDiagramEntity(logicalFlow.target()));


        return mkNewFlowDiagram(title, userId, entities, emptyList());
    }


    private Long makeForActor(EntityReference ref, String userId, String providedTitle) {
        Actor actor = actorDao.getById(ref.id());

        String title = isEmpty(providedTitle)
                ? actor.name() + " flows"
                : providedTitle;

        ArrayList<FlowDiagramEntity> entities = newArrayList(mkDiagramEntity(actor));
        ArrayList<FlowDiagramAnnotation> annotations = newArrayList(mkDiagramAnnotation(actor.entityReference(), title));

        return mkNewFlowDiagram(title, userId, entities, annotations);
    }


    private Long makeForApplication(EntityReference ref, String userId, String providedTitle) {
        Application app = applicationDao.getById(ref.id());

        String title = isEmpty(providedTitle)
                ? app.name() + " flows"
                : providedTitle;

        ArrayList<FlowDiagramEntity> entities = newArrayList(mkDiagramEntity(app));
        ArrayList<FlowDiagramAnnotation> annotations = newArrayList(mkDiagramAnnotation(app.entityReference(), title));


        return mkNewFlowDiagram(title, userId, entities, annotations);
    }


    private String mkLayoutData(List<FlowDiagramEntity> entities,
                                List<FlowDiagramAnnotation> annotations) {
        String transform = "\t\"diagramTransform\":\"translate(0,0) scale(1)\"";
        String entityPositionTemplate = "\"%s/%s\": { \"x\": %d, \"y\": %d }";
        Set<EntityKind> positionableEntityKinds = SetUtilities.fromArray(
                EntityKind.APPLICATION,
                EntityKind.ACTOR,
                EntityKind.END_USER_APPLICATION);

        Stream<String> entityPositions = entities
                .stream()
                .filter(e -> positionableEntityKinds.contains(e.entityReference().kind()))
                .map(e -> format(
                        entityPositionTemplate,
                        e.entityReference().kind().name(),
                        e.entityReference().id(),
                        300 + (rnd.nextInt(600) - 300),
                        200 + (rnd.nextInt(400) - 200)));

        Stream<String> annotationPositions = annotations.stream()
                .map(a -> format(
                        entityPositionTemplate,
                        "ANNOTATION",
                        a.annotationId(),
                        30,
                        -30));

        String positions = Stream
                .concat(entityPositions, annotationPositions)
                .collect(joining(",", "{", "}"));

        return "{ \"positions\": " + positions + ", " + transform + "}";
    }


    private long mkNewFlowDiagram(String title,
                                  String userId,
                                  List<FlowDiagramEntity> entities,
                                  List<FlowDiagramAnnotation> annotations) {
        FlowDiagram diagram = ImmutableFlowDiagram.builder()
                .name(title)
                .description(title)
                .layoutData(mkLayoutData(entities, annotations))
                .lastUpdatedAt(DateTimeUtilities.nowUtc())
                .lastUpdatedBy(userId)
                .build();

        long diagramId = flowDiagramDao.create(diagram);
        flowDiagramAnnotationDao.createAnnotations(
                map(annotations, d -> ImmutableFlowDiagramAnnotation
                        .copyOf(d)
                        .withDiagramId(diagramId)));
        flowDiagramEntityDao.createEntities(
                map(entities, d -> ImmutableFlowDiagramEntity
                        .copyOf(d)
                        .withDiagramId(diagramId)));

        return diagramId;
    }


    private static FlowDiagramAnnotation mkDiagramAnnotation(EntityReference entityReference, String note) {
        return ImmutableFlowDiagramAnnotation
                .builder()
                .annotationId(UUID.randomUUID().toString())
                .entityReference(entityReference)
                .note(note)
                .build();
    }

    static private ImmutableFlowDiagramEntity mkDiagramEntity(WaltzEntity entity) {
        return mkDiagramEntity(entity.entityReference());
    }


    static private ImmutableFlowDiagramEntity mkDiagramEntity(EntityReference ref) {
        return ImmutableFlowDiagramEntity
                .builder()
                .entityReference(ref)
                .isNotable(true)
                .build();
    }


    public Collection<FlowDiagram> search(EntitySearchOptions options) {
        return flowDiagramDao.search(options);
    }
}
