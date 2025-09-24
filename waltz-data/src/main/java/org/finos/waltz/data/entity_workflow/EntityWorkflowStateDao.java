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
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.DateTimeUtilities.nowUtc;
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

    public long updateState(Long workflowDefId, EntityReference ref, String user, String workflowState) {
        return dsl
                .update(ENTITY_WORKFLOW_STATE)
                .set(ENTITY_WORKFLOW_STATE.STATE, workflowState)
                .set(ENTITY_WORKFLOW_STATE.LAST_UPDATED_AT, Timestamp.valueOf(nowUtc()))
                .set(ENTITY_WORKFLOW_STATE.LAST_UPDATED_BY, user)
                .where(ENTITY_WORKFLOW_STATE.WORKFLOW_ID.eq(workflowDefId)
                        .and(ENTITY_WORKFLOW_STATE.ENTITY_ID.eq(ref.id()))
                        .and(ENTITY_WORKFLOW_STATE.ENTITY_KIND.eq(ref.kind().name())))
                .execute();
    }
}
