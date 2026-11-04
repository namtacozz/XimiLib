package com.ximilib.domain.exception;

public class CopyException extends BusinessRuleException {

    public CopyException(String message) {
        super("COPY_ERROR", message);
    }

    public CopyException(String errorCode, String message) {
        super(errorCode, message);
    }
}
