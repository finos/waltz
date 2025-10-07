package org.finos.waltz.service.workflow_state_machine.proposed_flow;

import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState;
import org.finos.waltz.service.workflow_state_machine.WorkflowContext;

public class ProposedFlowWorkflowContext extends WorkflowContext {
  private ProposedFlowWorkflowState currentState;
  private ProposedFlowWorkflowState prevState;
  private boolean isSourceApprover;
  private boolean isTargetApprover;

  public ProposedFlowWorkflowContext(
      long workflowId, EntityReference entityReference, String userId, String reason) {
    super(workflowId, entityReference, userId, reason);
  }

  public ProposedFlowWorkflowState getPrevState() {
    return prevState;
  }

  public ProposedFlowWorkflowContext setPrevState(ProposedFlowWorkflowState prevState) {
    this.prevState = prevState;
    return this;
  }

  public ProposedFlowWorkflowState getCurrentState() {
    return currentState;
  }

  public ProposedFlowWorkflowContext setCurrentState(ProposedFlowWorkflowState currentState) {
    this.currentState = currentState;
    return this;
  }

  public boolean isSourceApprover() {
    return isSourceApprover;
  }

  public ProposedFlowWorkflowContext setSourceApprover(boolean sourceApprover) {
    isSourceApprover = sourceApprover;
    return this;
  }

  public boolean isTargetApprover() {
    return isTargetApprover;
  }

  public ProposedFlowWorkflowContext setTargetApprover(boolean targetApprover) {
    isTargetApprover = targetApprover;
    return this;
  }
}
