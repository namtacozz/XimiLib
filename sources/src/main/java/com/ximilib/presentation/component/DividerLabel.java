package com.ximilib.presentation.component;

import com.ximilib.presentation.util.UITheme;
import java.awt.*;
import javax.swing.JComponent;

public class DividerLabel extends JComponent {

    private final String text;

    public DividerLabel() {
        this("hoặc");
    }

    public DividerLabel(String text) {
        this.text = text != null ? text : "hoặc";
        setFont(new Font("Segoe UI", Font.PLAIN, 12));
        setForeground(UITheme.TEXT_MUTED);
        setPreferredSize(new Dimension(360, 24));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int centerY = h / 2;

        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth(text);
        int textH = fm.getAscent();

        int gap = 12;
        int textX = (w - textW) / 2;
        int textY = centerY + (textH / 2) - 1;

        g2.setColor(UITheme.CARD_BORDER);
        g2.setStroke(new BasicStroke(1.0f));
        int leftEnd = Math.max(0, textX - gap);
        if (leftEnd > 10) {
            g2.drawLine(10, centerY, leftEnd, centerY);
        }

        g2.setColor(getForeground());
        g2.drawString(text, textX, textY);

        g2.setColor(UITheme.CARD_BORDER);
        int rightStart = textX + textW + gap;
        if (w - 10 > rightStart) {
            g2.drawLine(rightStart, centerY, w - 10, centerY);
        }

        g2.dispose();
    }
}
