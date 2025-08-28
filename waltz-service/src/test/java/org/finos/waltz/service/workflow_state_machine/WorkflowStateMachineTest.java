package org.finos.waltz.service.workflow_state_machine;

import org.finos.waltz.data.entity_workflow.EntityWorkflowStateDao;
import org.finos.waltz.data.entity_workflow.EntityWorkflowTransitionDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.ImmutableEntityReference;
import org.finos.waltz.service.workflow_state_machine.exception.TransitionNotFoundException;
import org.finos.waltz.service.workflow_state_machine.exception.TransitionPredicateFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.function.Predicate;

import static org.finos.waltz.service.workflow_state_machine.WorkflowStateMachineTest.WorkflowStateMachineState.*;
import static org.finos.waltz.service.workflow_state_machine.WorkflowStateMachineTest.WorkflowStateMachineTransitionAction.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class WorkflowStateMachineTest {
    @Mock
    private static EntityWorkflowStateDao stateDao;
    @Mock
    private static EntityWorkflowTransitionDao transitionDao;
    @Mock
    private WorkflowTransitionListener<WorkflowStateMachineState, WorkflowStateMachineContext> listener;

    private WorkflowStateMachine<WorkflowStateMachineState, WorkflowStateMachineTransitionAction, WorkflowStateMachineContext> machine;
    private WorkflowStateMachineContext context;
    private EntityReference entityRef = ImmutableEntityReference.builder()
            .kind(EntityKind.APPLICATION)
            .id(1L)
            .build();

    protected enum WorkflowStateMachineState {
        START,
        S1,
        S2A,
        S2B,
        S3,
        STOP
    }

    protected enum WorkflowStateMachineTransitionAction {
        BEGIN,
        NEXT,
        BACK
    }

    class WorkflowStateMachineContext extends WorkflowContext {
        public WorkflowStateMachineContext() {
            super(1L, entityRef, "test_user", "Initial context reason");
        }
    }

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        context = new WorkflowStateMachineContext();

        Predicate<WorkflowStateMachineContext> isUserNull = ctx -> ctx.getUserId() == null;
        machine = WorkflowStateMachine.builder(stateDao, transitionDao)
                .permit(START, S1, BEGIN)
                .permit(S1, S2A, NEXT)
                .permit(S2A, S1, BACK, isUserNull)
                .permit(S1, S2B, NEXT) // this can never be reached as S2A should take priority over S2B
                .permit(S2A, S3, NEXT)
                .permit(S2B, S3, NEXT)
                .permit(S2B, S1, BACK)
                .permit(S3, STOP, NEXT, listener)
                .build();
    }

    @Test
    public void testUnsupportedTransitionException() {
        // Verify that an invalid action throws the expected exception.
        // According to the setup, the `NEXT` action is not permitted from the `START` state.
        assertThrows(
                TransitionNotFoundException.class,
                () -> machine.fire(START, NEXT, context),
                "Should throw an exception for an unsupported transition");
    }

    @Test
    public void testTransitionPredicateFailedException() {
        // The default context has a non-null user, so the 'isUserNull' predicate will fail.
        assertThrows(
                TransitionPredicateFailedException.class,
                () -> machine.fire(S2A, BACK, context),
                "Should throw predicate failed exception when condition is not met");
    }

    @Test
    public void testSuccessfulTransition() throws TransitionNotFoundException, TransitionPredicateFailedException {
        WorkflowStateMachineState newState = machine.fire(START, BEGIN, context);
        assertEquals(S1, newState, "State should transition from START to S1");
    }

    @Test
    public void testUnreachableTransition() throws TransitionNotFoundException, TransitionPredicateFailedException {
        // Both (S1, S2A, NEXT) and (S1, S2B, NEXT) are defined.
        // The state machine should pick the first one it finds (S2A).
        WorkflowStateMachineState newState = machine.fire(S1, NEXT, context);

        assertEquals(S2A, newState, "Should transition to the first defined state S2A");
        assertNotEquals(S2B, newState, "Should not transition to the second, unreachable state S2B");
    }

    @Test
    public void testSuccessfulTransition_ListenerInvocation() throws TransitionNotFoundException, TransitionPredicateFailedException {
        // The listener is attached to the (S3, STOP, NEXT) transition.
        machine.fire(S3, NEXT, context);

        // Verify that the listener's onTransition method was called exactly once with the correct parameters.
        verify(listener, times(1)).onTransition(S3, STOP, context);
    }
}
