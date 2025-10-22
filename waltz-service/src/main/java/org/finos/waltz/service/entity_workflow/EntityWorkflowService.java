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
import org.finos.waltz.data.entity_workflow.EntityWorkflowResultDao;
import org.finos.waltz.data.entity_workflow.EntityWorkflowStateDao;
import org.finos.waltz.data.entity_workflow.EntityWorkflowTransitionDao;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.Severity;
import org.finos.waltz.model.changelog.ChangeLog;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.entity_workflow.EntityWorkflowDefinition;
import org.finos.waltz.model.entity_workflow.EntityWorkflowResult;
import org.finos.waltz.model.entity_workflow.EntityWorkflowState;
import org.finos.waltz.model.entity_workflow.EntityWorkflowTransition;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.model.Operation.ADD;
import static org.finos.waltz.model.Operation.UPDATE;
import static org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState.PROPOSED_CREATE;

@Service
public class EntityWorkflowService {
    private final ChangeLogService changeLogService;
    private final EntityWorkflowDefinitionDao entityWorkflowDefinitionDao;
    private final EntityWorkflowStateDao entityWorkflowStateDao;
    private final EntityWorkflowTransitionDao entityWorkflowTransitionDao;
    private final EntityWorkflowResultDao entityWorkflowResultDao;

    @Autowired
    public EntityWorkflowService(ChangeLogService changeLogService, EntityWorkflowDefinitionDao entityWorkflowDefinitionDao,
                                 EntityWorkflowStateDao entityWorkflowStateDao,
                                 EntityWorkflowTransitionDao entityWorkflowTransitionDao, EntityWorkflowResultDao entityWorkflowResultDao) {
        this.changeLogService = changeLogService;
        this.entityWorkflowDefinitionDao = entityWorkflowDefinitionDao;
        this.entityWorkflowStateDao = entityWorkflowStateDao;
        this.entityWorkflowTransitionDao = entityWorkflowTransitionDao;
        this.entityWorkflowResultDao = entityWorkflowResultDao;
    }


    public List<EntityWorkflowDefinition> findAllDefinitions() {
        return entityWorkflowDefinitionDao.findAll();
    }


    public EntityWorkflowState getStateForEntityReferenceAndWorkflowId(long workflowId,
                                                                       EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");

        return entityWorkflowStateDao.getByEntityReferenceAndWorkflowId(workflowId, ref);
    }


    public List<EntityWorkflowTransition> findTransitionsForEntityReferenceAndWorkflowId(long workflowId,
                                                                                         EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");

        return entityWorkflowTransitionDao.findForEntityReferenceAndWorkflowId(workflowId, ref);
    }

    public EntityWorkflowDefinition searchByName(String name) {
        return entityWorkflowDefinitionDao.searchByName(name);
    }

    public void createEntityWorkflow(EntityReference ref, Long entityWorkflowDefinitionId, String username,
                                     String workflowStateDesc,
                                     String prevState, String newState, String transitionReason) {
        entityWorkflowStateDao.createWorkflowState(entityWorkflowDefinitionId, ref,
                username, newState, workflowStateDesc);
        entityWorkflowTransitionDao.createWorkflowTransition(entityWorkflowDefinitionId, ref,
                username, prevState, newState, transitionReason);

        List<ChangeLog> changeLogList = Arrays.asList(
                mkChangeLog(ref, username, ADD, "New Workflow Created"),
                mkChangeLog(ref, username, ADD, format("Entity Workflow State changed to %s", newState)),
                mkChangeLog(ref, username, ADD,
                        format("Entity Workflow Transition saved with from: %s to: %s State", PROPOSED_CREATE, newState))
        );
        changeLogService.write(changeLogList);
    }

    public void updateStateTransition(String username, String reason, EntityWorkflowState workflowState,
                                      String currentState, String newState) {
        entityWorkflowStateDao.updateState(workflowState.workflowId(), workflowState.entityReference(),
                username, newState);
        entityWorkflowTransitionDao.createWorkflowTransition(workflowState.workflowId(), workflowState.entityReference(),
                username, currentState, newState, reason);

        List<ChangeLog> changeLogList = Arrays.asList(
                mkChangeLog(workflowState.entityReference(), username, UPDATE,
                        format("Entity Workflow State changed to %s", newState)),
                mkChangeLog(workflowState.entityReference(), username, UPDATE,
                        format("Entity Workflow Transition saved with from: %s to: %s State", currentState, newState))
        );
        changeLogService.write(changeLogList);
    }

    public void createEntityWorkflowResult(Long entityWorkflowDefinitionId, EntityReference workflowEntity, EntityReference resultEntity, String username) {
        entityWorkflowResultDao.create(entityWorkflowDefinitionId, workflowEntity, resultEntity, username);
    }

    public List<EntityWorkflowResult> findByWorkflowEntity(Long entityWorkflowDefinitionId, EntityReference workflowEntity) {
        return entityWorkflowResultDao.findByWorkflowEntity(entityWorkflowDefinitionId, workflowEntity);
    }

    private ImmutableChangeLog mkChangeLog(EntityReference entityReference,
                                           String userId,
                                           Operation operation,
                                           String message) {
        return ImmutableChangeLog.builder()
                .parentReference(entityReference)
                .message(message)
                .userId(userId)
                .operation(operation)
                .severity(Severity.INFORMATION)
                .build();
    }
}
