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

package com.khartec.waltz.service.flow_diagram;

import org.finos.waltz.data.flow_diagram.FlowDiagramOverlayGroupDao;
import org.finos.waltz.model.flow_diagram.FlowDiagramOverlayGroup;
import org.finos.waltz.model.flow_diagram.FlowDiagramOverlayGroupEntry;
import org.finos.waltz.model.flow_diagram.ImmutableFlowDiagramOverlayGroup;
import org.finos.waltz.model.flow_diagram.ImmutableFlowDiagramOverlayGroupEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.SetUtilities.map;


@Service
public class FlowDiagramOverlayGroupService {

    private final FlowDiagramOverlayGroupDao flowDiagramOverlayGroupDao;

    @Autowired
    public FlowDiagramOverlayGroupService(FlowDiagramOverlayGroupDao flowDiagramOverlayGroupDao) {
        checkNotNull(flowDiagramOverlayGroupDao, "flowDiagramOverlayGroupDao cannot be null");
        this.flowDiagramOverlayGroupDao = flowDiagramOverlayGroupDao;
    }


    public Set<FlowDiagramOverlayGroup> findByDiagramId(long diagramId) {
        return flowDiagramOverlayGroupDao.findByDiagramId(diagramId);
    }

    public Set<FlowDiagramOverlayGroupEntry> findOverlaysByDiagramId(long diagramId) {
        return flowDiagramOverlayGroupDao.findOverlaysByDiagramId(diagramId);
    }


    public Long create(FlowDiagramOverlayGroup group, String username) {
        //create change log
        return flowDiagramOverlayGroupDao.create(group);
    }


    public boolean delete(Long id, String username) {
        //create change log
        return flowDiagramOverlayGroupDao.delete(id);
    }


    public int updateOverlaysForDiagram(Long diagramId,
                                         Set<FlowDiagramOverlayGroupEntry> overlays,
                                         String username) {
        //create change log
        int deletedOverlays = flowDiagramOverlayGroupDao.deleteOverlaysForDiagram(diagramId);
        return flowDiagramOverlayGroupDao.createOverlays(overlays);
    }


    public Long clone(long diagramId, long id, String username) {
        FlowDiagramOverlayGroup group = flowDiagramOverlayGroupDao.getById(id);
        FlowDiagramOverlayGroup clonedGroup = ImmutableFlowDiagramOverlayGroup
                .copyOf(group)
                .withId(Optional.empty())
                .withDiagramId(diagramId);

        Long clonedGroupId = flowDiagramOverlayGroupDao.create(clonedGroup);

        Set<FlowDiagramOverlayGroupEntry> overlays = flowDiagramOverlayGroupDao.findOverlaysByGroupId(id);

        Set<FlowDiagramOverlayGroupEntry> clonedOverlays = map(overlays,
                o -> ImmutableFlowDiagramOverlayGroupEntry.copyOf(o)
                        .withId(Optional.empty())
                        .withOverlayGroupId(clonedGroupId));

        int overlaysCreated = flowDiagramOverlayGroupDao.createOverlays(clonedOverlays);
        return clonedGroupId;
    }
}
