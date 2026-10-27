package com.ximilib.application.dto;

import com.ximilib.models.User;

/**
 * LoginResult: Kết quả phản hồi từ Application Service khi thực hiện đăng nhập.
 */
public class LoginResult {

    private final boolean success;
    private final User user;
    private final String errorCode;
    private final String errorMessage;
    private final long remainingLockMinutes;

    private LoginResult(boolean success, User user, String errorCode, String errorMessage, long remainingLockMinutes) {
        this.success = success;
        this.user = user;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.remainingLockMinutes = remainingLockMinutes;
    }

    public static LoginResult success(User user) {
        return new LoginResult(true, user, null, null, 0);
    }

    public static LoginResult fail(String errorCode, String errorMessage) {
        return new LoginResult(false, null, errorCode, errorMessage, 0);
    }

    public static LoginResult locked(long remainingLockMinutes, String message) {
        return new LoginResult(false, null, "AUTH_002", message, remainingLockMinutes);
    }

    public boolean isSuccess() {
        return success;
    }

    public User getUser() {
        return user;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public long getRemainingLockMinutes() {
        return remainingLockMinutes;
    }
}
