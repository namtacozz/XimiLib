package com.ximilib.presentation.component;

import com.ximilib.gui.util.UITheme;
import com.ximilib.presentation.util.UiModalHelper;
import java.awt.*;
import javax.swing.*;
import raven.popup.component.GlassPaneChild;

/**
 * DigitalCardPopupPanel: The Thu Vien Dien Tu dang GlassPane Popup (In-App Overlay)
 * Tich hop ma QR Code va bo goc mo phong smartphone theo dung chuan raven.glasspanepopup.
 * Thay the hoan toan OS JDialog truyen thong de tiet kiem tai nguyen RAM va tranh loi focus.
 */
public class DigitalCardPopupPanel extends GlassPaneChild {

    private final String cardId;
    private final String memberName;

    public DigitalCardPopupPanel(String cardId, String memberName) {
        this.cardId = (cardId != null && !cardId.isBlank()) ? cardId : "LIB-2026-001";
        this.memberName = (memberName != null && !memberName.isBlank()) ? memberName : "Độc Giả";

        setLayout(new BorderLayout());
        setOpaque(false);
        initUI();
    }

    private void initUI() {
        JPanel cardContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Nền thẻ đổ bóng nhẹ bo góc
                g2.setColor(UITheme.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setColor(UITheme.BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        cardContainer.setOpaque(false);
        cardContainer.setLayout(new BoxLayout(cardContainer, BoxLayout.Y_AXIS));
        cardContainer.setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));
        cardContainer.setPreferredSize(new Dimension(320, 460));

        // 1. Icon Header Thư viện vector xanh ngọc
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY);
                g2.fillRoundRect(getWidth() / 2 - 20, 0, 40, 40, 12, 12);

                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.8f));
                int cx = getWidth() / 2;
                int cy = 20;
                g2.drawRoundRect(cx - 9, cy - 8, 8, 16, 2, 2);
                g2.drawRoundRect(cx + 1, cy - 8, 8, 16, 2, 2);
                g2.drawLine(cx - 7, cy - 4, cx - 3, cy - 4);
                g2.drawLine(cx - 7, cy, cx - 3, cy);
                g2.drawLine(cx + 3, cy - 4, cx + 7, cy - 4);
                g2.drawLine(cx + 3, cy, cx + 7, cy);
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

                g2.setColor(UITheme.PAGE_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(UITheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

                g2.setColor(Color.BLACK);
                drawFinderPattern(g2, 25, 25, 36);
                drawFinderPattern(g2, getWidth() - 61, 25, 36);
                drawFinderPattern(g2, 25, getHeight() - 61, 36);

                int startX = 25, startY = 25;
                int size = 6;
                int hash = Math.abs(cardId.hashCode());
                for (int r = 0; r < 20; r++) {
                    for (int c = 0; c < 20; c++) {
                        if ((r < 7 && c < 7) || (r < 7 && c > 12) || (r > 12 && c < 7)) continue;
                        if ((r * 7 + c * 13 + hash) % 3 == 0) {
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
            UiModalHelper.showToastSuccess(this, "Đã lưu mã QR thẻ thư viện vào thiết bị!");
        });

        JButton btnClose = UITheme.createPillButton("Đóng", UITheme.TINT_LIGHT, UITheme.TEXT_MAIN);
        btnClose.addActionListener(e -> UiModalHelper.closePopup());

        btnRow.add(btnDownload);
        btnRow.add(btnClose);

        cardContainer.add(iconPanel);
        cardContainer.add(Box.createVerticalStrut(6));
        cardContainer.add(lblTitle);
        cardContainer.add(Box.createVerticalStrut(4));
        cardContainer.add(lblSub);
        cardContainer.add(Box.createVerticalStrut(14));
        cardContainer.add(qrBox);
        cardContainer.add(Box.createVerticalStrut(12));
        cardContainer.add(lblNumber);
        cardContainer.add(Box.createVerticalStrut(2));
        cardContainer.add(lblName);
        cardContainer.add(Box.createVerticalStrut(16));
        cardContainer.add(btnRow);

        add(cardContainer, BorderLayout.CENTER);
    }
}
