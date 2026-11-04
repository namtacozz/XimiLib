package com.ximilib.domain.model;

import java.math.BigDecimal;

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
}
