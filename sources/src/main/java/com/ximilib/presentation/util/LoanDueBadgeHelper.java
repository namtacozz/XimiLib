package com.ximilib.presentation.util;

import com.ximilib.domain.model.LoanDueClassification;
import java.awt.Color;

/**
 * LoanDueBadgeHelper: Lớp hỗ trợ giao diện ánh xạ phân loại hạn trả sách (LoanDueClassification)
 * sang mã màu và kiểu hiển thị trực quan (Badge) trên Swing UI.
 * Giữ nguyên các mã màu chuẩn của giao diện và đảm bảo tầng Domain hoàn toàn độc lập với UI.
 */
public final class LoanDueBadgeHelper {

    private LoanDueBadgeHelper() {}

    public static Color getTextColor(LoanDueClassification.Type type) {
        if (type == null) return UITheme.TEXT_PRIMARY;
        return switch (type) {
            case OVERDUE -> UITheme.DANGER_TEXT;
            case DUE_TODAY -> UITheme.WARNING_TEXT;
            case DUE_SOON -> UITheme.SUCCESS_TEXT;
        };
    }

    public static Color getBackgroundColor(LoanDueClassification.Type type) {
        if (type == null) return UITheme.CARD_BG;
        return switch (type) {
            case OVERDUE -> UITheme.DANGER_BG;
            case DUE_TODAY -> UITheme.WARNING_BG;
            case DUE_SOON -> UITheme.SUCCESS_BG;
        };
    }

    public static Color getBorderColor(LoanDueClassification.Type type) {
        if (type == null) return UITheme.CARD_BORDER;
        return switch (type) {
            case OVERDUE -> Color.decode("#FECACA");
            case DUE_TODAY -> Color.decode("#FDE68A");
            case DUE_SOON -> Color.decode("#A7F3D0");
        };
    }
}
