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
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.ImmutableEntityReference;
import org.finos.waltz.model.entity_workflow.EntityWorkflowState;
import org.finos.waltz.model.entity_workflow.ImmutableEntityWorkflowState;
import org.finos.waltz.schema.tables.records.EntityWorkflowStateRecord;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.Checks.checkTrue;
import static org.finos.waltz.schema.Tables.*;
import static org.finos.waltz.schema.tables.EntityWorkflowState.ENTITY_WORKFLOW_STATE;

@Repository
public class EntityWorkflowStateDao {
    public static final RecordMapper<? super Record, EntityWorkflowState> TO_DOMAIN_MAPPER = record -> {
        EntityWorkflowStateRecord r = record.into(ENTITY_WORKFLOW_STATE);

        return ImmutableEntityWorkflowState
                .builder()
                .workflowId(r.getWorkflowId())
                .entityReference(ImmutableEntityReference.builder()
                        .kind(EntityKind.valueOf(r.getEntityKind()))
                        .id(r.getEntityId())
                        .build())
                .state(r.getState())
                .description(r.getDescription())
                .lastUpdatedAt(r.getLastUpdatedAt().toLocalDateTime())
                .lastUpdatedBy(r.getLastUpdatedBy())
                .provenance(r.getProvenance())
                .version(r.getVersion())
                .build();
    };

    private final DSLContext dsl;

    @Autowired
    public EntityWorkflowStateDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public EntityWorkflowState getByEntityReferenceAndWorkflowId(long workflowId, EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");

        return dsl
                .select(ENTITY_WORKFLOW_STATE.fields())
                .from(ENTITY_WORKFLOW_STATE)
                .where(ENTITY_WORKFLOW_STATE.WORKFLOW_ID.eq(workflowId))
                .and(ENTITY_WORKFLOW_STATE.ENTITY_ID.eq(ref.id()))
                .and(ENTITY_WORKFLOW_STATE.ENTITY_KIND.eq(ref.kind().name()))
                .fetchOne(TO_DOMAIN_MAPPER);
    }

    public void createWorkflowState(Long workflowDefId,
                                    EntityReference ref,
                                    String username,
                                    String workflowState,
                                    String description) {
        EntityWorkflowStateRecord stateRecord = dsl.newRecord(ENTITY_WORKFLOW_STATE);
        stateRecord.setWorkflowId(workflowDefId);
        stateRecord.setEntityId(ref.id());
        stateRecord.setEntityKind(ref.kind().name());
        stateRecord.setState(workflowState);
        stateRecord.setDescription(description);
        stateRecord.setProvenance("waltz");
        stateRecord.setLastUpdatedBy(username);
        stateRecord.setLastUpdatedAt(Timestamp.valueOf(DateTimeUtilities.nowUtc()));
        stateRecord.insert();
    }

    public long updateState(String username,
                            String reason,
                            List<EntityWorkflowState> workflowStates,
                            List<String> currentStates,
                            String newState) {
        checkNotNull(workflowStates, "workflowStates cannot be null");
        checkNotNull(currentStates, "currentStates cannot be null");
        checkTrue(workflowStates.size() == currentStates.size(), "workflowStates and currentStates must have the same size");

        if (workflowStates.isEmpty()) {
            return 0;
        }

        final AtomicInteger result = new AtomicInteger();
        dsl.transaction(ctx -> {
            DSLContext tx = ctx.dsl();
            result.set(doUpdateStateTransition(tx, username, reason, workflowStates, currentStates, newState));
        });
        return result.get();
    }

    private Integer doUpdateStateTransition(DSLContext tx, String username, String reason, List<EntityWorkflowState> workflowStates,
                                         List<String> currentStates, String newState) {

        Timestamp now = Timestamp.valueOf(DateTimeUtilities.nowUtc());

        List<Query> stateQueries = new ArrayList<>(workflowStates.size());
        List<Query> transitionQueries = new ArrayList<>(workflowStates.size());

        for (int i = 0; i < workflowStates.size(); i++) {
            EntityWorkflowState workflowState = workflowStates.get(i);

            stateQueries.add(
                    tx.update(ENTITY_WORKFLOW_STATE)
                            .set(ENTITY_WORKFLOW_STATE.STATE, newState)
                            .set(ENTITY_WORKFLOW_STATE.LAST_UPDATED_AT, now)
                            .set(ENTITY_WORKFLOW_STATE.LAST_UPDATED_BY, username)
                            .set(ENTITY_WORKFLOW_STATE.VERSION, workflowState.version() + 1)
                            .where(ENTITY_WORKFLOW_STATE.WORKFLOW_ID.eq(workflowState.workflowId()))
                            .and(ENTITY_WORKFLOW_STATE.ENTITY_KIND.eq(workflowState.entityReference().kind().name()))
                            .and(ENTITY_WORKFLOW_STATE.ENTITY_ID.eq(workflowState.entityReference().id()))
                            .and(ENTITY_WORKFLOW_STATE.VERSION.eq(workflowState.version())));

            transitionQueries.add(
                    tx.insertInto(ENTITY_WORKFLOW_TRANSITION)
                            .set(ENTITY_WORKFLOW_TRANSITION.WORKFLOW_ID, workflowState.workflowId())
                            .set(ENTITY_WORKFLOW_TRANSITION.ENTITY_ID, workflowState.entityReference().id())
                            .set(ENTITY_WORKFLOW_TRANSITION.ENTITY_KIND, workflowState.entityReference().kind().name())
                            .set(ENTITY_WORKFLOW_TRANSITION.FROM_STATE, currentStates.get(i))
                            .set(ENTITY_WORKFLOW_TRANSITION.TO_STATE, newState)
                            .set(ENTITY_WORKFLOW_TRANSITION.REASON, reason)
                            .set(ENTITY_WORKFLOW_TRANSITION.LAST_UPDATED_AT, now)
                            .set(ENTITY_WORKFLOW_TRANSITION.LAST_UPDATED_BY, username)
                            .set(ENTITY_WORKFLOW_TRANSITION.PROVENANCE, "waltz"));
        }


        int updatedRows = Arrays.stream(tx.batch(stateQueries).execute()).sum();
        int insertedRows = Arrays.stream(tx.batch(transitionQueries).execute()).sum();

        checkTrue(updatedRows == workflowStates.size(),
                "Workflow state update failed. Updated rows: %s, expected rows: %s",
                updatedRows,
                workflowStates.size());

        checkTrue(insertedRows == workflowStates.size(),
                "Workflow transition insert failed. Inserted rows: %s, expected rows: %s",
                insertedRows,
                workflowStates.size());

        return updatedRows;


    }
}
