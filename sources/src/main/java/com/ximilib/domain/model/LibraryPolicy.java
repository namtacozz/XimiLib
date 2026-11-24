package com.ximilib.domain.model;

import java.math.BigDecimal;
import java.util.List;

public final class LibraryPolicy {

    private LibraryPolicy() {}

    public static final int MAX_BOOKS_BORROW = 3;
    public static final int LOAN_DAYS_HOME = 14;
    public static final String DUE_TIME_DESK = "17:00";
    public static final int RENEW_DAYS = 7;
    public static final int MAX_RENEW_COUNT = 1;
    public static final BigDecimal FINE_PER_DAY = new BigDecimal("2000.00");
    public static final BigDecimal TECHNICAL_FEE = new BigDecimal("20000.00");
    public static final BigDecimal COMPENSATION_RATE = new BigDecimal("2.0");
    public static final int HOLD_HOURS = 24;
    public static final int CARD_VALIDITY_DAYS = 365;
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final int LOCKOUT_MINUTES = 15;

    private static volatile int maxBooksBorrow = MAX_BOOKS_BORROW;
    private static volatile int loanDaysHome = LOAN_DAYS_HOME;
    private static volatile int renewDays = RENEW_DAYS;
    private static volatile int maxRenewCount = MAX_RENEW_COUNT;
    private static volatile BigDecimal finePerDay = FINE_PER_DAY;
    private static volatile BigDecimal technicalFee = TECHNICAL_FEE;
    private static volatile BigDecimal compensationRate = COMPENSATION_RATE;
    private static volatile int holdHours = HOLD_HOURS;
    private static volatile int cardValidityDays = CARD_VALIDITY_DAYS;
    private static volatile int maxLoginAttempts = MAX_LOGIN_ATTEMPTS;
    private static volatile int lockoutMinutes = LOCKOUT_MINUTES;

    public static int getMaxBooksBorrow() { return maxBooksBorrow; }
    public static int getLoanDaysHome() { return loanDaysHome; }
    public static int getRenewDays() { return renewDays; }
    public static int getMaxRenewCount() { return maxRenewCount; }
    public static BigDecimal getFinePerDay() { return finePerDay; }
    public static BigDecimal getTechnicalFee() { return technicalFee; }
    public static BigDecimal getCompensationRate() { return compensationRate; }
    public static int getHoldHours() { return holdHours; }
    public static int getCardValidityDays() { return cardValidityDays; }
    public static int getMaxLoginAttempts() { return maxLoginAttempts; }
    public static int getLockoutMinutes() { return lockoutMinutes; }

    public static void reloadFromSettings(List<Object[]> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        for (Object[] row : rows) {
            if (row == null || row.length < 2 || row[0] == null || row[1] == null) {
                continue;
            }
            String key = row[0].toString().trim().toLowerCase();
            String val = row[1].toString().trim();
            applySetting(key, val);
        }
    }

    private static void applySetting(String key, String val) {
        if ("policy.max_borrow_books".equals(key) || "max_books_borrow".equals(key)) {
            maxBooksBorrow = parsePositiveInt(val, MAX_BOOKS_BORROW);
        } else if ("policy.borrow_duration_days".equals(key) || "loan_days_home".equals(key)) {
            loanDaysHome = parsePositiveInt(val, LOAN_DAYS_HOME);
        } else if ("policy.loan_renewal_days".equals(key) || "renew_days".equals(key)) {
            renewDays = parsePositiveInt(val, RENEW_DAYS);
        } else if ("policy.max_renewal_times".equals(key) || "max_renew_count".equals(key)) {
            maxRenewCount = parsePositiveInt(val, MAX_RENEW_COUNT);
        } else if ("policy.fine_per_day_vnd".equals(key) || "fine_per_day".equals(key)) {
            finePerDay = parsePositiveDecimal(val, FINE_PER_DAY);
        } else if ("policy.tech_fee_vnd".equals(key) || "technical_fee".equals(key)) {
            technicalFee = parsePositiveDecimal(val, TECHNICAL_FEE);
        } else if ("policy.compensation_rate".equals(key) || "compensation_rate".equals(key)) {
            compensationRate = parsePositiveDecimal(val, COMPENSATION_RATE);
        } else if ("policy.hold_reservation_hours".equals(key) || "hold_hours".equals(key)) {
            holdHours = parsePositiveInt(val, HOLD_HOURS);
        } else if ("card_validity_days".equals(key)) {
            cardValidityDays = parsePositiveInt(val, CARD_VALIDITY_DAYS);
        } else if ("policy.max_login_attempts".equals(key)) {
            maxLoginAttempts = parsePositiveInt(val, MAX_LOGIN_ATTEMPTS);
        } else if ("policy.lockout_minutes".equals(key)) {
            lockoutMinutes = parsePositiveInt(val, LOCKOUT_MINUTES);
        }
    }

    private static int parsePositiveInt(String val, int fallback) {
        try {
            int parsed = Integer.parseInt(val);
            return parsed > 0 ? parsed : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    private static BigDecimal parsePositiveDecimal(String val, BigDecimal fallback) {
        try {
            BigDecimal parsed = new BigDecimal(val);
            return parsed.compareTo(BigDecimal.ZERO) > 0 ? parsed : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }
}
