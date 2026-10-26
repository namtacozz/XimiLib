package com.ximilib.gui.components;

import com.ximilib.gui.util.UITheme;
import java.awt.*;
import javax.swing.*;

/**
 * BookArrivalCard: Thẻ sách trong mục "New Arrivals" (Sách Mới Nhập)
 * có chấm tròn trạng thái khả dụng xanh/đỏ và nút yêu thích.
 * Tác giả: Phan Vũ Xuân Kiên
 */
public class BookArrivalCard extends JPanel {

    private boolean isAvailable;

    public BookArrivalCard(String title, String author, Color coverColor, boolean isAvailable) {
        this.isAvailable = isAvailable;
        setOpaque(false);
        setLayout(new BorderLayout(0, 6));
        setPreferredSize(new Dimension(145, 215));
        setBorder(BorderFactory.createEmptyBorder(6, 6, 8, 6));

        // 1. Khung bìa sách mô phỏng
        JPanel coverPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Nền bìa
                g2.setColor(coverColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                // Gáy sách
                g2.setColor(new Color(0, 0, 0, 35));
                g2.fillRect(0, 0, 6, getHeight());

                // Chữ bìa
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2.drawString("XIMILIB", 14, 20);

                // Trái tim yêu thích góc dưới trái vẽ vector
                g2.setColor(new Color(255, 255, 255, 220));
                g2.fillOval(8, getHeight() - 26, 20, 20);
                g2.setColor(new Color(239, 68, 68));
                g2.fillOval(12, getHeight() - 22, 6, 6);
                g2.fillOval(16, getHeight() - 22, 6, 6);
                int[] hx = {12, 22, 17};
                int[] hy = {getHeight() - 19, getHeight() - 19, getHeight() - 12};
                g2.fillPolygon(hx, hy, 3);

                // Chấm tròn trạng thái góc dưới phải (Xanh = còn sách, Đỏ = hết)
                g2.setColor(isAvailable ? UITheme.SUCCESS_GREEN : UITheme.DANGER_TEXT);
                g2.fillOval(getWidth() - 20, getHeight() - 20, 10, 10);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(getWidth() - 20, getHeight() - 20, 10, 10);

                g2.dispose();
            }
        };
        coverPanel.setPreferredSize(new Dimension(133, 155));

        // 2. Tên sách và tác giả
        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_BOLD);
        lblTitle.setForeground(UITheme.TEXT_MAIN);

        JLabel lblAuthor = new JLabel(author);
        lblAuthor.setFont(UITheme.FONT_SMALL);
        lblAuthor.setForeground(UITheme.TEXT_MUTED);

        infoPanel.add(lblTitle);
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(lblAuthor);

        add(coverPanel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(UITheme.CARD_BG);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

        g2.setColor(UITheme.BORDER);
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

        g2.dispose();
        super.paintComponent(g);
    }
}
