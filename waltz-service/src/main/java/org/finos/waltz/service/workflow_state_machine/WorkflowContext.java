package org.finos.waltz.service.workflow_state_machine;

import org.finos.waltz.model.EntityReference;

/**
 * A generic context object passed to predicates and listeners.
 * This should contain all information necessary to evaluate a transition.
 */
public class WorkflowContext {
    //workflow_id, entity_id, entity_kind uniquely identifies a record in the entity_workflow_state
    private final long workflowId;
    private final EntityReference entityReference;
    private final String userId;
    private final String reason; // Reason for the transition

    public WorkflowContext(long workflowId, EntityReference entityReference, String userId, String reason) {
        this.workflowId = workflowId;
        this.entityReference = entityReference;
        this.userId = userId;
        this.reason = reason;
    }

    public long getWorkflowId() {
        return workflowId;
    }

    public String getUserId() {
        return userId;
    }

    public String getReason() {
        return reason;
    }

    public EntityReference getEntityReference() {
        return entityReference;
    }
}
