package com.ximilib.presentation.component;

import com.ximilib.presentation.util.UITheme;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

/**
 * SectionHeader: Tiêu đề khối chuẩn phong cách LibRack/Stripe.
 * Gồm tiêu đề 16px đậm, nhãn số lượng tùy chọn và liên kết hành động "Xem tất cả >".
 */
public class SectionHeader extends JPanel {

    private final JPanel left;
    private JLabel lblBadge;
    private JLabel lblAction;

    public SectionHeader(String title, String actionText, Runnable onAction) {
        this(title, null, actionText, onAction);
    }

    public SectionHeader(String title, String badgeCount, String actionText, Runnable onAction) {
        setOpaque(false);
        setLayout(new BorderLayout());
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(UITheme.TEXT_MAIN);
        left.add(lblTitle);

        if (badgeCount != null && !badgeCount.isBlank()) {
            lblBadge = new JLabel(badgeCount);
            lblBadge.setFont(UITheme.FONT_BADGE);
            lblBadge.setOpaque(true);
            lblBadge.setBackground(UITheme.TINT);
            lblBadge.setForeground(UITheme.ON_TINT);
            lblBadge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
            left.add(lblBadge);
        }

        add(left, BorderLayout.WEST);

        if (actionText != null && !actionText.isBlank()) {
            lblAction = new JLabel(actionText);
            lblAction.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblAction.setForeground(UITheme.PRIMARY);
            lblAction.setCursor(new Cursor(Cursor.HAND_CURSOR));
            lblAction.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 2));
            lblAction.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    lblAction.setForeground(UITheme.PRIMARY_HOVER);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    lblAction.setForeground(UITheme.PRIMARY);
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (onAction != null) {
                        onAction.run();
                    }
                }
            });
            add(lblAction, BorderLayout.EAST);
        }
    }

    public void setBadge(String badgeCount) {
        if (badgeCount != null && !badgeCount.isBlank()) {
            if (lblBadge == null) {
                lblBadge = new JLabel(badgeCount);
                lblBadge.setFont(UITheme.FONT_BADGE);
                lblBadge.setOpaque(true);
                lblBadge.setBackground(UITheme.TINT);
                lblBadge.setForeground(UITheme.ON_TINT);
                lblBadge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
                left.add(lblBadge);
            } else {
                lblBadge.setText(badgeCount);
                lblBadge.setVisible(true);
            }
        } else if (lblBadge != null) {
            lblBadge.setVisible(false);
        }
        revalidate();
        repaint();
    }
}
