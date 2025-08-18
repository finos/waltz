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

package org.finos.waltz.data.entity_workflow;


import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.model.MakerCheckerState;
import org.finos.waltz.schema.tables.records.EntityWorkflowTransitionRecord;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.ImmutableEntityReference;
import org.finos.waltz.model.entity_workflow.EntityWorkflowTransition;
import org.finos.waltz.model.entity_workflow.ImmutableEntityWorkflowTransition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

import static org.finos.waltz.schema.tables.EntityWorkflowTransition.ENTITY_WORKFLOW_TRANSITION;
import static org.finos.waltz.common.Checks.checkNotNull;

@Repository
public class EntityWorkflowTransitionDao {

    private static final RecordMapper<? super Record, EntityWorkflowTransition> TO_DOMAIN_MAPPER = record -> {
        EntityWorkflowTransitionRecord r = record.into(ENTITY_WORKFLOW_TRANSITION);

        return ImmutableEntityWorkflowTransition
                .builder()
                .workflowId(r.getWorkflowId())
                .entityReference(ImmutableEntityReference.builder()
                        .kind(EntityKind.valueOf(r.getEntityKind()))
                        .id(r.getEntityId())
                        .build())
                .fromState(r.getFromState())
                .toState(r.getToState())
                .reason(r.getReason())
                .lastUpdatedAt(r.getLastUpdatedAt().toLocalDateTime())
                .lastUpdatedBy(r.getLastUpdatedBy())
                .provenance(r.getProvenance())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public EntityWorkflowTransitionDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<EntityWorkflowTransition> findForEntityReferenceAndWorkflowId(long workflowId, EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");

        return dsl
                .select(ENTITY_WORKFLOW_TRANSITION.fields())
                .from(ENTITY_WORKFLOW_TRANSITION)
                .where(ENTITY_WORKFLOW_TRANSITION.WORKFLOW_ID.eq(workflowId))
                .and(ENTITY_WORKFLOW_TRANSITION.ENTITY_ID.eq(ref.id()))
                .and(ENTITY_WORKFLOW_TRANSITION.ENTITY_KIND.eq(ref.kind().name()))
                .fetch(TO_DOMAIN_MAPPER);
    }

    public void createWorkflowTransition(Long proposedFlowId, Long entityWorkflowDefId, String username){
        EntityWorkflowTransitionRecord transitionRecord = dsl.newRecord(ENTITY_WORKFLOW_TRANSITION);
        transitionRecord.setWorkflowId(entityWorkflowDefId);
        transitionRecord.setEntityId(proposedFlowId);
        transitionRecord.setEntityKind(EntityKind.PROPOSED_FLOW.name());
        transitionRecord.setFromState(MakerCheckerState.PROPOSED_CREATE.name());
        transitionRecord.setToState(MakerCheckerState.ACTION_PENDING.name());
        transitionRecord.setReason("flow proposed");
        transitionRecord.setProvenance("waltz");
        transitionRecord.setLastUpdatedAt(Timestamp.valueOf(DateTimeUtilities.nowUtc()));
        transitionRecord.setLastUpdatedBy(username);
        transitionRecord.insert();
    }

    public List<EntityWorkflowTransition> findForWorkflowId(long workflowId) {

        return dsl
                .select(ENTITY_WORKFLOW_TRANSITION.fields())
                .from(ENTITY_WORKFLOW_TRANSITION)
                .where(ENTITY_WORKFLOW_TRANSITION.WORKFLOW_ID.eq(workflowId))
                .orderBy(ENTITY_WORKFLOW_TRANSITION.LAST_UPDATED_AT.desc())
                .fetch(TO_DOMAIN_MAPPER);
    }
}
