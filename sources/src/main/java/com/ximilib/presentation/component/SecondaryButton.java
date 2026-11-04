package com.ximilib.presentation.component;

import com.formdev.flatlaf.FlatClientProperties;
import com.ximilib.presentation.util.UITheme;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;

public class SecondaryButton extends JButton {

    public SecondaryButton(String text) {
        super(text);
        InitComponent();
    }

    private void InitComponent() {
        setFont(UITheme.FONT_BUTTON);
        setBackground(Color.WHITE);
        setForeground(UITheme.TEXT_MAIN);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        putClientProperty(FlatClientProperties.STYLE, "arc: 8; margin: 6,16,6,16; borderColor: " + UITheme.HEX_CARD_BORDER);
        setPreferredSize(new Dimension(getPreferredSize().width, 38));
    }
}
