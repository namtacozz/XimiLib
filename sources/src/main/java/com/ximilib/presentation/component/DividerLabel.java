package com.ximilib.presentation.component;

import com.ximilib.presentation.util.UITheme;
import java.awt.*;
import javax.swing.JComponent;

/**
 * DividerLabel: Thành phần phân cách vẽ thuần Graphics2D.
 * Gồm 2 đường kẻ ngang màu CARD_BORDER và chữ (mặc định "hoặc") màu TEXT_MUTED ở giữa.
 * Loại bỏ 100% việc dùng các ký tự Unicode em-dash (—) gây lỗi ô vuông (tofu glyph).
 */
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

        int gap = 12; // Khoảng cách giữa đường kẻ và chữ
        int textX = (w - textW) / 2;
        int textY = centerY + (textH / 2) - 1;

        // 1. Vẽ vạch kẻ bên trái
        g2.setColor(UITheme.CARD_BORDER);
        g2.setStroke(new BasicStroke(1.0f));
        int leftEnd = Math.max(0, textX - gap);
        if (leftEnd > 10) {
            g2.drawLine(10, centerY, leftEnd, centerY);
        }

        // 2. Vẽ chữ "hoặc" ở giữa
        g2.setColor(getForeground());
        g2.drawString(text, textX, textY);

        // 3. Vẽ vạch kẻ bên phải
        g2.setColor(UITheme.CARD_BORDER);
        int rightStart = textX + textW + gap;
        if (w - 10 > rightStart) {
            g2.drawLine(rightStart, centerY, w - 10, centerY);
        }

        g2.dispose();
    }
}
