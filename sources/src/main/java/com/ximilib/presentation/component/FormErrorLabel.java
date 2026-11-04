package com.ximilib.presentation.component;

import com.ximilib.presentation.util.UITheme;

import javax.swing.JLabel;

public class FormErrorLabel extends JLabel {

    public FormErrorLabel() {
        InitComponent("");
    }

    public FormErrorLabel(String message) {
        InitComponent(message);
    }

    private void InitComponent(String message) {
        setFont(UITheme.FONT_SMALL);
        setForeground(UITheme.DANGER_TEXT);
        setText(message);
        setVisible(message != null && !message.trim().isEmpty());
    }

    public void SetErrorMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            setText("");
            setVisible(false);
        } else {
            setText(message);
            setVisible(true);
        }
    }

    public void ClearError() {
        SetErrorMessage("");
    }
}
