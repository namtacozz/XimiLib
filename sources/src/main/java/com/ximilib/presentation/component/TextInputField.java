package com.ximilib.presentation.component;

import com.formdev.flatlaf.FlatClientProperties;
import com.ximilib.presentation.util.UITheme;

import javax.swing.JTextField;
import java.awt.Dimension;

public class TextInputField extends JTextField {

    public TextInputField(String placeholder) {
        InitComponent(placeholder);
    }

    public TextInputField(String placeholder, int columns) {
        super(columns);
        InitComponent(placeholder);
    }

    private void InitComponent(String placeholder) {
        setFont(UITheme.FONT_REGULAR);
        setForeground(UITheme.TEXT_MAIN);
        putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        putClientProperty(FlatClientProperties.STYLE, "arc: 8; margin: 4,10,4,10");
        setPreferredSize(new Dimension(getPreferredSize().width, 38));
    }

    public void SetError(boolean hasError) {
        if (hasError) {
            putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
        } else {
            putClientProperty(FlatClientProperties.OUTLINE, null);
        }
        repaint();
    }
}
