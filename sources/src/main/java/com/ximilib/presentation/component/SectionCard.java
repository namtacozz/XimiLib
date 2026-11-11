package com.ximilib.presentation.component;

import com.formdev.flatlaf.FlatClientProperties;
import com.ximilib.presentation.util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

/**
 * Thẻ giao diện chuẩn bo góc (OOP Component Reusability).
 * Thay thế cho các khối 'new JPanel() { paintComponent }' lặp lại trong toàn bộ ứng dụng.
 */
public class SectionCard extends JPanel {

    private int cornerRadius = 16;
    private Color customBg = Color.WHITE;
    private Color customBorder = UITheme.CARD_BORDER;
    private boolean drawCustomBorder = true;

    public SectionCard() {
        this(16);
    }

    public SectionCard(int padding) {
        initComponent(new Insets(padding, padding, padding, padding), 16, Color.WHITE, UITheme.CARD_BORDER);
    }

    public SectionCard(LayoutManager layout, int padding) {
        super(layout);
        initComponent(new Insets(padding, padding, padding, padding), 16, Color.WHITE, UITheme.CARD_BORDER);
    }

    public SectionCard(LayoutManager layout, Insets padding, int radius, Color bg, Color borderColor) {
        super(layout);
        initComponent(padding, radius, bg, borderColor);
    }

    public SectionCard(int radius, Color bg) {
        initComponent(new Insets(0, 0, 0, 0), radius, bg, null);
    }

    public static SectionCard whiteCard(LayoutManager layout, int padding) {
        return new SectionCard(layout, padding);
    }

    public static SectionCard filledCard(int radius, Color bg) {
        return new SectionCard(radius, bg);
    }

    private void initComponent(Insets padding, int radius, Color bg, Color borderColor) {
        this.cornerRadius = radius;
        this.customBg = (bg != null) ? bg : Color.WHITE;
        this.customBorder = borderColor;
        this.drawCustomBorder = (borderColor != null);

        setOpaque(false);
        setBackground(customBg);
        if (padding != null) {
            setBorder(BorderFactory.createEmptyBorder(padding.top, padding.left, padding.bottom, padding.right));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Vẽ nền bo góc
        g2.setColor(customBg != null ? customBg : getBackground());
        g2.fillRoundRect(0, 0, w, h, cornerRadius, cornerRadius);

        // Vẽ viền nếu có cấu hình
        if (drawCustomBorder && customBorder != null) {
            g2.setColor(customBorder);
            g2.drawRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
