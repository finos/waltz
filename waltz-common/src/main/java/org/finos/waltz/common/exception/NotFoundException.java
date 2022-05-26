package org.finos.waltz.common.exception;

import static java.lang.String.format;

public class NotFoundException extends RuntimeException {

    private final String code;


    public NotFoundException(String code, String message) {
        super(message);
        this.code = code;
    }


    public NotFoundException(String code, String messageFormat, Object... args) {
        super(format(messageFormat, args));
        this.code = code;
    }


    public String getCode() {
        return code;
    }
}
