package com.ximilib.gui.util;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.AbstractBorder;

/**
 * UITheme: Định nghĩa bảng màu, font chữ và các thành phần giao diện chuẩn
 * theo phong cách LibRack (librack.com) cho đồ án XimiLib.
 * Tác giả: Phan Vũ Xuân Kiên (GUI / DB)
 */
public class UITheme {

    // Bảng màu chuẩn LibRack Emerald Theme
    public static final Color PRIMARY = new Color(5, 150, 105);          // #059669 - Emerald Green
    public static final Color PRIMARY_HOVER = new Color(4, 120, 87);     // #047857
    public static final Color PRIMARY_LIGHT = new Color(16, 185, 129);   // #10B981
    public static final Color PRIMARY_MINT = new Color(209, 250, 229);    // #D1FAE5 - Mint nhạt
    
    public static final Color BG_LIGHT = new Color(248, 250, 252);        // #F8FAFC - Nền tổng thể
    public static final Color CARD_BG = Color.WHITE;                      // #FFFFFF - Nền card
    public static final Color BORDER = new Color(226, 232, 240);          // #E2E8F0 - Viền xám mảnh
    public static final Color BORDER_FOCUS = new Color(16, 185, 129);     // Viền khi focus
    
    public static final Color TEXT_MAIN = new Color(15, 23, 42);          // #0F172A - Chữ đậm chính
    public static final Color TEXT_MUTED = new Color(100, 116, 139);      // #64748B - Chữ phụ/mờ
    
    // Màu trạng thái
    public static final Color SUCCESS_GREEN = new Color(16, 185, 129);   // #10B981
    public static final Color SUCCESS_TEXT = new Color(21, 128, 61);      // #15803D
    public static final Color SUCCESS_BG = new Color(220, 252, 231);      // #DCFCE7
    public static final Color WARNING_TEXT = new Color(180, 83, 9);       // #B45309
    public static final Color WARNING_BG = new Color(254, 243, 199);      // #FEF3C7
    public static final Color DANGER_TEXT = new Color(185, 28, 28);       // #B91C1C
    public static final Color DANGER_BG = new Color(254, 226, 226);       // #FEE2E2
    public static final Color PURPLE_TEXT = new Color(126, 34, 206);      // #7E22CE
    public static final Color PURPLE_BG = new Color(243, 232, 255);       // #F3E8FF

    // Font chữ chuẩn giao diện
    public static final Font FONT_TITLE_LARGE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BADGE = new Font("Segoe UI", Font.BOLD, 11);

    /**
     * Khởi tạo Look and Feel FlatLaf nếu có trong classpath,
     * nếu không có thì fallback mượt mà sang System Look and Feel.
     */
    public static void setupLookAndFeel() {
        try {
            // Thử khởi tạo FlatLaf qua Reflection để tương thích cả khi chưa tải jar
            Class<?> flatLafClass = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            flatLafClass.getMethod("setup").invoke(null);
            
            // Cấu hình bo góc mặc định của FlatLaf
            UIManager.put("Button.arc", 999);
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 12);
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }
    }

    /**
     * Tạo nút bấm con nhộng (Pill Button) màu sắc hiện đại chuẩn LibRack
     */
    public static JButton createPillButton(String text, Color bgColor, Color fgColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(
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
