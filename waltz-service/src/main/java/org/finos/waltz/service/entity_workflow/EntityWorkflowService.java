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

package org.finos.waltz.service.entity_workflow;


import org.finos.waltz.data.entity_workflow.EntityWorkflowDefinitionDao;
import org.finos.waltz.data.entity_workflow.EntityWorkflowStateDao;
import org.finos.waltz.data.entity_workflow.EntityWorkflowTransitionDao;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.entity_workflow.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class EntityWorkflowService {

    private final EntityWorkflowDefinitionDao entityWorkflowDefinitionDao;
    private final EntityWorkflowStateDao entityWorkflowStateDao;
    private final EntityWorkflowTransitionDao entityWorkflowTransitionDao;

    @Autowired
    public EntityWorkflowService(EntityWorkflowDefinitionDao entityWorkflowDefinitionDao,
                                 EntityWorkflowStateDao entityWorkflowStateDao,
                                 EntityWorkflowTransitionDao entityWorkflowTransitionDao) {
        this.entityWorkflowDefinitionDao = entityWorkflowDefinitionDao;
        this.entityWorkflowStateDao = entityWorkflowStateDao;
        this.entityWorkflowTransitionDao = entityWorkflowTransitionDao;
    }


    public List<EntityWorkflowDefinition> findAllDefinitions() {
        return entityWorkflowDefinitionDao.findAll();
    }


    public EntityWorkflowState getStateForEntityReferenceAndWorkflowId(long workflowId,
                                                                       EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");

        return entityWorkflowStateDao.getWorkflowState(workflowId, ref);
    }


    public List<EntityWorkflowTransition> findTransitionsForEntityReferenceAndWorkflowId(long workflowId,
                                                                                         EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");

        return entityWorkflowTransitionDao.findForEntityReferenceAndWorkflowId(workflowId, ref);
    }

    public EntityWorkflowDefinition searchByName(String name) {
        return entityWorkflowDefinitionDao.searchByName(name);
    }

    public void updateEntityWorkflow(EntityReference ref, Long entityWorkflowDefinitionId, String username,
                                     String prevState, String newState, String transitionReason) {
        entityWorkflowStateDao.updateState(entityWorkflowDefinitionId, ref,
                username, newState);
        entityWorkflowTransitionDao.createWorkflowTransition(entityWorkflowDefinitionId, ref,
                username, prevState, newState, transitionReason);
    }

    public void createEntityWorkflow(EntityReference ref, Long entityWorkflowDefinitionId, String username,
                                     String workFlowStateDesc,
                                     String prevState, String newState, String transitionReason) {
        entityWorkflowStateDao.createWorkflowState(entityWorkflowDefinitionId, ref,
                username, newState, workFlowStateDesc);
        entityWorkflowTransitionDao.createWorkflowTransition(entityWorkflowDefinitionId, ref,
                username, prevState, newState, transitionReason);
    }

    public EntityWorkflowView getEntityWorkflowView(String workFlowDefName, EntityReference ref) {
        checkNotNull(workFlowDefName, "workFlowDefName cannot be null");
        checkNotNull(ref, "ref cannot be null");

        EntityWorkflowDefinition entityWorkflowDefinition = searchByName(workFlowDefName);
        Long workFlowId = Optional.ofNullable(entityWorkflowDefinition)
                .flatMap(EntityWorkflowDefinition::id)
                .orElseThrow(() -> new NoSuchElementException("Workflow not found"));
        EntityWorkflowState entityWorkflowState = entityWorkflowStateDao.getWorkflowState(workFlowId, ref);
        List<EntityWorkflowTransition> entityWorkflowTransitionList = entityWorkflowTransitionDao.findForEntityReferenceAndWorkflowId(workFlowId, ref);

        return ImmutableEntityWorkflowView.builder()
                .workflowDefinition(entityWorkflowDefinition)
                .workflowState(entityWorkflowState)
                .workflowTransitionList(entityWorkflowTransitionList)
                .build();
    }
}
