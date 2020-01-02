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

package com.khartec.waltz.data.entity_workflow;


import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.entity_workflow.EntityWorkflowState;
import com.khartec.waltz.model.entity_workflow.ImmutableEntityWorkflowState;
import com.khartec.waltz.schema.tables.records.EntityWorkflowStateRecord;
import org.jooq.DSLContext;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.EntityWorkflowState.ENTITY_WORKFLOW_STATE;

@Repository
public class EntityWorkflowStateDao {

    private static final RecordMapper<? super EntityWorkflowStateRecord, EntityWorkflowState> TO_DOMAIN_MAPPER = r ->
            ImmutableEntityWorkflowState
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


    private final DSLContext dsl;


    @Autowired
    public EntityWorkflowStateDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public EntityWorkflowState getByEntityReferenceAndWorkflowId(long workflowId, EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");

        return dsl.selectFrom(ENTITY_WORKFLOW_STATE)
                .where(ENTITY_WORKFLOW_STATE.WORKFLOW_ID.eq(workflowId))
                .and(ENTITY_WORKFLOW_STATE.ENTITY_ID.eq(ref.id()))
                .and(ENTITY_WORKFLOW_STATE.ENTITY_KIND.eq(ref.kind().name()))
                .fetchOne(TO_DOMAIN_MAPPER);
    }
}
