package com.khartec.waltz.model.exceptions;

public class NotAuthorizedException extends RuntimeException {

    public NotAuthorizedException() {
        this("User is not authorized to perform that action");
    }

    public NotAuthorizedException(String message) {
        super(message);
    }
}
