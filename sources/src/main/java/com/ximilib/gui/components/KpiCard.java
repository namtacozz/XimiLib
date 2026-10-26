package com.ximilib.gui.components;

import com.ximilib.gui.util.UITheme;
import java.awt.*;
import javax.swing.*;

/**
 * KpiCard: Thẻ chỉ số thống kê bo góc mềm mại, hiển thị số liệu lớn kèm icon
 * và nhãn tăng trưởng theo thiết kế LibRack.
 * Tác giả: Phan Vũ Xuân Kiên
 */
public class KpiCard extends JPanel {

    public enum CardType {
        TOTAL_BOOKS,
        ACTIVE_LOANS,
        MEMBERS,
        OVERDUE
    }

    public KpiCard(CardType type, String value, String label, String trend) {
        setOpaque(false);
        setLayout(new BorderLayout(12, 10));
        setPreferredSize(new Dimension(185, 115));
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        // Màu sắc icon và chữ theo từng loại thẻ
        Color iconBg;
        Color iconColor;
        Color numberColor = UITheme.TEXT_MAIN;
        String iconSymbol;

        switch (type) {
            case TOTAL_BOOKS -> {
                iconBg = UITheme.SUCCESS_BG;
                iconColor = UITheme.SUCCESS_TEXT;
                iconSymbol = "📄";
            }
            case ACTIVE_LOANS -> {
                iconBg = UITheme.PRIMARY_MINT;
                iconColor = UITheme.PRIMARY;
                iconSymbol = "✔";
            }
            case MEMBERS -> {
                iconBg = UITheme.PURPLE_BG;
                iconColor = UITheme.PURPLE_TEXT;
                iconSymbol = "👥";
            }
            case OVERDUE -> {
                iconBg = UITheme.DANGER_BG;
                iconColor = UITheme.DANGER_TEXT;
                numberColor = UITheme.DANGER_TEXT;
                iconSymbol = "⚠";
            }
            default -> {
                iconBg = UITheme.BG_LIGHT;
                iconColor = UITheme.TEXT_MUTED;
                iconSymbol = "•";
            }
        }

        // 1. Icon tròn ở góc trên bên trái vẽ vector hoàn toàn, không phụ thuộc font
        JPanel iconCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconBg);
                g2.fillOval(0, 0, getWidth(), getHeight());
                
                g2.setColor(iconColor);
                g2.setStroke(new BasicStroke(1.8f));
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                switch (type) {
                    case TOTAL_BOOKS -> {
                        // Vẽ trang sách / tài liệu
                        g2.drawRoundRect(cx - 6, cy - 8, 12, 16, 2, 2);
                        g2.drawLine(cx - 3, cy - 4, cx + 3, cy - 4);
                        g2.drawLine(cx - 3, cy, cx + 3, cy);
                        g2.drawLine(cx - 3, cy + 4, cx + 1, cy + 4);
                    }
                    case ACTIVE_LOANS -> {
                        // Dấu tích kiểm tra
                        g2.drawLine(cx - 6, cy, cx - 1, cy + 5);
                        g2.drawLine(cx - 1, cy + 5, cx + 6, cy - 4);
                    }
                    case MEMBERS -> {
                        // Hai người dùng
                        g2.drawOval(cx - 3, cy - 7, 6, 6);
                        g2.drawArc(cx - 6, cy, 12, 10, 0, 180);
                    }
                    case OVERDUE -> {
                        // Tam giác cảnh báo
                        int[] xs = {cx, cx - 8, cx + 8};
                        int[] ys = {cy - 7, cy + 7, cy + 7};
                        g2.drawPolygon(xs, ys, 3);
                        g2.drawLine(cx, cy - 2, cx, cy + 2);
                        g2.fillRect(cx - 1, cy + 4, 2, 2);
                    }
                }
                g2.dispose();
            }
        };
        iconCircle.setPreferredSize(new Dimension(32, 32));
        iconCircle.setOpaque(false);

        // Header chứa icon và trend badge (nếu có)
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(iconCircle, BorderLayout.WEST);

        if (trend != null && !trend.isEmpty()) {
            JLabel lblTrend = new JLabel(trend);
            lblTrend.setFont(UITheme.FONT_BADGE);
            lblTrend.setForeground(UITheme.SUCCESS_TEXT);
            topRow.add(lblTrend, BorderLayout.EAST);
        }

        // 2. Phần nội dung số liệu
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblValue.setForeground(numberColor);

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(UITheme.FONT_SMALL);
        lblLabel.setForeground(UITheme.TEXT_MUTED);

        contentPanel.add(lblValue);
        contentPanel.add(Box.createVerticalStrut(2));
        contentPanel.add(lblLabel);

        add(topRow, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Nền trắng bo góc 16px
        g2.setColor(UITheme.CARD_BG);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

        // Viền xám mảnh #E2E8F0
        g2.setColor(UITheme.BORDER);
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

        g2.dispose();
        super.paintComponent(g);
    }
}
