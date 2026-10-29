package com.ximilib.presentation.component;

import com.ximilib.presentation.util.UITheme;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

/**
 * KpiCard: Thẻ chỉ số thống kê vận hành chuẩn LibRack / Stripe.
 * - Chiều cao ~88-92px, bo góc 12px viền mảnh.
 * - Icon tròn tint 36px vẽ vector Java2D.
 * - Số liệu lớn 24px đậm, nhãn 12px textMuted, dòng phụ khả dụng nếu có.
 * - Thẻ Quá hạn màu đỏ khi > 0, màu trung tính khi = 0.
 * - Bỏ huy hiệu xu hướng (+12%, +8%).
 * - Hiệu ứng hover tintLight, con trỏ tay, click điều hướng.
 */
public class KpiCard extends JPanel {

    public enum CardType {
        TOTAL_BOOKS,
        ACTIVE_LOANS,
        MEMBERS,
        OVERDUE
    }

    private final CardType type;
    private final JLabel lblValue;
    private final JLabel lblLabel;
    private final JLabel lblSub;
    private final JPanel iconCircle;
    private boolean isHover = false;
    private Runnable clickAction;

    public KpiCard(CardType type, String value, String label, String subText) {
        this(type, value, label, subText, null);
    }

    public KpiCard(CardType type, String value, String label, String subText, Runnable onClick) {
        this.type = type;
        this.clickAction = onClick;

        setOpaque(false);
        setLayout(new BorderLayout(14, 0));
        setPreferredSize(new Dimension(220, 88));
        setMinimumSize(new Dimension(170, 88));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 1. Icon tròn 36px bên trái
        iconCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bg = getIconBgColor();
                Color fg = getIconFgColor();

                g2.setColor(bg);
                g2.fillOval(0, 0, getWidth(), getHeight());

                g2.setColor(fg);
                g2.setStroke(new BasicStroke(1.8f));
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                switch (type) {
                    case TOTAL_BOOKS -> {
                        // Trang sách
                        g2.drawRoundRect(cx - 7, cy - 9, 14, 18, 2, 2);
                        g2.drawLine(cx - 4, cy - 4, cx + 4, cy - 4);
                        g2.drawLine(cx - 4, cy, cx + 4, cy);
                        g2.drawLine(cx - 4, cy + 4, cx + 1, cy + 4);
                    }
                    case ACTIVE_LOANS -> {
                        // Sách đang mượn / dấu kiểm
                        g2.drawRoundRect(cx - 8, cy - 7, 16, 14, 3, 3);
                        g2.drawLine(cx - 4, cy, cx - 1, cy + 3);
                        g2.drawLine(cx - 1, cy + 3, cx + 4, cy - 3);
                    }
                    case MEMBERS -> {
                        // Biểu tượng người dùng
                        g2.drawOval(cx - 4, cy - 8, 8, 8);
                        g2.drawArc(cx - 7, cy, 14, 12, 0, 180);
                    }
                    case OVERDUE -> {
                        // Đồng hồ / chuông cảnh báo
                        g2.drawOval(cx - 8, cy - 8, 16, 16);
                        g2.drawLine(cx, cy - 5, cx, cy);
                        g2.drawLine(cx, cy, cx + 4, cy);
                    }
                }
                g2.dispose();
            }
        };
        iconCircle.setPreferredSize(new Dimension(36, 36));
        iconCircle.setMaximumSize(new Dimension(36, 36));
        iconCircle.setOpaque(false);

        JPanel leftWrapper = new JPanel(new GridBagLayout());
        leftWrapper.setOpaque(false);
        leftWrapper.add(iconCircle);

        // 2. Cột số liệu và nhãn bên phải
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblValue.setForeground(getValueTextColor());

        lblLabel = new JLabel(label);
        lblLabel.setFont(UITheme.FONT_BADGE);
        lblLabel.setForeground(UITheme.TEXT_MUTED);

        lblSub = new JLabel(subText != null ? subText : "");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(UITheme.PRIMARY);
        lblSub.setVisible(subText != null && !subText.isBlank());

        contentPanel.add(Box.createVerticalGlue());
        contentPanel.add(lblValue);
        contentPanel.add(Box.createVerticalStrut(1));
        contentPanel.add(lblLabel);
        if (subText != null && !subText.isBlank()) {
            contentPanel.add(lblSub);
        }
        contentPanel.add(Box.createVerticalGlue());

        add(leftWrapper, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // Hiệu ứng hover & click
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHover = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && clickAction != null) {
                    clickAction.run();
                }
            }
        };

        addMouseListener(mouseAdapter);
        contentPanel.addMouseListener(mouseAdapter);
        lblValue.addMouseListener(mouseAdapter);
        lblLabel.addMouseListener(mouseAdapter);
        lblSub.addMouseListener(mouseAdapter);
        iconCircle.addMouseListener(mouseAdapter);
    }

    public void updateData(String value, String subText) {
        lblValue.setText(value);
        lblValue.setForeground(getValueTextColor());
        if (subText != null && !subText.isBlank()) {
            lblSub.setText(subText);
            lblSub.setVisible(true);
        } else {
            lblSub.setVisible(false);
        }
        iconCircle.repaint();
        repaint();
    }

    private boolean isOverduePositive() {
        if (type != CardType.OVERDUE) return false;
        try {
            int val = Integer.parseInt(lblValue.getText().trim());
            return val > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private Color getIconBgColor() {
        if (type == CardType.OVERDUE) {
            return isOverduePositive() ? UITheme.DANGER_BG : UITheme.TINT;
        }
        return UITheme.TINT;
    }

    private Color getIconFgColor() {
        if (type == CardType.OVERDUE) {
            return isOverduePositive() ? UITheme.DANGER_TEXT : UITheme.PRIMARY;
        }
        return UITheme.PRIMARY;
    }

    private Color getValueTextColor() {
        if (type == CardType.OVERDUE && isOverduePositive()) {
            return UITheme.DANGER_TEXT;
        }
        return UITheme.TEXT_MAIN;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Nền trắng, khi hover đổi nhẹ sang tintLight
        g2.setColor(isHover ? UITheme.TINT_LIGHT : UITheme.CARD_BG);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

        // Viền 1px bo góc 12px
        g2.setColor(isHover ? UITheme.PRIMARY : UITheme.BORDER);
        g2.setStroke(new BasicStroke(isHover ? 1.4f : 1.0f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

        g2.dispose();
        super.paintComponent(g);
    }
}
