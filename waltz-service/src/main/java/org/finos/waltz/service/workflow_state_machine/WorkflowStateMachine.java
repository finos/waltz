package org.finos.waltz.service.workflow_state_machine;

import org.finos.waltz.data.entity_workflow.EntityWorkflowStateDao;
import org.finos.waltz.data.entity_workflow.EntityWorkflowTransitionDao;
import org.finos.waltz.service.workflow_state_machine.exception.TransitionNotFoundException;
import org.finos.waltz.service.workflow_state_machine.exception.TransitionPredicateFailedException;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// --- STATE MACHINE ---

/**
 * The main state machine class.
 *
 * @param <S> The enum representing the states.
 * @param <A> The enum representing the actions that trigger transitions.
 * @param <C> The type of the context object, which must extend WorkflowContext.
 */
public class WorkflowStateMachine<S extends Enum<S>, A extends Enum<A>, C extends WorkflowContext> {
    private final Map<S, List<WorkflowStateTransition<S, A, C>>> transitionsByState;
    private final EntityWorkflowStateDao stateDao;
    private final EntityWorkflowTransitionDao transitionDao;

    private WorkflowStateMachine(Class<S> stateClass,
                                 Set<WorkflowStateTransition<S, A, C>> transitions,
                                 EntityWorkflowStateDao stateDao,
                                 EntityWorkflowTransitionDao transitionDao) {
        this.stateDao = Objects.requireNonNull(stateDao);
        this.transitionDao = Objects.requireNonNull(transitionDao);
        this.transitionsByState = new EnumMap<>(stateClass);
        for (WorkflowStateTransition<S, A, C> t : transitions) {
            this.transitionsByState
                    .computeIfAbsent(t.getFromState(), k -> new ArrayList<>())
                    .add(t);
        }
    }

    public S fire(S currentState, A action, C context) throws TransitionNotFoundException, TransitionPredicateFailedException {
        WorkflowStateTransition<S, A, C> transition = findFirstMatchingTransition(currentState, action, context);
        S toState = transition.getToState();

        try {
            //TODO.. currently the state machine will only return the next transition state and is not responsible for database updates
            // 1. Log the transition
//            transitionDao.createWorkflowTransition(
//                    context.getWorkflowDefId(),
//                    context.getEntityId(),
//                    context.getEntityKind(),
//                    currentState.name(),
//                    toState.name(),
//                    context.getReason(),
//                    context.getUserId(),
//                    "waltz"); // or some other provenance

            // 2. Update the current state; insert if it's a new state
            //TODO.. should be insert or update
//            stateDao.insertOrUpdate(
//                    context.getWorkflowId(),
//                    context.getEntityId(),
//                    context.getEntityKind(),
//                    toState.name(),
//                    context.getUserId(),
//                    "waltz",
//                    context.getReason());

            // 3. Execute supplementary listener only after successful persistence
            if (transition.getListener() != null) {
                //TODO.. refresh context to reflect new changes?
                transition.getListener().onTransition(currentState, toState, context);
            }
        } catch (Exception e) {
            // Wrap persistence exceptions in a runtime exception to signal a critical failure
            throw new IllegalStateException("Failed to persist state change for entity " + context.getEntityId() + ": " + e.getMessage(), e);
        }

        return toState;
    }

    private WorkflowStateTransition<S, A, C> findFirstMatchingTransition(S currentState, A action, C context) throws TransitionNotFoundException, TransitionPredicateFailedException {
        List<WorkflowStateTransition<S, A, C>> possibleTransitions = transitionsByState.getOrDefault(currentState, Collections.emptyList())
                .stream()
                .filter(t -> t.getAction() == action)
                .collect(Collectors.toList());

        if (possibleTransitions.isEmpty()) {
            throw new TransitionNotFoundException(String.format(
                    "No transition definition found for state '%s' and action '%s' for entity %s",
                    currentState,
                    action,
                    context.getEntityId()));
        }

        Optional<WorkflowStateTransition<S, A, C>> passingTransition = possibleTransitions.stream()
                .filter(t -> t.getCondition().test(context))
                .findFirst();

        return passingTransition.orElseThrow(() -> new TransitionPredicateFailedException(String.format(
                "A transition for state '%s' and action '%s' exists for entity %s, but its predicate failed",
                currentState,
                action,
                context.getEntityId())));
    }

    public static WorkflowStateMachineBuilder builder(EntityWorkflowStateDao stateDao, EntityWorkflowTransitionDao transitionDao) {
        return new WorkflowStateMachineBuilder(stateDao, transitionDao);
    }

    public static class WorkflowStateMachineBuilder<S extends Enum<S>, A extends Enum<A>, C extends WorkflowContext> {
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
}
