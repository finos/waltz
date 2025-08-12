package org.finos.waltz.service.workflow_state_machine.proposed_flow;

import org.finos.waltz.data.entity_workflow.EntityWorkflowStateDao;
import org.finos.waltz.data.entity_workflow.EntityWorkflowTransitionDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState;
import org.finos.waltz.service.workflow_state_machine.WorkflowDefinition;
import org.finos.waltz.service.workflow_state_machine.WorkflowStateMachine;
import org.finos.waltz.service.workflow_state_machine.WorkflowTransitionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

import static org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState.*;
import static org.finos.waltz.service.workflow_state_machine.proposed_flow.ProposedFlowWorkflowTransitionAction.*;

/**
 * A concrete implementation of a workflow definition for the PROPOSED_FLOW entity kind.
 * This class defines all the states, actions, and transition rules for this specific workflow.
 */
@Component
public class ProposedFlowWorkflowDefinition implements WorkflowDefinition<ProposedFlowWorkflowState, ProposedFlowWorkflowTransitionAction, ProposedFlowWorkflowContext> {
    private WorkflowStateMachine workflowStateMachine;

    private final EntityWorkflowStateDao stateDao;
    private final EntityWorkflowTransitionDao transitionDao;
    // FILTERS
    private static Predicate<ProposedFlowWorkflowContext> isSourceApprover = ctx -> false/*ctx.userHasRole("SOURCE_APPROVER")*/;
    private static Predicate<ProposedFlowWorkflowContext> isTargetApprover = ctx -> false/*ctx.userHasRole("TARGET_APPROVER")*/;
    private static Predicate<ProposedFlowWorkflowContext> canSourceFullyApprove = isSourceApprover
            .and(ctx -> ctx.getPreviousState() == TARGET_APPROVED);
    private static Predicate<ProposedFlowWorkflowContext> canTargetFullyApprove = isTargetApprover
            .and(ctx -> ctx.getPreviousState() == SOURCE_APPROVED);

    // TODO.. might have to refresh the context object, the state values can be stale
    private static WorkflowTransitionListener<ProposedFlowWorkflowState, ProposedFlowWorkflowContext> fullyApprovedTransitionListener = (from, to, ctx) ->
            System.out.printf(
                    "LISTENER: User '%s' was notified of transition from %s -> %s for entity %d%n",
                    /*ctx.getUserId(),*/ from, to, ctx.getEntityId()
            );

    @Autowired
    public ProposedFlowWorkflowDefinition(EntityWorkflowStateDao stateDao, EntityWorkflowTransitionDao transitionDao) {
        this.stateDao = stateDao;
        this.transitionDao = transitionDao;
    }

    @Override
    public EntityKind getEntityKind() {
        return EntityKind.PROPOSED_FLOW;
    }

    private WorkflowStateMachine<ProposedFlowWorkflowState, ProposedFlowWorkflowTransitionAction, ProposedFlowWorkflowContext> build() {
        WorkflowStateMachine.WorkflowStateMachineBuilder builder = WorkflowStateMachine.builder(stateDao, transitionDao)
                .permit(PROPOSED_CREATE, PENDING_APPROVALS, PROPOSE)
                // PENDING_APPROVAL transitions
                .permit(PENDING_APPROVALS, SOURCE_APPROVED, APPROVE,
                        isSourceApprover)
                .permit(PENDING_APPROVALS, TARGET_APPROVED, APPROVE,
                        isTargetApprover)
                .permit(PENDING_APPROVALS, SOURCE_REJECTED, REJECT,
                        isSourceApprover)
                .permit(PENDING_APPROVALS, TARGET_REJECTED, REJECT,
                        isTargetApprover)

                // SOURCE_APPROVED transitions
                .permit(SOURCE_APPROVED, FULLY_APPROVED, APPROVE,
                        canSourceFullyApprove, fullyApprovedTransitionListener)
                .permit(SOURCE_APPROVED, TARGET_APPROVED, APPROVE,
                        isTargetApprover)
                .permit(SOURCE_APPROVED, TARGET_REJECTED, REJECT,
                        isTargetApprover)

                // TARGET_APPROVED transitions
                .permit(TARGET_APPROVED, FULLY_APPROVED, APPROVE,
                        canTargetFullyApprove, fullyApprovedTransitionListener)
                .permit(TARGET_APPROVED, SOURCE_APPROVED, APPROVE,
                        isSourceApprover)
                .permit(TARGET_APPROVED, SOURCE_REJECTED, REJECT,
                        isSourceApprover);

        return builder.build();
    }

    public WorkflowStateMachine getMachine() {
        if (workflowStateMachine == null) {
            workflowStateMachine = build();
        }
        return this.workflowStateMachine;
    }
}
