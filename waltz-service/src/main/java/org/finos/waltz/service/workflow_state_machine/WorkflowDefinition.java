package org.finos.waltz.service.workflow_state_machine;

import org.finos.waltz.model.EntityKind;

/**
 * Defines a contract for any class that provides a state machine for a given entity kind.
 * There can be more than one workflow definitions for a given entity kind but there
 * should only be one workflow definition for a workflow id.
 *
 * @param <S> The enum representing the states of the workflow.
 * @param <A> The type representing the actions that can be performed on the workflow.
 * @param <C> The context object containing data required by predicates and listeners.
 */
public interface WorkflowDefinition<S extends Enum<S>, A extends Enum<A>, C extends WorkflowContext> {

    /**
     * @return The entity kind this workflow applies to.
     */
    EntityKind getEntityKind();

    WorkflowStateMachine getMachine();
}
