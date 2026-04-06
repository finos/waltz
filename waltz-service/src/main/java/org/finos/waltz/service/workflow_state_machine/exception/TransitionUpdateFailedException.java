package org.finos.waltz.service.workflow_state_machine.exception;

public class TransitionUpdateFailedException extends RuntimeException {
    public TransitionUpdateFailedException(String message) {
        super(message);
    }
}
