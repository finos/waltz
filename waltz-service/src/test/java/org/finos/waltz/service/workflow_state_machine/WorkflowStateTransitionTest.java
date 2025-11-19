package org.finos.waltz.service.workflow_state_machine;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.ImmutableEntityReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WorkflowStateTransitionTest {
    public final TestContext CONTEXT = new TestContext();
    @Mock
    private WorkflowTransitionListener mockListener;
    @Mock
    private Predicate<TestContext> mockPredicate;

    private EntityReference entityRef = ImmutableEntityReference.builder()
            .kind(EntityKind.APPLICATION)
            .id(1L)
            .build();

    private enum TestState {
        A, B
    }

    private enum TestAction {
        GO
    }

    class TestContext extends WorkflowContext {
        public TestContext() {
            super(1L, entityRef, "test_user", "Test context reason");
        }
    }

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void constructorShouldSetAllProperties() {
        // Arrange
        Predicate<TestContext> mockPredicate = ctx -> true;
        WorkflowTransitionListener<TestState, TestContext> mockListener = (from, to, ctx) -> {
        };

        // Act
        WorkflowStateTransition<TestState, TestAction, TestContext> transition = new WorkflowStateTransition<>(
                TestState.A,
                TestState.B,
                TestAction.GO,
                mockPredicate,
                mockListener);

        // Assert
        assertEquals(TestState.A, transition.getFromState(), "From state should be set correctly");
        assertEquals(TestState.B, transition.getToState(), "To state should be set correctly");
        assertEquals(TestAction.GO, transition.getAction(), "Action should be set correctly");
        assertSame(mockPredicate, transition.getCondition(), "Predicate should be the same instance as provided");
        assertSame(mockListener, transition.getListener(), "Listener should be the same instance as provided");
    }

    @Test
    public void customPredicateShouldBeEvaluated() {
        // Arrange
        WorkflowStateTransition<TestState, TestAction, TestContext> transition = new WorkflowStateTransition<TestState, TestAction, TestContext>(
                TestState.A,
                TestState.B,
                TestAction.GO,
                mockPredicate,
                mockListener);

        // Act
        when(mockPredicate.test(CONTEXT)).thenReturn(true);

        // Assert
        assertTrue(transition.getCondition().test(CONTEXT), "Predicate should return true when mock is configured to do so");
        verify(mockPredicate, times(1)).test(CONTEXT);
    }


    @Test
    public void customListenerShouldBeInvoked() {
        // Arrange
        WorkflowStateTransition<TestState, TestAction, TestContext> transition = new WorkflowStateTransition<>(
                TestState.A,
                TestState.B,
                TestAction.GO,
                mockPredicate,
                mockListener);

        // Act
        transition.getListener().onTransition(TestState.A, TestState.B, CONTEXT);

        // Assert
        verify(mockListener, times(1)).onTransition(TestState.A, TestState.B, CONTEXT);
    }
}