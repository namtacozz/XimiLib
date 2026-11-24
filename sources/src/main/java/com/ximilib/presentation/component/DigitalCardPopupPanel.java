package com.ximilib.presentation.component;

import com.ximilib.presentation.util.UITheme;
import com.ximilib.presentation.util.AppLogo;
import com.ximilib.presentation.util.QrCodeGenerator;
import com.ximilib.presentation.util.UiModalHelper;
import java.awt.*;
import javax.swing.*;
import raven.popup.component.GlassPaneChild;

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

    public static void show(String cardId, String memberName) {
        UiModalHelper.showQuickPopup(new DigitalCardPopupPanel(cardId, memberName));
    }

    private void initUI() {
        JPanel cardContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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

        JLabel iconPanel = new JLabel(AppLogo.getIcon(40), SwingConstants.CENTER);
        iconPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        iconPanel.setPreferredSize(new Dimension(300, 44));
        iconPanel.setMaximumSize(new Dimension(300, 44));

        JLabel lblTitle = new JLabel("Thẻ Thư Viện Điện Tử");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Xuất trình mã QR này cho thủ thư tại quầy");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        boolean[][] qrMatrix = QrCodeGenerator.encode(cardId);
        JPanel qrBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

                int w = getWidth();
                int h = getHeight();

                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, w, h, 16, 16);
                g2.setColor(UITheme.BORDER);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);

                int mSize = qrMatrix.length;
                int moduleSize = Math.max(2, (Math.min(w, h) - 24) / mSize);
                int qrPixelWidth = mSize * moduleSize;
                int startX = (w - qrPixelWidth) / 2;
                int startY = (h - qrPixelWidth) / 2;

                g2.setColor(Color.BLACK);
                for (int r = 0; r < mSize; r++) {
                    for (int c = 0; c < mSize; c++) {
                        if (qrMatrix[r][c]) {
                            g2.fillRect(startX + c * moduleSize, startY + r * moduleSize, moduleSize, moduleSize);
                        }
                    }
                }
                g2.dispose();
            }
        };
        qrBox.setPreferredSize(new Dimension(180, 180));
        qrBox.setMaximumSize(new Dimension(180, 180));
        qrBox.setOpaque(false);
        qrBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblNumber = new JLabel("Mã thẻ: " + cardId);
        lblNumber.setFont(UITheme.FONT_BOLD);
        lblNumber.setForeground(UITheme.TEXT_MAIN);
        lblNumber.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblName = new JLabel(memberName + " • Thư viện làng Xì Mi");
        lblName.setFont(UITheme.FONT_SMALL);
        lblName.setForeground(UITheme.TEXT_MUTED);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnRow.setOpaque(false);

        JButton btnDownload = UITheme.createPillButton("Tải Ảnh QR", UITheme.PRIMARY, Color.WHITE);
        btnDownload.addActionListener(e -> saveQrToPng(qrMatrix));

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

    private void saveQrToPng(boolean[][] qrMatrix) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("TheThuVien_" + cardId + ".png"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        java.io.File target = chooser.getSelectedFile();
        if (!target.getName().toLowerCase().endsWith(".png")) {
            target = new java.io.File(target.getAbsolutePath() + ".png");
        }
        int size = 360;
        int mSize = qrMatrix.length;
        int moduleSize = Math.max(4, (size - 32) / mSize);
        int qrWidth = mSize * moduleSize;
        int start = (size - qrWidth) / 2;

        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, size, size);
        g2.setColor(Color.BLACK);
        for (int r = 0; r < mSize; r++) {
            for (int c = 0; c < mSize; c++) {
                if (qrMatrix[r][c]) {
                    g2.fillRect(start + c * moduleSize, start + r * moduleSize, moduleSize, moduleSize);
                }
            }
        }
        g2.dispose();
        try {
            javax.imageio.ImageIO.write(image, "png", target);
            UiModalHelper.showToastSuccess(this, "Đã lưu mã QR thẻ thư viện: " + target.getName());
        } catch (Exception ex) {
            UiModalHelper.showToastError(this, "Không thể lưu ảnh QR: " + ex.getMessage());
        }
    }
}
