package com.ximilib.presentation.component;

import com.ximilib.presentation.util.UITheme;
import java.awt.*;
import java.awt.geom.Path2D;
import javax.swing.*;

/**
 * PasswordPolicyChecklist: Thành phần kiểm tra độ mạnh mật khẩu theo thời gian thực (SRS UC-2, BR-10).
 * - Tối thiểu 8 ký tự
 * - Ít nhất 1 chữ hoa
 * - Ít nhất 1 chữ số
 * - Ít nhất 1 ký tự đặc biệt
 * Hiển thị mượt mà: icon tick xanh khi đạt, chấm xám khi chưa đạt. Tự động ẩn khi trống.
 */
public class PasswordPolicyChecklist extends JPanel {

    private final CheckItem itemLength = new CheckItem("Tối thiểu 8 ký tự");
    private final CheckItem itemUpper = new CheckItem("Ít nhất 1 chữ hoa (A-Z)");
    private final CheckItem itemDigit = new CheckItem("Ít nhất 1 chữ số (0-9)");
    private final CheckItem itemSpecial = new CheckItem("1 ký tự đặc biệt (!@#...)");

    private boolean allSatisfied = false;

    public PasswordPolicyChecklist() {
        setOpaque(false);
        setLayout(new GridLayout(2, 2, 8, 4));
        setPreferredSize(new Dimension(380, 44));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        add(itemLength);
        add(itemUpper);
        add(itemDigit);
        add(itemSpecial);

        setVisible(false); // Ẩn khi chưa nhập
    }

    public void updatePassword(String password) {
        if (password == null || password.isEmpty()) {
            setVisible(false);
            allSatisfied = false;
            return;
        }

        setVisible(true);
        boolean lengthOk = password.length() >= 8;
        boolean upperOk = password.chars().anyMatch(Character::isUpperCase);
        boolean digitOk = password.chars().anyMatch(Character::isDigit);
        boolean specialOk = password.matches(".*[^A-Za-z0-9].*");

        itemLength.setStatus(lengthOk);
        itemUpper.setStatus(upperOk);
        itemDigit.setStatus(digitOk);
        itemSpecial.setStatus(specialOk);

        allSatisfied = lengthOk && upperOk && digitOk && specialOk;
        revalidate();
        repaint();
    }

    public boolean isAllSatisfied() {
        return allSatisfied;
    }

    private static class CheckItem extends JPanel {
        private final String text;
        private boolean satisfied = false;

        public CheckItem(String text) {
            this.text = text;
            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.LEFT, 6, 0));

            JLabel lbl = new JLabel(text);
            lbl.setFont(UITheme.FONT_SMALL);
            lbl.setForeground(UITheme.TEXT_MUTED);

            add(new IconBadge());
            add(lbl);
        }

        public void setStatus(boolean satisfied) {
            this.satisfied = satisfied;
            JLabel lbl = (JLabel) getComponent(1);
            lbl.setForeground(satisfied ? new Color(22, 101, 52) : UITheme.TEXT_MUTED);
            repaint();
        }

        private class IconBadge extends JComponent {
            public IconBadge() {
                setPreferredSize(new Dimension(14, 14));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                if (satisfied) {
                    // Tròn xanh lá cây + Dấu tick trắng
                    g2.setColor(new Color(34, 197, 94));
                    g2.fillOval(0, 0, w, h);

                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    Path2D.Float tick = new Path2D.Float();
                    tick.moveTo(w * 0.25f, h * 0.50f);
                    tick.lineTo(w * 0.45f, h * 0.72f);
                    tick.lineTo(w * 0.78f, h * 0.28f);
                    g2.draw(tick);
                } else {
                    // Chấm xám rỗng
                    g2.setColor(UITheme.BORDER);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawOval(1, 1, w - 3, h - 3);
                }

                g2.dispose();
            }
        }
    }
}
