package com.ximilib.gui.components;

import com.ximilib.gui.util.UITheme;
import java.awt.*;
import javax.swing.*;

/**
 * EventCard: Thẻ hiển thị tin tức / sự kiện thư viện theo mục "News & Events" của LibRack.
 * Tác giả: Phan Vũ Xuân Kiên
 */
public class EventCard extends JPanel {

    public enum EventType {
        EVENT,
        ANNOUNCEMENT
    }

    public EventCard(EventType type, String title, String timeInfo, String locationOrDesc, String actionText) {
        setOpaque(false);
        setLayout(new BorderLayout(0, 8));
        setPreferredSize(new Dimension(245, 175));
        setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // 1. Khung ảnh banner trên đầu
        Color bannerBg = (type == EventType.EVENT) ? new Color(139, 92, 246) : new Color(249, 115, 22);
        JPanel bannerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Nền bo góc trên
                g2.setColor(bannerBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.fillRect(0, getHeight() - 10, getWidth(), 10); // Vuông góc dưới để khớp thân card

                // Biểu tượng
                g2.setColor(new Color(255, 255, 255, 180));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
                String sym = (type == EventType.EVENT) ? "📚" : "📢";
                g2.drawString(sym, getWidth() / 2 - 14, getHeight() / 2 + 8);

                g2.dispose();
            }
        };
        bannerPanel.setPreferredSize(new Dimension(245, 75));
        bannerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));

        // Tag con nhộng trên banner
        JLabel lblTag = new JLabel(type == EventType.EVENT ? "SỰ KIỆN" : "THÔNG BÁO") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblTag.setFont(UITheme.FONT_BADGE);
        lblTag.setForeground(Color.WHITE);
        lblTag.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        bannerPanel.add(lblTag);

        // 2. Nội dung text bên dưới
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

        JLabel lblTitle = new JLabel("<html><b>" + title + "</b></html>");
        lblTitle.setFont(UITheme.FONT_BOLD);
        lblTitle.setForeground(UITheme.TEXT_MAIN);

        JLabel lblTime = new JLabel("🕒 " + timeInfo);
        lblTime.setFont(UITheme.FONT_SMALL);
        lblTime.setForeground(UITheme.TEXT_MUTED);

        JLabel lblDetail = new JLabel(locationOrDesc);
        lblDetail.setFont(UITheme.FONT_SMALL);
        lblDetail.setForeground(UITheme.TEXT_MUTED);

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(lblTime);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(lblDetail);

        // 3. Nút hành động con nhộng
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomRow.setOpaque(false);

        Color btnColor = (type == EventType.EVENT) ? UITheme.PRIMARY : UITheme.PURPLE_TEXT;
        JButton btnAction = UITheme.createPillButton(actionText, btnColor, Color.WHITE);
        btnAction.setFont(UITheme.FONT_BADGE);
        btnAction.setMargin(new Insets(2, 8, 2, 8));
        bottomRow.add(btnAction);

        add(bannerPanel, BorderLayout.NORTH);
        add(textPanel, BorderLayout.CENTER);
        add(bottomRow, BorderLayout.SOUTH);
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
