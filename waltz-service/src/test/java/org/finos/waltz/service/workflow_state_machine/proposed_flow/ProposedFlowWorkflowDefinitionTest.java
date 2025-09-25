package org.finos.waltz.service.workflow_state_machine.proposed_flow;

import org.finos.waltz.data.entity_workflow.EntityWorkflowStateDao;
import org.finos.waltz.data.entity_workflow.EntityWorkflowTransitionDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.ImmutableEntityReference;
import org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState;
import org.finos.waltz.service.workflow_state_machine.WorkflowStateMachine;
import org.finos.waltz.service.workflow_state_machine.exception.TransitionNotFoundException;
import org.finos.waltz.service.workflow_state_machine.exception.TransitionPredicateFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState.*;
import static org.finos.waltz.service.workflow_state_machine.proposed_flow.ProposedFlowWorkflowTransitionAction.*;
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
    private final EntityReference entityRef = ImmutableEntityReference.builder()
            .kind(EntityKind.PROPOSED_FLOW)
            .id(1L)
            .build();

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

    @Test
    void testSourceApprovedTransition_FromPendingApprovals() throws TransitionNotFoundException, TransitionPredicateFailedException {
        ProposedFlowWorkflowContext context = createContext(true, false, PENDING_APPROVALS);
        ProposedFlowWorkflowState newState = machine.fire(PENDING_APPROVALS, APPROVE, context);
        assertEquals(SOURCE_APPROVED, newState);
    }

    @Test
    void testSourceApprovedTransition_FromTargetApproved() throws TransitionNotFoundException, TransitionPredicateFailedException {
        ProposedFlowWorkflowContext context = createContext(true, false, TARGET_APPROVED);
        ProposedFlowWorkflowState newState = machine.fire(TARGET_APPROVED, APPROVE, context);
        assertEquals(SOURCE_APPROVED, newState);
    }

    @Test
    void testTargetApprovedTransition_FromPendingApprovals() throws TransitionNotFoundException, TransitionPredicateFailedException {
        ProposedFlowWorkflowContext context = createContext(false, true, PENDING_APPROVALS);
        ProposedFlowWorkflowState newState = machine.fire(PENDING_APPROVALS, APPROVE, context);
        assertEquals(TARGET_APPROVED, newState);
    }

    @Test
    void testTargetApprovedTransition_FromSourceApproved() throws TransitionNotFoundException, TransitionPredicateFailedException {
        ProposedFlowWorkflowContext context = createContext(false, true, SOURCE_APPROVED);
        ProposedFlowWorkflowState newState = machine.fire(SOURCE_APPROVED, APPROVE, context);
        assertEquals(TARGET_APPROVED, newState);
    }

    @Test
    void testFullyApprovedTransition_UserIsSourceApprover() throws TransitionNotFoundException, TransitionPredicateFailedException {
        ProposedFlowWorkflowState currentState = SOURCE_APPROVED;
        ProposedFlowWorkflowContext context = createContext(true, false, currentState)
                .setCurrentState(currentState)
                .setPrevState(TARGET_APPROVED);
        ProposedFlowWorkflowState newState = machine.fire(currentState, APPROVE, context);
        assertEquals(FULLY_APPROVED, newState);
    }

    @Test
    void testFullyApprovedTransition_UserIsTargetApprover() throws TransitionNotFoundException, TransitionPredicateFailedException {
        ProposedFlowWorkflowState currentState = TARGET_APPROVED;
        ProposedFlowWorkflowContext context = createContext(false, true, currentState)
                .setCurrentState(currentState)
                .setPrevState(SOURCE_APPROVED);
        ProposedFlowWorkflowState newState = machine.fire(currentState, APPROVE, context);
        assertEquals(FULLY_APPROVED, newState);
    }

    @Test
    void testSourceApprovedToTargetRejected() throws TransitionNotFoundException, TransitionPredicateFailedException {
        ProposedFlowWorkflowContext context = createContext(false, true, SOURCE_APPROVED);
        ProposedFlowWorkflowState newState = machine.fire(SOURCE_APPROVED, REJECT, context);
        assertEquals(TARGET_REJECTED, newState);
    }

    @Test
    void testTargetApprovedToSourceRejected() throws TransitionNotFoundException, TransitionPredicateFailedException {
        ProposedFlowWorkflowContext context = createContext(true, false, TARGET_APPROVED);
        ProposedFlowWorkflowState newState = machine.fire(TARGET_APPROVED, REJECT, context);
        assertEquals(SOURCE_REJECTED, newState);
    }

    @Test
    void testPendingApprovalsToSourceRejected() throws TransitionNotFoundException, TransitionPredicateFailedException {
        ProposedFlowWorkflowContext context = createContext(true, false, PENDING_APPROVALS);
        ProposedFlowWorkflowState newState = machine.fire(PENDING_APPROVALS, REJECT, context);
        assertEquals(SOURCE_REJECTED, newState);
    }

    @Test
    void testPendingApprovalsToTargetRejected() throws TransitionNotFoundException, TransitionPredicateFailedException {
        ProposedFlowWorkflowContext context = createContext(false, true, PENDING_APPROVALS);
        ProposedFlowWorkflowState newState = machine.fire(PENDING_APPROVALS, REJECT, context);
        assertEquals(TARGET_REJECTED, newState);
    }

    private ProposedFlowWorkflowContext createContext(boolean isSourceApprover, boolean isTargetApprover, ProposedFlowWorkflowState currentState) {
        return new ProposedFlowWorkflowContext(1L, entityRef, "test_user", "test reason")
                .setSourceApprover(isSourceApprover)
                .setTargetApprover(isTargetApprover)
                .setCurrentState(currentState);
    }
}