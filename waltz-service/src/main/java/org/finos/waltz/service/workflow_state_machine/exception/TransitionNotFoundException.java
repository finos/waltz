package org.finos.waltz.service.workflow_state_machine.exception;

public class TransitionNotFoundException extends Exception {
    public TransitionNotFoundException(String message) {
        super(message);
    }
}
