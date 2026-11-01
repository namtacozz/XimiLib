package com.ximilib.domain.exception;

/**
 * BookException: Ngoại lệ nghiệp vụ liên quan đến phân khu / đầu sách.
 */
public class BookException extends BusinessRuleException {

    public BookException(String message) {
        super("BOOK_ERROR", message);
    }

    public BookException(String errorCode, String message) {
        super(errorCode, message);
    }
}
