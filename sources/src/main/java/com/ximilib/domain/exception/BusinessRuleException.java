package com.ximilib.domain.exception;

/**
 * BusinessRuleException: Ngoại lệ cơ sở cho các quy tắc nghiệp vụ trong XimiLib.
 */
public class BusinessRuleException extends RuntimeException {
    
    private final String errorCode;

    public BusinessRuleException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessRuleException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
