package com.ximilib.presentation.component;

import com.formdev.flatlaf.FlatClientProperties;
import com.ximilib.presentation.util.UITheme;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Dimension;

public class StatusBadge extends JLabel {

    public enum BadgeType {
        SUCCESS,
        WARNING,
        DANGER,
        INFO
    }

    public StatusBadge(String text, BadgeType type) {
        super(text, SwingConstants.CENTER);
        InitComponent(type);
    }

    private void InitComponent(BadgeType type) {
        setFont(UITheme.FONT_BADGE);
        setOpaque(true);

        Color bgColor;
        Color fgColor;

        if (type == BadgeType.SUCCESS) {
            bgColor = UITheme.SUCCESS_BG;
            fgColor = UITheme.SUCCESS_TEXT;
        } else if (type == BadgeType.WARNING) {
            bgColor = UITheme.WARNING_BG;
            fgColor = UITheme.WARNING_TEXT;
        } else if (type == BadgeType.DANGER) {
            bgColor = UITheme.DANGER_BG;
            fgColor = UITheme.DANGER_TEXT;
        } else {
            bgColor = UITheme.TINT;
            fgColor = UITheme.ON_TINT;
        }

        setBackground(bgColor);
        setForeground(fgColor);
        putClientProperty(FlatClientProperties.STYLE, "arc: 999; margin: 3,10,3,10");
        setPreferredSize(new Dimension(getPreferredSize().width + 20, 24));
    }
}
