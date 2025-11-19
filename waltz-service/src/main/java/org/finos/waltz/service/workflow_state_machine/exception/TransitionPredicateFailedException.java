package org.finos.waltz.service.workflow_state_machine.exception;

public class TransitionPredicateFailedException extends Exception {
    public TransitionPredicateFailedException(String message) {
        super(message);
    }
}
