package com.khartec.waltz.data.entity_workflow;


import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.entity_workflow.EntityWorkflowTransition;
import com.khartec.waltz.model.entity_workflow.ImmutableEntityWorkflowTransition;
import com.khartec.waltz.schema.tables.records.EntityWorkflowTransitionRecord;
import org.jooq.DSLContext;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.EntityWorkflowTransition.ENTITY_WORKFLOW_TRANSITION;

@Repository
public class EntityWorkflowTransitionDao {

    private static final RecordMapper<? super EntityWorkflowTransitionRecord, EntityWorkflowTransition> TO_DOMAIN_MAPPER = r ->
            ImmutableEntityWorkflowTransition
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


    private final DSLContext dsl;


    @Autowired
    public EntityWorkflowTransitionDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<EntityWorkflowTransition> findForEntityReferenceAndWorkflowId(long workflowId, EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");

        return dsl.selectFrom(ENTITY_WORKFLOW_TRANSITION)
                .where(ENTITY_WORKFLOW_TRANSITION.WORKFLOW_ID.eq(workflowId))
                .and(ENTITY_WORKFLOW_TRANSITION.ENTITY_ID.eq(ref.id()))
                .and(ENTITY_WORKFLOW_TRANSITION.ENTITY_KIND.eq(ref.kind().name()))
                .fetch(TO_DOMAIN_MAPPER);
    }
}
