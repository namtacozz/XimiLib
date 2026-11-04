package com.ximilib.domain.exception;

public class AccountLockedException extends BusinessRuleException {

    private final long remainingMinutes;

    public AccountLockedException(long remainingMinutes) {
        super("AUTH_002", "Tài khoản bị tạm khóa " + (remainingMinutes > 0 ? remainingMinutes : 15) +
              " phút do nhập sai quá 5 lần quy định (Quy tắc BR-01).");
        this.remainingMinutes = remainingMinutes;
    }

    public long getRemainingMinutes() {
        return remainingMinutes;
    }
}
