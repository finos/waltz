package org.finos.waltz.service.workflow_state_machine.proposed_flow;

import org.finos.waltz.data.entity_workflow.EntityWorkflowStateDao;
import org.finos.waltz.data.entity_workflow.EntityWorkflowTransitionDao;
import org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState;
import org.finos.waltz.service.workflow_state_machine.WorkflowStateMachine;
import org.finos.waltz.service.workflow_state_machine.exception.TransitionNotFoundException;
import org.finos.waltz.service.workflow_state_machine.exception.TransitionPredicateFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState.PENDING_APPROVALS;
import static org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState.PROPOSED_CREATE;
import static org.finos.waltz.service.workflow_state_machine.proposed_flow.ProposedFlowWorkflowTransitionAction.APPROVE;
import static org.finos.waltz.service.workflow_state_machine.proposed_flow.ProposedFlowWorkflowTransitionAction.PROPOSE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ProposedFlowWorkflowDefinitionTest {

    @Mock
    private EntityWorkflowStateDao stateDao;
    @Mock
    private EntityWorkflowTransitionDao transitionDao;

    private ProposedFlowWorkflowDefinition definition;
    private WorkflowStateMachine<ProposedFlowWorkflowState, ProposedFlowWorkflowTransitionAction, ProposedFlowWorkflowContext> machine;
    private ProposedFlowWorkflowContext mockContext;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Instantiate the class under test with its mocked dependencies
        definition = new ProposedFlowWorkflowDefinition(stateDao, transitionDao);

        // Get the state machine instance to test its configured behavior
        machine = definition.getMachine();

        // Create a mock context for the tests
        mockContext = mock(ProposedFlowWorkflowContext.class);
    }

    @Test
    void testSuccessfulInitialTransition() throws TransitionNotFoundException, TransitionPredicateFailedException {
        ProposedFlowWorkflowState newState = machine.fire(PROPOSED_CREATE, PROPOSE, mockContext);
        assertEquals(PENDING_APPROVALS, newState);
    }

    @Test
    void testUnsupportedTransitionException() {
        // The APPROVE action is not defined for the PROPOSED_CREATE state
        assertThrows(
                TransitionNotFoundException.class,
                () -> machine.fire(PROPOSED_CREATE, APPROVE, mockContext),
                "Should throw an exception for an unsupported transition");
    }

    @Test
    void testTransitionPredicateFailedException() {
        // All transitions from PENDING_APPROVALS have predicates that are currently hardcoded to return `false`.
        // Therefore, any attempt to transition should fail the predicate check.
        assertThrows(
                TransitionPredicateFailedException.class,
                () -> machine.fire(PENDING_APPROVALS, APPROVE, mockContext),
                "Should throw predicate failed exception because the hardcoded predicate returns false");
    }

    @Test
    void testGetMachineIsIdempotent() {
        WorkflowStateMachine<?, ?, ?> machine1 = definition.getMachine();
        WorkflowStateMachine<?, ?, ?> machine2 = definition.getMachine();
        // Should return the same cached instance, not a new one
        assertSame(machine1, machine2, "getMachine() should return the same instance on subsequent calls");
    }

    // TODO.. following test cases need to be updated when real predicates are defined
    @Test
    void testSourceApprovedTransition_UserIsSourceApprover() throws TransitionNotFoundException, TransitionPredicateFailedException {
        // context need to be set with correct attributes/values for transition predicate to return true
//        ProposedFlowWorkflowContext context = new ProposedFlowWorkflowContext();
//        ProposedFlowWorkflowState newState = machine.fire(PENDING_APPROVALS, APPROVE, );
//        assertEquals(SOURCE_APPROVED, newState);
    }

    @Test
    void testTargetApprovedTransition_UserIsTargetApprover() throws TransitionNotFoundException, TransitionPredicateFailedException {
        // context need to be set with correct attributes/values for transition predicate to return true
//        ProposedFlowWorkflowContext context = new ProposedFlowWorkflowContext();
//        ProposedFlowWorkflowState newState = machine.fire(PENDING_APPROVALS, APPROVE, );
//        assertEquals(TARGET_APPROVED, newState);
    }
}