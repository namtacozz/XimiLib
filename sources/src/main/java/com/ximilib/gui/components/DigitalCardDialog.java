package com.ximilib.gui.components;

import com.ximilib.gui.util.UITheme;
import java.awt.*;
import javax.swing.*;

/**
 * DigitalCardDialog: Dialog hiển thị Thẻ Thư Viện Điện Tử tích hợp mã QR Code
 * mô phỏng theo widget smartphone của LibRack.
 * Tác giả: Phan Vũ Xuân Kiên
 */
public class DigitalCardDialog extends JDialog {

    public DigitalCardDialog(Frame parent, String cardId, String memberName) {
        super(parent, "Thẻ Thư Viện Điện Tử - XimiLib", true);
        setSize(340, 480);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(UITheme.CARD_BG);
        setLayout(new BorderLayout());

        JPanel container = new JPanel();
        container.setBackground(UITheme.CARD_BG);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // 1. Icon thư viện xanh ngọc
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY);
                g2.fillRoundRect(getWidth() / 2 - 20, 0, 40, 40, 12, 12);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("📖", getWidth() / 2 - fm.stringWidth("📖") / 2, 27);
                g2.dispose();
            }
        };
        iconPanel.setPreferredSize(new Dimension(300, 44));
        iconPanel.setMaximumSize(new Dimension(300, 44));
        iconPanel.setOpaque(false);

        // 2. Tiêu đề
        JLabel lblTitle = new JLabel("Thẻ Thư Viện Điện Tử");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Xuất trình mã QR này cho thủ thư tại quầy");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 3. Khung vẽ mã QR Code mô phỏng
        JPanel qrBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Nền bo góc của QR
                g2.setColor(new Color(248, 250, 252));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(UITheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

                // Vẽ các khối định vị QR (3 góc vuông lớn)
                g2.setColor(Color.BLACK);
                drawFinderPattern(g2, 25, 25, 36);
                drawFinderPattern(g2, getWidth() - 61, 25, 36);
                drawFinderPattern(g2, 25, getHeight() - 61, 36);

                // Vẽ các ô đen ngẫu nhiên mô phỏng ma trận QR
                int startX = 25, startY = 25;
                int size = 6;
                for (int r = 0; r < 20; r++) {
                    for (int c = 0; c < 20; c++) {
                        // Tránh 3 góc finder pattern
                        if ((r < 7 && c < 7) || (r < 7 && c > 12) || (r > 12 && c < 7)) continue;
                        if ((r * 7 + c * 13 + cardId.hashCode()) % 3 == 0) {
                            g2.fillRect(startX + c * size, startY + r * size, size - 1, size - 1);
                        }
                    }
                }
                g2.dispose();
            }

            private void drawFinderPattern(Graphics2D g2, int x, int y, int s) {
                g2.fillRect(x, y, s, s);
                g2.setColor(Color.WHITE);
                g2.fillRect(x + 4, y + 4, s - 8, s - 8);
                g2.setColor(Color.BLACK);
                g2.fillRect(x + 8, y + 8, s - 16, s - 16);
            }
        };
        qrBox.setPreferredSize(new Dimension(170, 170));
        qrBox.setMaximumSize(new Dimension(170, 170));
        qrBox.setOpaque(false);
        qrBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 4. Số thẻ & Tên thành viên
        JLabel lblNumber = new JLabel("Mã thẻ: " + cardId);
        lblNumber.setFont(UITheme.FONT_BOLD);
        lblNumber.setForeground(UITheme.TEXT_MAIN);
        lblNumber.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblName = new JLabel(memberName + " • ĐHBK Đà Nẵng");
        lblName.setFont(UITheme.FONT_SMALL);
        lblName.setForeground(UITheme.TEXT_MUTED);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 5. Nút bấm thao tác
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnRow.setOpaque(false);

        JButton btnDownload = UITheme.createPillButton("Tải Ảnh QR", UITheme.PRIMARY, Color.WHITE);
        btnDownload.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Đã lưu mã QR thẻ thư viện vào thiết bị!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnClose = UITheme.createPillButton("Đóng", new Color(241, 245, 249), UITheme.TEXT_MAIN);
        btnClose.addActionListener(e -> dispose());

        btnRow.add(btnDownload);
        btnRow.add(btnClose);

        container.add(iconPanel);
        container.add(Box.createVerticalStrut(6));
        container.add(lblTitle);
        container.add(Box.createVerticalStrut(4));
        container.add(lblSub);
        container.add(Box.createVerticalStrut(16));
        container.add(qrBox);
        container.add(Box.createVerticalStrut(14));
        container.add(lblNumber);
        container.add(Box.createVerticalStrut(2));
        container.add(lblName);
        container.add(Box.createVerticalStrut(20));
        container.add(btnRow);

        add(container, BorderLayout.CENTER);
    }
}
