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

package com.khartec.waltz.service.flow_diagram;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.data.flow_diagram.FlowDiagramAnnotationDao;
import com.khartec.waltz.data.flow_diagram.FlowDiagramDao;
import com.khartec.waltz.data.flow_diagram.FlowDiagramEntityDao;
import com.khartec.waltz.data.flow_diagram.FlowDiagramIdSelectorFactory;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.flow_diagram.*;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.exception.InvalidResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtc;
import static com.khartec.waltz.model.EntityKind.FLOW_DIAGRAM;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static java.util.stream.Collectors.toList;


@Service
public class FlowDiagramService {

    private final ChangeLogService changeLogService;
    private final FlowDiagramDao flowDiagramDao;
    private final FlowDiagramEntityDao flowDiagramEntityDao;
    private final FlowDiagramAnnotationDao flowDiagramAnnotationDao;
    private final FlowDiagramIdSelectorFactory flowDiagramIdSelectorFactory;


    @Autowired
    public FlowDiagramService(ChangeLogService changeLogService,
                              FlowDiagramDao flowDiagramDao,
                              FlowDiagramEntityDao flowDiagramEntityDao,
                              FlowDiagramAnnotationDao flowDiagramAnnotationDao,
                              FlowDiagramIdSelectorFactory flowDiagramIdSelectorFactory) {
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(flowDiagramDao, "flowDiagramDao cannot be null");
        checkNotNull(flowDiagramEntityDao, "flowDiagramEntityDao cannot be null");
        checkNotNull(flowDiagramAnnotationDao, "flowDiagramAnnotationDao cannot be null");
        checkNotNull(flowDiagramIdSelectorFactory, "flowDiagramIdSelectorFactory cannot be null");

        this.changeLogService = changeLogService;
        this.flowDiagramDao = flowDiagramDao;
        this.flowDiagramEntityDao = flowDiagramEntityDao;
        this.flowDiagramAnnotationDao = flowDiagramAnnotationDao;
        this.flowDiagramIdSelectorFactory = flowDiagramIdSelectorFactory;
    }


    public FlowDiagram getById(long diagramId) {
        return flowDiagramDao.getById(diagramId);
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

        List<EntityReference> existingEntities = ListUtilities.newArrayList();

        if (diagram.id().isPresent()) {
            // update
            diagramId = diagram.id().get();

            if(!flowDiagramDao.update(diagram)) {
                throw new InvalidResultException("Could not update diagram with Id: " + diagramId);
            }

            existingEntities = ListUtilities.map(flowDiagramEntityDao.findForDiagram(diagramId), fde -> fde.entityReference());
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

        List<EntityReference> newEntities = ListUtilities.map(command.entities(), fde -> fde.entityReference());
        auditEntityChange(mkRef(FLOW_DIAGRAM, diagramId), existingEntities, newEntities, username);
        return diagramId;
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
        flowDiagramAnnotationDao.deleteForDiagram(id);
        flowDiagramEntityDao.deleteForDiagram(id);
        auditChange("removed", mkRef(FLOW_DIAGRAM, id), username, Operation.REMOVE);
        return flowDiagramDao.deleteById(id);
    }


    private void auditChange(String verb, EntityReference diagramRef, String username, Operation operation) {
        ImmutableChangeLog logEntry = ImmutableChangeLog.builder()
                .parentReference(diagramRef)
                .severity(Severity.INFORMATION)
                .userId(username)
                .message(String.format(
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
        List<EntityReference> addedEntities = new ArrayList(newEntities);
        addedEntities.removeAll(previousEntities);

        List<ChangeLog> addLogEntries = addedEntities.stream()
                .map(ref -> ImmutableChangeLog.builder()
                        .parentReference(diagramRef)
                        .severity(Severity.INFORMATION)
                        .userId(username)
                        .message(String.format(
                                "Added entity to diagram: %s",
                                ref))
                        .childKind(ref.kind())
                        .operation(Operation.ADD)
                        .build())
                .collect(Collectors.toList());

        // get removed entities
        List<EntityReference> removedEntities = new ArrayList(previousEntities);
        removedEntities.removeAll(newEntities);

        List<ChangeLog> removeLogEntries = removedEntities.stream()
                .map(ref -> ImmutableChangeLog.builder()
                        .parentReference(diagramRef)
                        .severity(Severity.INFORMATION)
                        .userId(username)
                        .message(String.format(
                                "Removed entity from diagram: %s",
                                ref))
                        .childKind(ref.kind())
                        .operation(Operation.REMOVE)
                        .build())
                .collect(Collectors.toList());

        List<ChangeLog> allLogEntries = new ArrayList<>(addLogEntries);
        allLogEntries.addAll(removeLogEntries);
        changeLogService.write(allLogEntries);
    }
}
