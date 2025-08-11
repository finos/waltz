package org.finos.waltz.service.workflow_state_machine;

// --- CONTEXT ---

/**
 * A generic context object passed to predicates and listeners.
 * This should contain all information necessary to evaluate a transition.
 */
public class WorkflowContext<S extends Enum<S>> {
    //workflow_id, entity_id, entity_kind uniquely identifies a record in the entity_workflow_state
    private final long workflowDefId;
    private final long entityId;
    private final String entityKind;
    private final String userId;
    private final String reason; // Reason for the transition

    public WorkflowContext(long workflowDefId, long entityId, String entityKind, String userId, String reason) {
        this.workflowDefId = workflowDefId;
        this.entityId = entityId;
        this.entityKind = entityKind;
        this.userId = userId;
        this.reason = reason;
    }

    // Getters...
    public long getWorkflowDefId() {
        return workflowDefId;
    }

    public long getEntityId() {
        return entityId;
    }

    public String getEntityKind() {
        return entityKind;
    }

    public String getUserId() {
        return userId;
    }

    public String getReason() {
        return reason;
    }
}
