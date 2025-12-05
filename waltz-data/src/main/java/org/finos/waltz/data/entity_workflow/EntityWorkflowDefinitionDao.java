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


import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.entity_workflow.*;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.records.EntityWorkflowDefinitionRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.schema.Tables.*;
import static org.finos.waltz.schema.tables.EntityWorkflowDefinition.ENTITY_WORKFLOW_DEFINITION;

@Repository
public class EntityWorkflowDefinitionDao {

    public static final RecordMapper<? super Record, EntityWorkflowDefinition> TO_DOMAIN_MAPPER = record -> {
        EntityWorkflowDefinitionRecord r = record.into(ENTITY_WORKFLOW_DEFINITION);

        return ImmutableEntityWorkflowDefinition
                .builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .build();
    };

    private final DSLContext dsl;


    @Autowired
    public EntityWorkflowDefinitionDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<EntityWorkflowDefinition> findAll() {
        return dsl
                .select(ENTITY_WORKFLOW_DEFINITION.fields())
                .from(ENTITY_WORKFLOW_DEFINITION)
                .fetch(TO_DOMAIN_MAPPER);
    }

    public EntityWorkflowDefinition searchByName(String name) {
        return dsl.select(ENTITY_WORKFLOW_DEFINITION.fields())
                .from(ENTITY_WORKFLOW_DEFINITION)
                .where(ENTITY_WORKFLOW_DEFINITION.NAME.eq(name))
                .fetchOne(TO_DOMAIN_MAPPER);
    }

    public EntityWorkflowView getEntityWorkflowView(String workFlowDefName, EntityReference ref) {
        checkNotNull(workFlowDefName, "workFlowDefName cannot be null");
        checkNotNull(ref, "ref cannot be null");

        // Fetch all related workflow data in a single query
        Result<Record> records = dsl
                .select(Tables.ENTITY_WORKFLOW_DEFINITION.fields())
                .select(ENTITY_WORKFLOW_STATE.fields())
                .select(ENTITY_WORKFLOW_TRANSITION.fields())
                .select(ENTITY_WORKFLOW_RESULT.fields())
                .from(Tables.ENTITY_WORKFLOW_DEFINITION)
                .leftJoin(ENTITY_WORKFLOW_STATE).on(ENTITY_WORKFLOW_STATE.WORKFLOW_ID.eq(Tables.ENTITY_WORKFLOW_DEFINITION.ID)
                        .and(ENTITY_WORKFLOW_STATE.ENTITY_ID.eq(ref.id()))
                        .and(ENTITY_WORKFLOW_STATE.ENTITY_KIND.eq(ref.kind().name())))
                .leftJoin(ENTITY_WORKFLOW_TRANSITION).on(ENTITY_WORKFLOW_TRANSITION.WORKFLOW_ID.eq(Tables.ENTITY_WORKFLOW_DEFINITION.ID)
                        .and(ENTITY_WORKFLOW_TRANSITION.ENTITY_ID.eq(ref.id()))
                        .and(ENTITY_WORKFLOW_TRANSITION.ENTITY_KIND.eq(ref.kind().name())))
                .leftJoin(ENTITY_WORKFLOW_RESULT).on(ENTITY_WORKFLOW_RESULT.WORKFLOW_ID.eq(Tables.ENTITY_WORKFLOW_DEFINITION.ID)
                        .and(ENTITY_WORKFLOW_RESULT.WORKFLOW_ENTITY_ID.eq(ref.id()))
                        .and(ENTITY_WORKFLOW_RESULT.WORKFLOW_ENTITY_KIND.eq(ref.kind().name())))
                .where(Tables.ENTITY_WORKFLOW_DEFINITION.NAME.eq(workFlowDefName))
                .fetch();

        checkNotNull(ref, "Workflow not found: " + workFlowDefName);

        // All records share the same definition and state, so we can map them from the first record
        Record firstRecord = records.get(0);
        EntityWorkflowDefinition definition = EntityWorkflowDefinitionDao.TO_DOMAIN_MAPPER.map(firstRecord.into(Tables.ENTITY_WORKFLOW_DEFINITION));
        EntityWorkflowState state = firstRecord.get(ENTITY_WORKFLOW_STATE.WORKFLOW_ID) == null
                ? null
                : EntityWorkflowStateDao.TO_DOMAIN_MAPPER.map(firstRecord.into(ENTITY_WORKFLOW_STATE));

        // Map the potentially multiple transitions and results
        List<EntityWorkflowTransition> transitions = records.stream()
                .filter(r -> r.get(ENTITY_WORKFLOW_TRANSITION.WORKFLOW_ID) != null)
                .map(r -> EntityWorkflowTransitionDao.TO_DOMAIN_MAPPER.map(r.into(ENTITY_WORKFLOW_TRANSITION))).distinct()
                .collect(Collectors.toList());

        List<EntityReference> results = records.stream()
                .filter(r -> r.get(ENTITY_WORKFLOW_RESULT.WORKFLOW_ID) != null)
                .map(r -> EntityWorkflowResultDao.TO_DOMAIN_MAPPER.map(r.into(ENTITY_WORKFLOW_RESULT)).resultEntity())
                .collect(Collectors.toList());

        return ImmutableEntityWorkflowView.builder()
                .workflowDefinition(definition)
                .workflowState(state)
                .workflowTransitionList(transitions)
                .entityWorkflowResultList(results)
                .build();
    }
}
