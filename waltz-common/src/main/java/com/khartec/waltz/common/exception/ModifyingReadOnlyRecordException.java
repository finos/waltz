package com.khartec.waltz.common.exception;

public class ModifyingReadOnlyRecordException extends RuntimeException {

    private final String code;


    public ModifyingReadOnlyRecordException(String code, String message) {
        super(message);
        this.code = code;
    }


    public String getCode() {
        return code;
    }
}
