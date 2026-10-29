package com.ximilib.presentation.util;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.*;
import java.util.Collections;
import javax.swing.*;
import javax.swing.border.AbstractBorder;

/**
 * UITheme: Định nghĩa bảng màu thương hiệu Pastel Pink, font chữ và các thành phần
 * giao diện chuẩn tập trung cho dự án XimiLib.
 * Toàn bộ ứng dụng tham chiếu token tại đây, không hard-code mã hex trong view.
 */
public class UITheme {

    // =========================================================================
    // 1. BẢNG MÀU THƯƠNG HIỆU MỚI (HỒNG PASTEL TOKEN)
    // =========================================================================
    public static final Color PRIMARY = Color.decode("#DB2777");          // Nút chính, phân trang active, focus, link
    public static final Color PRIMARY_HOVER = Color.decode("#BE185D");    // Hover nút / link
    public static final Color PRIMARY_PRESSED = Color.decode("#9D174D");  // Nhấn giữ nút
    public static final Color PASTEL = Color.decode("#F9A8D4");           // Icon nền tròn, chấm chỉ báo, vạch trang trí
    public static final Color PASTEL_MID = Color.decode("#FBCFE8");       // Viền nhấn, thanh tiến độ, đầu gradient login
    public static final Color BRAND_GRADIENT_MID = Color.decode("#F472B6"); // Điểm giữa gradient dọc BrandPanel
    public static final Color TINT = Color.decode("#FCE7F3");             // Nền tiêu đề bảng, dòng chọn, sidebar active
    public static final Color TINT_LIGHT = Color.decode("#FDF2F8");       // Hover dòng / thẻ phụ
    public static final Color ON_TINT = Color.decode("#9D174D");          // Chữ/icon đặt trên nền tint
    public static final Color ON_PASTEL = Color.decode("#831843");        // Tiêu đề đặt trên nền pastel
    public static final Color PAGE_BG = Color.decode("#FFF7FA");          // Nền cửa sổ / trang
    public static final Color CARD_BORDER = Color.decode("#F3E4EC");      // Viền thẻ, đường kẻ mảnh
    public static final Color TEXT_PRIMARY = Color.decode("#1F1720");     // Chữ chính
    public static final Color TEXT_MUTED = Color.decode("#6B5A66");       // Chữ phụ, nhãn, mô tả
    public static final Color ON_PRIMARY = Color.decode("#FFFFFF");       // Chữ trên nền primary

    // Chuỗi mã Hex tương ứng
    public static final String HEX_PRIMARY = "#DB2777";
    public static final String HEX_PRIMARY_HOVER = "#BE185D";
    public static final String HEX_PRIMARY_PRESSED = "#9D174D";
    public static final String HEX_PASTEL = "#F9A8D4";
    public static final String HEX_PASTEL_MID = "#FBCFE8";
    public static final String HEX_TINT = "#FCE7F3";
    public static final String HEX_TINT_LIGHT = "#FDF2F8";
    public static final String HEX_ON_TINT = "#9D174D";
    public static final String HEX_ON_PASTEL = "#831843";
    public static final String HEX_PAGE_BG = "#FFF7FA";
    public static final String HEX_CARD_BORDER = "#F3E4EC";
    public static final String HEX_TEXT_PRIMARY = "#1F1720";
    public static final String HEX_TEXT_MUTED = "#6B5A66";
    public static final String HEX_ON_PRIMARY = "#FFFFFF";

    // Bí danh (Aliases) tương thích ngược với code hiện có
    public static final Color BG_LIGHT = PAGE_BG;
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER = CARD_BORDER;
    public static final Color BORDER_FOCUS = PRIMARY;
    public static final Color ROW_SELECTED = TINT;
    public static final Color TEXT_MAIN = TEXT_PRIMARY;
    public static final Color PRIMARY_TINT = TINT;
    public static final Color PRIMARY_MINT = PASTEL;
    public static final Color PRIMARY_LIGHT = PASTEL_MID;

