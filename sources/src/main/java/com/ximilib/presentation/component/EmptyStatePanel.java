package com.ximilib.presentation.component;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.ximilib.presentation.util.UITheme;
import java.awt.*;
import javax.swing.*;

public class EmptyStatePanel extends JPanel {

    public EmptyStatePanel(String svgPath, String title, String subtitle) {
        this(svgPath, 160, title, subtitle, null, null);
    }

    public EmptyStatePanel(String svgPath, int iconSize, String title, String subtitle) {
        this(svgPath, iconSize, title, subtitle, null, null);
    }

    public EmptyStatePanel(String svgPath, String title, String subtitle, String actionText, Runnable onAction) {
        this(svgPath, 160, title, subtitle, actionText, onAction);
    }

    public EmptyStatePanel(String svgPath, int iconSize, String title, String subtitle, String actionText, Runnable onAction) {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel lblIcon;
        try {
            lblIcon = new JLabel(new FlatSVGIcon(svgPath, iconSize, iconSize));
        } catch (Exception e) {
            lblIcon = new JLabel();
        }
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(UITheme.FONT_REGULAR);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(Box.createVerticalGlue());
        add(lblIcon);
        add(Box.createVerticalStrut(14));
        add(lblTitle);
        add(Box.createVerticalStrut(6));
        add(lblSub);

        if (actionText != null && onAction != null) {
            add(Box.createVerticalStrut(16));
            JButton btnAction = UITheme.createPillButton(actionText, UITheme.PRIMARY, Color.WHITE);
            btnAction.setFont(UITheme.FONT_REGULAR);
            btnAction.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnAction.addActionListener(e -> onAction.run());
            add(btnAction);
        }

        add(Box.createVerticalGlue());
    }
}
