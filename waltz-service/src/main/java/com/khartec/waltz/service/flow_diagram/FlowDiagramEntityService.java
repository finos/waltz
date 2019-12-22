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

import com.khartec.waltz.data.GenericSelector;
import com.khartec.waltz.data.GenericSelectorFactory;
import com.khartec.waltz.data.flow_diagram.FlowDiagramEntityDao;
import com.khartec.waltz.data.flow_diagram.FlowDiagramIdSelectorFactory;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.flow_diagram.FlowDiagramEntity;
import com.khartec.waltz.model.flow_diagram.ImmutableFlowDiagramEntity;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.newArrayList;


@Service
public class FlowDiagramEntityService {

    private final FlowDiagramEntityDao flowDiagramEntityDao;
    private final FlowDiagramIdSelectorFactory flowDiagramIdSelectorFactory = new FlowDiagramIdSelectorFactory();
    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();


    @Autowired
    public FlowDiagramEntityService(FlowDiagramEntityDao flowDiagramEntityDao) {
        checkNotNull(flowDiagramEntityDao, "flowDiagramEntityDao cannot be null");
        this.flowDiagramEntityDao = flowDiagramEntityDao;
    }


    public List<FlowDiagramEntity> findByDiagramId(long diagramId) {
        return flowDiagramEntityDao.findForDiagram(diagramId);
    }


    public List<FlowDiagramEntity> findByEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return flowDiagramEntityDao.findForEntity(ref);
    }


    public List<FlowDiagramEntity> findForEntitySelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        GenericSelector selector = genericSelectorFactory.apply(options);
        return flowDiagramEntityDao.findForEntitySelector(selector.kind(), selector.selector());
    }


    public List<FlowDiagramEntity> findForDiagramSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = flowDiagramIdSelectorFactory.apply(options);
        return flowDiagramEntityDao.findForDiagramSelector(selector);
    }


    public boolean removeRelationship(long diagramId, EntityReference entityReference) {
        return flowDiagramEntityDao.deleteEntityForDiagram(
                diagramId,
                entityReference);
    }


    public boolean addRelationship(long diagramId, EntityReference entityReference) {
        ArrayList<FlowDiagramEntity> entities = newArrayList(
                ImmutableFlowDiagramEntity.builder()
                        .diagramId(diagramId)
                        .entityReference(entityReference)
                        .isNotable(false)
                        .build());
        int[] rc = flowDiagramEntityDao.createEntities(entities);
        return rc[0] == 1;
    }


    public int deleteForEntitySelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        GenericSelector selector = genericSelectorFactory.apply(options);
        return flowDiagramEntityDao.deleteForGenericEntitySelector(selector);

    }
}
