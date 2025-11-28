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

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.entity_workflow.EntityWorkflowResult;
import org.finos.waltz.model.entity_workflow.ImmutableEntityWorkflowResult;
import org.finos.waltz.schema.tables.records.EntityWorkflowResultRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.tables.EntityWorkflowResult.ENTITY_WORKFLOW_RESULT;

@Repository
public class EntityWorkflowResultDao {
    public static final RecordMapper<? super Record, EntityWorkflowResult> TO_DOMAIN_MAPPER = r -> {
        EntityWorkflowResultRecord record = r.into(ENTITY_WORKFLOW_RESULT);
        return ImmutableEntityWorkflowResult.builder()
                .workflowId(record.getWorkflowId())
                .workflowEntity(mkRef(
                        EntityKind.valueOf(record.getWorkflowEntityKind()),
                        record.getWorkflowEntityId()))
                .resultEntity(mkRef(
                        EntityKind.valueOf(record.getResultEntityKind()),
                        record.getResultEntityId()))
                .createdAt(record.getCreatedAt().toLocalDateTime())
                .createdBy(record.getCreatedBy())
                .build();
    };

    private final DSLContext dsl;

    @Autowired
    public EntityWorkflowResultDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }

    /**
     * Creates a link between a workflow entity and its result.
     * If the link already exists, it does nothing.
     *
     * @param workflowEntity The source entity (e.g., the Proposed Flow)
     * @param resultEntity   The resulting entity (e.g., the created Logical Flow)
     * @param username       The user creating the link
     */
    public void create(Long workflowId, EntityReference workflowEntity, EntityReference resultEntity, String username) {
        checkNotNull(workflowEntity, "workflowEntity cannot be null");
        checkNotNull(resultEntity, "resultEntity cannot be null");

        dsl.insertInto(ENTITY_WORKFLOW_RESULT)
                .set(ENTITY_WORKFLOW_RESULT.WORKFLOW_ID, workflowId)
                .set(ENTITY_WORKFLOW_RESULT.WORKFLOW_ENTITY_KIND, workflowEntity.kind().name())
                .set(ENTITY_WORKFLOW_RESULT.WORKFLOW_ENTITY_ID, workflowEntity.id())
                .set(ENTITY_WORKFLOW_RESULT.RESULT_ENTITY_KIND, resultEntity.kind().name())
                .set(ENTITY_WORKFLOW_RESULT.RESULT_ENTITY_ID, resultEntity.id())
                .set(ENTITY_WORKFLOW_RESULT.CREATED_BY, username)
                .onDuplicateKeyIgnore()
                .execute();
    }

    /**
     * Finds the entity that was created as a result of a given workflow entity.
     *
     * @param workflowEntity The source entity (e.g., the Proposed Flow)
     * @return The reference to the resulting entity, if any
     */
    public List<EntityWorkflowResult> findByWorkflowEntity(Long workflowId, EntityReference workflowEntity) {
        checkNotNull(workflowEntity, "workflowEntity cannot be null");

        return dsl
                .select(ENTITY_WORKFLOW_RESULT.fields())
                .from(ENTITY_WORKFLOW_RESULT)
                .where(ENTITY_WORKFLOW_RESULT.WORKFLOW_ENTITY_KIND.eq(workflowEntity.kind().name()))
                .and(ENTITY_WORKFLOW_RESULT.WORKFLOW_ENTITY_ID.eq(workflowEntity.id()))
                .and(ENTITY_WORKFLOW_RESULT.WORKFLOW_ID.eq(workflowId))
                .fetch(TO_DOMAIN_MAPPER);
    }
}
