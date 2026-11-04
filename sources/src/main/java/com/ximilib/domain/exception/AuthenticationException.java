package com.ximilib.domain.exception;

public class AuthenticationException extends BusinessRuleException {

    public AuthenticationException() {
        super("AUTH_001", "Tên đăng nhập hoặc mật khẩu không chính xác.");
    }

    public AuthenticationException(String message) {
        super("AUTH_001", message);
    }
}
