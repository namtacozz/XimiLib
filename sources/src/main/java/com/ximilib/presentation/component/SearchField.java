package com.ximilib.presentation.component;

import com.ximilib.presentation.util.IconFactory;
import com.ximilib.presentation.util.UITheme;
import java.awt.*;
import javax.swing.*;

public class SearchField extends JTextField {

    private final String placeholder;
    private final boolean isFlatLaf;

    public SearchField(String placeholder) {
        this.placeholder = placeholder;
        this.isFlatLaf = UIManager.getLookAndFeel().getClass().getName().contains("Flat");

        setFont(UITheme.FONT_REGULAR);
        setForeground(UITheme.TEXT_MAIN);
        setBackground(UITheme.BG_LIGHT);
        setCaretColor(UITheme.TEXT_MAIN);

        putClientProperty("JTextField.placeholderText", placeholder);
        putClientProperty("JTextField.showClearButton", true);
        putClientProperty("JComponent.roundRect", true);

        Icon searchIcon = IconFactory.createSearchIcon(UITheme.TEXT_MUTED, 16);
        putClientProperty("JTextField.leadingIcon", searchIcon);

        if (isFlatLaf) {

            setBorder(new UITheme.RoundedBorder(UITheme.BORDER, 20));
        } else {

            setBorder(BorderFactory.createCompoundBorder(
                new UITheme.RoundedBorder(UITheme.BORDER, 20),
                BorderFactory.createEmptyBorder(6, 36, 6, 14)
            ));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!isFlatLaf) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(UITheme.TEXT_MUTED);
            g2.setStroke(new BasicStroke(1.8f));
            int cy = getHeight() / 2;
            int r = 5;
            g2.drawOval(14, cy - r - 1, r * 2, r * 2);
            g2.drawLine(14 + r * 2 - 1, cy + r - 2, 14 + r * 2 + 5, cy + r + 4);

            if (getText() == null || getText().isEmpty()) {
                g2.setFont(getFont());
                g2.setColor(UITheme.TEXT_MUTED);
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(placeholder, 36, y);
            }
            g2.dispose();
        }
    }
}
