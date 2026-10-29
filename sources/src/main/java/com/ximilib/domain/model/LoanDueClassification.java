package com.ximilib.domain.model;

import com.ximilib.presentation.util.UITheme;
import java.awt.Color;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * LoanDueClassification: Hàm thuần phân loại hạn trả sách (Domain pure function).
 * Phục vụ khối "Cần xử lý hôm nay" và "Sách đang mượn". Dễ dàng kiểm thử đơn vị.
 */
public record LoanDueClassification(Type type, int days, String label) {

    public enum Type {
        OVERDUE,   // Quá hạn (Màu đỏ - Danger)
        DUE_TODAY, // Đến hạn hôm nay (Màu vàng - Warning)
        DUE_SOON   // Còn hạn (Màu xanh lá - Success)
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

    public Color textColor() {
        return switch (type) {
            case OVERDUE -> UITheme.DANGER_TEXT;
            case DUE_TODAY -> UITheme.WARNING_TEXT;
            case DUE_SOON -> UITheme.SUCCESS_TEXT;
        };
    }

    public Color backgroundColor() {
        return switch (type) {
            case OVERDUE -> UITheme.DANGER_BG;
            case DUE_TODAY -> UITheme.WARNING_BG;
            case DUE_SOON -> UITheme.SUCCESS_BG;
        };
    }

    public Color borderColor() {
        return switch (type) {
            case OVERDUE -> Color.decode("#FECACA");
            case DUE_TODAY -> Color.decode("#FDE68A");
            case DUE_SOON -> Color.decode("#A7F3D0");
        };
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
