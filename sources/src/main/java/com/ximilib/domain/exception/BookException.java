package com.ximilib.domain.exception;

public class BookException extends BusinessRuleException {

    public BookException(String message) {
        super("BOOK_ERROR", message);
    }

    public BookException(String errorCode, String message) {
        super(errorCode, message);
    }
}
