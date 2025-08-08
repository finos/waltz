//package org.finos.waltz.service.proposed_flow;
//
//import org.finos.waltz.data.entity_workflow.EntityWorkflowStateDao;
//import org.finos.waltz.model.EntityKind;
//import org.finos.waltz.model.EntityReference;
//import org.finos.waltz.model.state_machine.MakerCheckerState;
//import org.finos.waltz.model.state_machine.WorkflowStateMachine;
//import org.finos.waltz.model.state_machine.exception.TransitionNotFoundException;
//import org.finos.waltz.model.state_machine.exception.TransitionPredicateFailedException;
//import org.finos.waltz.model.state_machine.proposed_flow.ProposedFlowAction;
//import org.finos.waltz.model.state_machine.proposed_flow.ProposedFlowContext;
//import org.finos.waltz.model.state_machine.proposed_flow.ProposedWorkflowContext;
//import org.finos.waltz.model.user.User;
//import org.finos.waltz.service.workflow_state_machine.WorkflowRegistry;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//@Service
//public class ProposedFlowService {
//
//    private final WorkflowRegistry workflowRegistry;
//    private final EntityWorkflowStateDao entityWorkflowStateDao; // Assuming this DAO exists to get current state
//
//    @Autowired
//    public ProposedFlowService(WorkflowRegistry workflowRegistry, EntityWorkflowStateDao entityWorkflowStateDao) {
//        this.workflowRegistry = workflowRegistry;
//        this.entityWorkflowStateDao = entityWorkflowStateDao;
//    }
//
//    /**
//     * Approves a proposed flow for a given entity.
//     * @param entityRef The entity the flow belongs to
//     * @param approvingUser The user performing the approval
//     * @return The new state of the workflow
//     */
//    public MakerCheckerState approve(EntityReference entityRef, User approvingUser) {
//
//        // 1. GET THE STATE MACHINE
//        // Ask the registry for the correct machine for this entity kind.
//        // Note: The cast is necessary because the registry stores machines with wildcard generics.
//        WorkflowStateMachine<MakerCheckerState, ProposedFlowAction, ProposedFlowContext> machine =
//                (WorkflowStateMachine<MakerCheckerState, ProposedFlowAction, ProposedFlowContext>) workflowRegistry
//                        .getMachine(EntityKind.PROPOSED_FLOW);
//
//
//        // 2. GATHER CONTEXT
//        // Get the current state from the database.
//        MakerCheckerState currentState = entityWorkflowStateDao.getStateForEntity(entityRef);
//
//        // Create the context object with all data needed for predicates and listeners.
//        ProposedWorkflowContext context = new ProposedWorkflowContext(
//                entityRef,
//                "User approval",
//                approvingUser,
//                true,  // Example: determine if user is a source approver
//                false); // Example: determine if user is a target approver
//
//
//        // 3. FIRE THE ACTION
//        // Execute the action and handle all possible outcomes.
//        try {
//            MakerCheckerState newState = machine.fire(
//                    currentState,
//                    ProposedFlowAction.APPROVE,
//                    context);
//
//            System.out.println("Successfully transitioned entity " + entityRef.id() + " to state: " + newState);
//            return newState;
//
//        } catch (TransitionNotFoundException e) {
//            // This means there is no 'APPROVE' action from the 'currentState'.
//            // This is likely a UI or logic bug.
//            System.err.println("Error: " + e.getMessage());
//            throw new IllegalStateException("Cannot approve from state: " + currentState, e);
//        } catch (TransitionPredicateFailedException e) {
//            // This means the user does not have permission to perform this action.
//            // For example, the predicate `(ctx) -> ctx.isSourceApprover()` returned false.
//            System.err.println("Permission denied: " + e.getMessage());
//            throw new IllegalArgumentException("User does not have permission to approve this flow.", e);
//        } catch (IllegalStateException e) {
//            // This means something went wrong during the database update.
//            // This is a critical failure.
//            System.err.println("Persistence Error: " + e.getMessage());
//            throw e; // Re-throw the critical exception
//        }
//    }
//}
