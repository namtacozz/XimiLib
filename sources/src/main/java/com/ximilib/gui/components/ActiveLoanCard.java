package com.ximilib.gui.components;

import com.ximilib.gui.util.UITheme;
import java.awt.*;
import javax.swing.*;

/**
 * ActiveLoanCard: Thẻ hiển thị từng đầu sách đang mượn kèm thời hạn và nút "Gia hạn"
 * chuẩn theo mục "My Active Loans" của LibRack.
 * Tác giả: Phan Vũ Xuân Kiên
 */
public class ActiveLoanCard extends JPanel {

    public enum StatusType {
        NORMAL,   // Còn nhiều ngày (xanh lá)
        WARNING,  // Sắp hết hạn (vàng)
        OVERDUE   // Quá hạn (đỏ)
    }

    public ActiveLoanCard(String title, String author, String borrower, 
                          String statusText, StatusType status, boolean canExtend) {
        setOpaque(false);
        setLayout(new BorderLayout(14, 0));
        setPreferredSize(new Dimension(280, 105));
        setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        // 1. Ảnh bìa sách minh họa mô phỏng
        JPanel bookCover = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Nền bìa sách bo góc
                Color coverBg = switch (status) {
                    case NORMAL -> new Color(254, 243, 199); // Vàng kem nhạt
                    case WARNING -> new Color(30, 58, 138);  // Xanh đêm
                    case OVERDUE -> new Color(245, 158, 11); // Cam đất
                };
                g2.setColor(coverBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // Gáy sách mô phỏng
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRect(0, 0, 5, getHeight());

                // Chữ tóm tắt trên bìa
                g2.setColor(status == StatusType.WARNING ? Color.WHITE : UITheme.TEXT_MAIN);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 8));
                String shortTitle = title.length() > 8 ? title.substring(0, 8) + ".." : title;
                g2.drawString(shortTitle, 8, 22);

                g2.dispose();
            }
        };
        bookCover.setPreferredSize(new Dimension(55, 82));
        bookCover.setOpaque(false);

        // 2. Thông tin sách & Hạn trả
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

        if (borrower != null && !borrower.isEmpty()) {
            JLabel lblBorrower = new JLabel("Bạn đọc: " + borrower);
            lblBorrower.setFont(UITheme.FONT_BADGE);
            lblBorrower.setForeground(UITheme.PURPLE_TEXT);
            infoPanel.add(Box.createVerticalStrut(3));
            infoPanel.add(lblBorrower);
        }

        // Hàng trạng thái thời hạn và nút gia hạn
        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        statusRow.setOpaque(false);

        // Pill badge trạng thái hạn trả
        Color badgeBg = switch (status) {
            case NORMAL -> UITheme.PRIMARY_MINT;
            case WARNING -> UITheme.WARNING_BG;
            case OVERDUE -> UITheme.DANGER_BG;
        };
        Color badgeFg = switch (status) {
            case NORMAL -> UITheme.PRIMARY;
            case WARNING -> UITheme.WARNING_TEXT;
            case OVERDUE -> UITheme.DANGER_TEXT;
        };

        JLabel lblBadge = new JLabel(statusText) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(badgeBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblBadge.setOpaque(false);
        lblBadge.setFont(UITheme.FONT_BADGE);
        lblBadge.setForeground(badgeFg);
        lblBadge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        statusRow.add(lblBadge);

        // Nút bấm "Gia hạn"
        if (canExtend) {
            JButton btnExtend = UITheme.createPillButton("Gia hạn", UITheme.PRIMARY, Color.WHITE);
            btnExtend.setFont(UITheme.FONT_BADGE);
            btnExtend.setMargin(new Insets(2, 10, 2, 10));
            btnExtend.addActionListener(e -> {
                JOptionPane.showMessageDialog(this,
                    "Gia hạn thành công cuốn sách: " + title + " thêm 7 ngày!",
                    "Gia Hạn Mượn Sách",
                    JOptionPane.INFORMATION_MESSAGE);
            });
            statusRow.add(btnExtend);
        }

        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(statusRow);

        add(bookCover, BorderLayout.WEST);
        add(infoPanel, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Nền trắng bo tròn
        g2.setColor(UITheme.CARD_BG);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

        // Viền mảnh
        g2.setColor(UITheme.BORDER);
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

        g2.dispose();
        super.paintComponent(g);
    }
}
