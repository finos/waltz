package org.finos.waltz.service.workflow_state_machine;

// --- LISTENER ---

/**
 * A listener that is executed after a successful transition for supplementary actions.
 *
 * @param <C> The type of the context object.
 */
public interface WorkflowTransitionListener<S extends Enum<S>, C extends WorkflowContext> {
    void onTransition(S from, S to, C context);
}
