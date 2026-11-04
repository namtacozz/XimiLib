package com.ximilib.presentation.component;

import com.formdev.flatlaf.FlatClientProperties;
import com.ximilib.presentation.util.UITheme;

import javax.swing.JButton;
import java.awt.Cursor;
import java.awt.Dimension;

public class PrimaryButton extends JButton {

    public PrimaryButton(String text) {
        super(text);
        InitComponent();
    }

    private void InitComponent() {
        setFont(UITheme.FONT_BUTTON);
        setBackground(UITheme.PRIMARY);
        setForeground(UITheme.ON_PRIMARY);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        putClientProperty(FlatClientProperties.STYLE, "arc: 8; margin: 6,16,6,16");
        setPreferredSize(new Dimension(getPreferredSize().width, 38));
    }
}
