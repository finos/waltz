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
 * A concrete implementation of a {@link WorkflowDefinition} for the {@link EntityKind#PROPOSED_FLOW} entity kind.
 * <p>
 * This class defines all the states, actions, and transition rules for this specific workflow.
 *
 * <h3>Workflow Transitions:</h3>
 * <ul>
 *     <li>{@code PROPOSED_CREATE -> PENDING_APPROVALS}</li>
 *     <li>{@code PENDING_APPROVALS -> SOURCE_APPROVED}</li>
 *     <li>{@code PENDING_APPROVALS -> TARGET_APPROVED}</li>
 *     <li>{@code PENDING_APPROVALS -> SOURCE_REJECTED}</li>
 *     <li>{@code PENDING_APPROVALS -> TARGET_REJECTED}</li>
 *     <li>{@code SOURCE_APPROVED -> TARGET_REJECTED}</li>
 *     <li>{@code SOURCE_APPROVED -> FULLY_APPROVED}</li>
 *     <li>{@code TARGET_APPROVED -> SOURCE_REJECTED}</li>
 *     <li>{@code TARGET_APPROVED -> FULLY_APPROVED}</li>
 * </ul>
 *
 * @see ProposedFlowWorkflowState
 * @see ProposedFlowWorkflowTransitionAction
 */
@Component
public class ProposedFlowWorkflowDefinition implements WorkflowDefinition<ProposedFlowWorkflowState, ProposedFlowWorkflowTransitionAction, ProposedFlowWorkflowContext> {
    private final EntityWorkflowStateDao stateDao;
    private final EntityWorkflowTransitionDao transitionDao;
    // FILTERS
    private static final Predicate<ProposedFlowWorkflowContext> isSourceApprover = ctx -> ctx.isSourceApprover();
    private static final Predicate<ProposedFlowWorkflowContext> isTargetApprover = ctx -> ctx.isTargetApprover();
    private static final Predicate<ProposedFlowWorkflowContext> canSourceFullyApprove = isSourceApprover
            .and(ctx -> ctx.getCurrentState() == TARGET_APPROVED);
    private static final Predicate<ProposedFlowWorkflowContext> canTargetFullyApprove = isTargetApprover
            .and(ctx -> ctx.getCurrentState() == SOURCE_APPROVED);

    // Refreshing the context object might be needed as the state values can be stale
    private static final WorkflowTransitionListener<ProposedFlowWorkflowState, ProposedFlowWorkflowContext> fullyApprovedTransitionListener = (from, to, ctx) ->
            System.out.printf(
                    "LISTENER: User '%s' was notified of transition from %s -> %s for entity %s%n",
                    ctx.getUserId(), from, to, ctx.getEntityReference()
            );

    private WorkflowStateMachine workflowStateMachine;

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
                        canTargetFullyApprove, fullyApprovedTransitionListener)
                .permit(SOURCE_APPROVED, TARGET_REJECTED, REJECT,
                        isTargetApprover)

                // TARGET_APPROVED transitions
                .permit(TARGET_APPROVED, FULLY_APPROVED, APPROVE,
                        canSourceFullyApprove, fullyApprovedTransitionListener)
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
