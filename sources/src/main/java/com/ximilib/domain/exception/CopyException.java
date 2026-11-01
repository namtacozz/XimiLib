package com.ximilib.domain.exception;

/**
 * CopyException: Ngoại lệ nghiệp vụ liên quan đến cuốn sách / vị trí lưu trữ kệ sách.
 */
public class CopyException extends BusinessRuleException {

    public CopyException(String message) {
        super("COPY_ERROR", message);
    }

    public CopyException(String errorCode, String message) {
        super(errorCode, message);
    }
}
