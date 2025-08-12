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
import org.finos.waltz.model.entity_workflow.WorkflowStateChangeCommand;
import org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState;
import org.finos.waltz.schema.tables.records.EntityWorkflowStateRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.schema.tables.EntityWorkflowState.ENTITY_WORKFLOW_STATE;

@Repository
public class EntityWorkflowStateDao {
    private final DSLContext dsl;

    @Autowired
    public EntityWorkflowStateDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    private static final RecordMapper<? super Record, EntityWorkflowState> TO_DOMAIN_MAPPER = record -> {
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
                .build();
    };

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

    public void createWorkflowState(Long requestFlowId, Long entityWorkflowDefId, String username, EntityKind entityKind,
                                    ProposedFlowWorkflowState workflowState, String description) {
        EntityWorkflowStateRecord stateRecord = dsl.newRecord(ENTITY_WORKFLOW_STATE);
        stateRecord.setWorkflowId(entityWorkflowDefId);
        stateRecord.setEntityId(requestFlowId);
        stateRecord.setEntityKind(entityKind.name());
        stateRecord.setState(workflowState.name());
        stateRecord.setDescription(description);
        stateRecord.setProvenance("waltz");
        stateRecord.setLastUpdatedBy(username);
        stateRecord.setLastUpdatedAt(Timestamp.valueOf(DateTimeUtilities.nowUtc()));
        stateRecord.insert();
    }

    /**
     * Inserts a new workflow state record, or updates the existing record
     * if one already exists for the given entity reference. The uniqueness
     * is determined by the combination of entity_id and entity_kind.
     *
     * @param command  The command object containing details of the state change.
     * @param username The user performing the action.
     */
    // TODO..unsed method for future use by the state machine??
    public void insertOrUpdate(WorkflowStateChangeCommand command, String username) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        dsl.insertInto(ENTITY_WORKFLOW_STATE)
                .set(ENTITY_WORKFLOW_STATE.ENTITY_ID, command.entityReference().id())
                .set(ENTITY_WORKFLOW_STATE.ENTITY_KIND, command.entityReference().kind().name())
                .set(ENTITY_WORKFLOW_STATE.WORKFLOW_ID, command.workflowId())
                .set(ENTITY_WORKFLOW_STATE.STATE, command.state())
                .set(ENTITY_WORKFLOW_STATE.PROVENANCE, command.provenance())
                .set(ENTITY_WORKFLOW_STATE.DESCRIPTION, command.description())
                .set(ENTITY_WORKFLOW_STATE.LAST_UPDATED_BY, username)
                .set(ENTITY_WORKFLOW_STATE.LAST_UPDATED_AT, now)
                // This is the "upsert" part.
                // It specifies the unique key to check for conflicts.
                .onConflict(
                        ENTITY_WORKFLOW_STATE.ENTITY_ID,
                        ENTITY_WORKFLOW_STATE.ENTITY_KIND)
                // If a conflict occurs, perform an update instead.
                .doUpdate()
                .set(ENTITY_WORKFLOW_STATE.STATE, command.state())
                .set(ENTITY_WORKFLOW_STATE.LAST_UPDATED_BY, username)
                .set(ENTITY_WORKFLOW_STATE.LAST_UPDATED_AT, now)
                .execute();
    }
}
