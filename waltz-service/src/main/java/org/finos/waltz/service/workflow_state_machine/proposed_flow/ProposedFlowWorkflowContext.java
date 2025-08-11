package org.finos.waltz.service.workflow_state_machine.proposed_flow;

import org.finos.waltz.service.workflow_state_machine.WorkflowContext;

public class ProposedFlowWorkflowContext extends WorkflowContext<ProposedFlowWorkflowState> {
    private ProposedFlowWorkflowState previousState;
    private boolean isSourceApprover;
    private String userRole;

    public ProposedFlowWorkflowContext(long workflowId, long entityId, String entityKind, String userId, String reason) {
        super(workflowId, entityId, entityKind, userId, reason);
    }

    public ProposedFlowWorkflowState getPreviousState() {
        return previousState;
    }

    public void setPreviousState(ProposedFlowWorkflowState previousState) {
        this.previousState = previousState;
    }
}
