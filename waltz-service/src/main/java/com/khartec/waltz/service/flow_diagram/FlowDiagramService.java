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

package com.khartec.waltz.service.flow_diagram;

import com.khartec.waltz.data.flow_diagram.FlowDiagramAnnotationDao;
import com.khartec.waltz.data.flow_diagram.FlowDiagramDao;
import com.khartec.waltz.data.flow_diagram.FlowDiagramEntityDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.flow_diagram.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtc;
import static java.util.stream.Collectors.toList;


@Service
public class FlowDiagramService {

    private final FlowDiagramDao flowDiagramDao;
    private final FlowDiagramEntityDao flowDiagramEntityDao;
    private final FlowDiagramAnnotationDao flowDiagramAnnotationDao;


    @Autowired
    public FlowDiagramService(FlowDiagramDao flowDiagramDao,
                              FlowDiagramEntityDao flowDiagramEntityDao,
                              FlowDiagramAnnotationDao flowDiagramAnnotationDao) {
        checkNotNull(flowDiagramDao, "flowDiagramDao cannot be null");
        checkNotNull(flowDiagramEntityDao, "flowDiagramEntityDao cannot be null");
        checkNotNull(flowDiagramAnnotationDao, "flowDiagramAnnotationDao cannot be null");

        this.flowDiagramDao = flowDiagramDao;
        this.flowDiagramEntityDao = flowDiagramEntityDao;
        this.flowDiagramAnnotationDao = flowDiagramAnnotationDao;
    }


    public FlowDiagram getById(long diagramId) {
        return flowDiagramDao.getById(diagramId);
    }


    public List<FlowDiagram> findByEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return flowDiagramDao.findByEntityReference(ref);
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

        Long diagramId = null;

        if (diagram.id().isPresent()) {
            // update
            diagramId = diagram.id().get();
            flowDiagramDao.update(diagram);
            flowDiagramEntityDao.deleteForDiagram(diagramId);
            flowDiagramAnnotationDao.deleteForDiagram(diagramId);
        } else {
            // create
            diagramId = flowDiagramDao.create(diagram);
        }

        createEntities(diagramId, command.entities());
        createAnnotations(diagramId, command.annotations());
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

}
