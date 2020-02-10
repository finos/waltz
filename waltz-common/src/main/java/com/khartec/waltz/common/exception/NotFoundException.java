package com.khartec.waltz.common.exception;

public class NotFoundException extends RuntimeException {

    private final String code;


    public NotFoundException(String code, String message) {
        super(message);
        this.code = code;
    }


    public NotFoundException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }


    public String getCode() {
        return code;
    }
}
