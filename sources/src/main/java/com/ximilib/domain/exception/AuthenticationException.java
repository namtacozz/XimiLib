package com.ximilib.domain.exception;

/**
 * AuthenticationException: Lỗi thông tin xác thực không chính xác (Mã AUTH_001).
 * Không tiết lộ tài khoản có tồn tại trong hệ thống hay không để bảo mật danh tính.
 */
public class AuthenticationException extends BusinessRuleException {

    public AuthenticationException() {
        super("AUTH_001", "Tên đăng nhập hoặc mật khẩu không chính xác.");
    }

    public AuthenticationException(String message) {
        super("AUTH_001", message);
    }
}
