package com.ximilib.domain.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record LoanDueClassification(Type type, int days, String label) {

    public enum Type {
        OVERDUE,
        DUE_TODAY,
        DUE_SOON
    }

    public boolean isOverdue() {
        return type == Type.OVERDUE;
    }

    public boolean isDueToday() {
        return type == Type.DUE_TODAY;
    }

    public int daysRemaining() {
        return type == Type.OVERDUE ? -days : days;
    }

    public String displayText() {
        return label;
    }

    public static LoanDueClassification classify(LocalDate dueDate, LocalDate today) {
        if (dueDate == null || today == null) {
            return new LoanDueClassification(Type.DUE_TODAY, 0, "Không rõ");
        }

        long diff = ChronoUnit.DAYS.between(today, dueDate);
        if (diff < 0) {
            int overdueDays = (int) Math.abs(diff);
            return new LoanDueClassification(Type.OVERDUE, overdueDays, "Quá hạn " + overdueDays + " ngày");
        } else if (diff == 0) {
            return new LoanDueClassification(Type.DUE_TODAY, 0, "Hạn hôm nay");
        } else {
            return new LoanDueClassification(Type.DUE_SOON, (int) diff, "Còn " + diff + " ngày");
        }
    }
}
