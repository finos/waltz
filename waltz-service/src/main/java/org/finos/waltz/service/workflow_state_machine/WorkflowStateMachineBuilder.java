package org.finos.waltz.service.workflow_state_machine;

import org.finos.waltz.data.entity_workflow.EntityWorkflowStateDao;
import org.finos.waltz.data.entity_workflow.EntityWorkflowTransitionDao;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;


public class WorkflowStateMachineBuilder<S extends Enum<S>, A extends Enum<A>, C extends WorkflowContext> {
    private final Set<WorkflowStateTransition<S, A, C>> transitions = new HashSet<>();
    private final EntityWorkflowStateDao stateDao;
    private final EntityWorkflowTransitionDao transitionDao;
    private Class<S> stateClass;

    public WorkflowStateMachineBuilder(EntityWorkflowStateDao stateDao, EntityWorkflowTransitionDao transitionDao) {
        this.stateDao = stateDao;
        this.transitionDao = transitionDao;
    }

    public WorkflowStateMachineBuilder<S, A, C> permit(S from, S to, A action) {
        return permit(from, to, action, c -> true); // Default condition is always true
    }

    public WorkflowStateMachineBuilder<S, A, C> permit(S from, S to, A action, Predicate<C> condition) {
        // By default, provide a no-op listener
        return permit(from, to, action, condition, (f, t, c) -> {
        });
    }

    public WorkflowStateMachineBuilder<S, A, C> permit(S from, S to, A action, Predicate<C> condition, WorkflowTransitionListener<S, C> listener) {
        if (this.stateClass == null && from != null) {
            this.stateClass = from.getDeclaringClass();
        }
        this.transitions.add(new WorkflowStateTransition<>(from, to, action, condition, listener));
        return this;
    }

    public WorkflowStateMachine<S, A, C> build() {
        if (stateClass == null) {
            throw new IllegalStateException("Cannot build a state machine with no transitions. At least one 'permit' call must be made.");
        }
        return new WorkflowStateMachine<>(this.stateClass, this.transitions, stateDao, transitionDao);
    }
}