    // =========================================================================
    // 2. MÀU TRẠNG THÁI (GIỮ NGUYÊN, KHÔNG ĐỔI SANG HỒNG)
    // =========================================================================
    public static final Color SUCCESS_GREEN = Color.decode("#10B981");
    public static final Color SUCCESS_TEXT = Color.decode("#047857");
    public static final Color SUCCESS_BG = Color.decode("#D1FAE5");
    public static final Color WARNING_TEXT = Color.decode("#B45309");
    public static final Color WARNING_BG = Color.decode("#FEF3C7");
    public static final Color DANGER_TEXT = Color.decode("#B91C1C");
    public static final Color DANGER_BG = Color.decode("#FEE2E2");
    public static final Color PURPLE_TEXT = Color.decode("#7E22CE");
    public static final Color PURPLE_BG = Color.decode("#F3E8FF");

    // =========================================================================
    // 3. FONT CHỮ CHUẨN GIAO DIỆN
    // =========================================================================
    public static final Font FONT_TITLE_LARGE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BADGE = new Font("Segoe UI", Font.BOLD, 11);

    /**
     * Lấy ký tự chữ cái đầu an toàn cho Avatar, chống lỗi StringIndexOutOfBoundsException.
     */
    public static String getAvatarInitial(String fullName) {
        if (fullName == null) return "U";
        String trimmed = fullName.trim();
        if (trimmed.isEmpty()) return "U";
        int lastSpace = trimmed.lastIndexOf(' ');
        if (lastSpace >= 0 && lastSpace + 1 < trimmed.length()) {
            return String.valueOf(trimmed.charAt(lastSpace + 1)).toUpperCase();
        }
        return String.valueOf(trimmed.charAt(0)).toUpperCase();
    }

    /**
     * Khởi tạo Look and Feel FlatLaf tập trung với accentColor hồng pastel (#DB2777).
     */
    public static void setupLookAndFeel() {
        try {
            // Thiết lập accentColor hồng pastel cho FlatLaf trước khi khởi tạo
            FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", HEX_PRIMARY));
            FlatLightLaf.setup();

            // Cấu hình bo góc và màu mặc định đồng bộ trên UIManager
            UIManager.put("Button.arc", 999);
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 12);

            UIManager.put("Component.focusColor", PRIMARY);
            UIManager.put("Component.borderColor", CARD_BORDER);
            UIManager.put("Component.focusedBorderColor", PRIMARY);

            UIManager.put("Panel.background", PAGE_BG);
            UIManager.put("Table.selectionBackground", TINT);
            UIManager.put("Table.selectionForeground", ON_TINT);
            UIManager.put("Table.gridColor", CARD_BORDER);
            UIManager.put("TableHeader.background", TINT);
            UIManager.put("TableHeader.foreground", TEXT_PRIMARY);
            UIManager.put("TableHeader.bottomSeparatorColor", CARD_BORDER);

            UIManager.put("ScrollBar.thumbHoverColor", PASTEL_MID);
            UIManager.put("CheckBox.icon.selectedBorderColor", PRIMARY);
            UIManager.put("CheckBox.icon.selectedBackground", PRIMARY);
        } catch (Throwable e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }
    }

    /**
     * Tạo nút bấm con nhộng (Pill Button) màu sắc hiện đại.
     */
    public static JButton createPillButton(String text, Color bgColor, Color fgColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bgColor.equals(PRIMARY) ? PRIMARY_PRESSED : bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bgColor.equals(PRIMARY) ? PRIMARY_HOVER : new Color(
                        Math.max(0, bgColor.getRed() - 15),
                        Math.max(0, bgColor.getGreen() - 15),
                        Math.max(0, bgColor.getBlue() - 15)
                    ));
                } else {
                    g2.setColor(bgColor);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setForeground(fgColor);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(6, 16, 6, 16));
        return btn;
    }

    /**
     * Viền bo tròn tùy biến với màu sắc và bán kính
     */
    public static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius;

        public RoundedBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }
    }
}
