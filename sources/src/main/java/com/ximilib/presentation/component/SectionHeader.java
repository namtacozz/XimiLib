package com.ximilib.presentation.component;

import com.ximilib.gui.util.UITheme;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

/**
 * SectionHeader: Tiêu đề khối chuẩn phong cách LibRack/Stripe.
 * Gồm tiêu đề 16px đậm, nhãn số lượng tùy chọn và liên kết hành động "Xem tất cả >".
 */
public class SectionHeader extends JPanel {

    public SectionHeader(String title, String actionText, Runnable onAction) {
        this(title, null, actionText, onAction);
    }

    public SectionHeader(String title, String badgeCount, String actionText, Runnable onAction) {
        setOpaque(false);
        setLayout(new BorderLayout());
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(UITheme.TEXT_MAIN);
        left.add(lblTitle);

        if (badgeCount != null && !badgeCount.isBlank()) {
            JLabel lblBadge = new JLabel(badgeCount);
            lblBadge.setFont(UITheme.FONT_BADGE);
            lblBadge.setOpaque(true);
            lblBadge.setBackground(UITheme.TINT);
            lblBadge.setForeground(UITheme.ON_TINT);
            lblBadge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
            left.add(lblBadge);
        }

        add(left, BorderLayout.WEST);

        if (actionText != null && !actionText.isBlank()) {
            JLabel lblAction = new JLabel(actionText);
            lblAction.setFont(UITheme.FONT_SMALL);
            lblAction.setForeground(UITheme.PRIMARY);
            lblAction.setCursor(new Cursor(Cursor.HAND_CURSOR));
            lblAction.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 4));
            if (onAction != null) {
                lblAction.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        onAction.run();
                    }
                });
            }
            add(lblAction, BorderLayout.EAST);
        }
    }
}
