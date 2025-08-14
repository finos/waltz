package org.finos.waltz.service.workflow_state_machine;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Represents a single potential transition from one state to another.
 *
 * @param <C> The type of the context object.
 */
public class WorkflowStateTransition<S extends Enum<S>, A extends Enum<A>, C extends WorkflowContext> {
    private final S fromState;
    private final S toState;
    private final A action;
    /**
     * Transition happens only when the condition returns true
     */
    private final Predicate<C> condition;
    //TODO.. multiple listeners? Future/Callback?
    private final WorkflowTransitionListener<S, C> listener;

    public WorkflowStateTransition(S fromState,
                                   S toState,
                                   A action,
                                   Predicate<C> condition,
                                   WorkflowTransitionListener<S, C> listener) {
        this.fromState = Objects.requireNonNull(fromState);
        this.toState = Objects.requireNonNull(toState);
        this.action = Objects.requireNonNull(action);
        this.condition = Objects.requireNonNull(condition);
        this.listener = Objects.requireNonNull(listener);
    }

    // Getters...
    public S getFromState() {
        return fromState;
    }

    public S getToState() {
        return toState;
    }

    public A getAction() {
        return action;
    }

    public Predicate<C> getCondition() {
        return condition;
    }

    public WorkflowTransitionListener<S, C> getListener() {
        return listener;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        WorkflowStateTransition<?, ?, ?> that = (WorkflowStateTransition<?, ?, ?>) o;
        return Objects.equals(fromState, that.fromState) && Objects.equals(toState, that.toState) && Objects.equals(action, that.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromState, toState, action);
    }
}
