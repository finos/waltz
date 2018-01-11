package com.khartec.waltz.data.entity_workflow;


import com.khartec.waltz.model.entity_workflow.EntityWorkflowDefinition;
import com.khartec.waltz.model.entity_workflow.ImmutableEntityWorkflowDefinition;
import com.khartec.waltz.schema.tables.records.EntityWorkflowDefinitionRecord;
import org.jooq.DSLContext;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.schema.tables.EntityWorkflowDefinition.ENTITY_WORKFLOW_DEFINITION;

@Repository
public class EntityWorkflowDefinitionDao {

    private static final RecordMapper<? super EntityWorkflowDefinitionRecord, EntityWorkflowDefinition> TO_DOMAIN_MAPPER = r ->
            ImmutableEntityWorkflowDefinition
                    .builder()
                    .id(r.getId())
                    .name(r.getName())
                    .description(r.getDescription())
                    .build();


    private final DSLContext dsl;


    @Autowired
    public EntityWorkflowDefinitionDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<EntityWorkflowDefinition> findAll() {
        return dsl.selectFrom(ENTITY_WORKFLOW_DEFINITION)
                .fetch(TO_DOMAIN_MAPPER);
    }
}
