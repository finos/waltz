package org.finos.waltz.service.workflow_state_machine;

import org.finos.waltz.model.EntityKind;

/**
 * Defines a contract for any class that provides a state machine for a given entity kind.
 * Implementations of this interface are discovered by the {@link WorkflowRegistry}.
 *
 * @param <S> The enum representing the states of the workflow.
 * @param <A> The type representing the actions that can be performed on the workflow.
 * @param <C> The context object containing data required by predicates and listeners.
 */
public interface WorkflowDefinition<S extends Enum<S>, A extends Enum<A>, C extends WorkflowContext> {

    /**
     * @return The entity kind this workflow applies to. This will be used as the key in the registry.
     */
    EntityKind getEntityKind();

    /**
     * Builds the state machine with all its transition rules.
     *
     * @param builder A pre-configured builder to which the transition rules will be added.
     * @return A fully constructed {@link WorkflowStateMachine} for this entity kind.
     */
    WorkflowStateMachine<S, A, C> build(WorkflowStateMachineBuilder<S, A, C> builder);
}
