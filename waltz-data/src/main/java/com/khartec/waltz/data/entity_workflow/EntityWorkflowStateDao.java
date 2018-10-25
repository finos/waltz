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
