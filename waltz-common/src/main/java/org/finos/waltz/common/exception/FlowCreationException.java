package org.finos.waltz.common.exception;

public class FlowCreationException extends Exception{

    public FlowCreationException(String message, Exception ex) {
        super(message);
    }
}
