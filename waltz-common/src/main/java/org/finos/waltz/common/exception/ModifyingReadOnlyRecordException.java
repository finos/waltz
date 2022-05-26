package org.finos.waltz.common.exception;

import static java.lang.String.format;

public class ModifyingReadOnlyRecordException extends RuntimeException {

    private final String code;


    public ModifyingReadOnlyRecordException(String code, String message) {
        super(message);
        this.code = code;
    }

    public ModifyingReadOnlyRecordException(String code, String messageFormat, Object... args) {
        super(format(messageFormat, args));
        this.code = code;
    }


    public String getCode() {
        return code;
    }
}
