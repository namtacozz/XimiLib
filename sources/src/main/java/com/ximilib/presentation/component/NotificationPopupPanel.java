package com.ximilib.presentation.component;

import com.ximilib.gui.util.UITheme;
import com.ximilib.presentation.util.UiModalHelper;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import raven.popup.component.GlassPaneChild;

/**
 * NotificationPopupPanel: Hop thu chuong thong bao nhanh tren Header (GlassPanePopup)
 * Hien thi cac thong bao nhac han, qua han va he thong.
 */
public class NotificationPopupPanel extends GlassPaneChild {

    public record NotificationItem(String title, String message, String time, boolean isWarning) {}

    public NotificationPopupPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        initUI();
    }

    private void initUI() {
        JPanel container = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(UITheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        container.setOpaque(false);
        container.setLayout(new BorderLayout(0, 8));
        container.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        container.setPreferredSize(new Dimension(360, 380));

        // Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Thông Báo Hệ Thống");
        lblTitle.setFont(UITheme.FONT_BOLD);
        lblTitle.setForeground(UITheme.TEXT_MAIN);

        JLabel lblBadge = new JLabel(" 2 Mới ");
        lblBadge.setFont(UITheme.FONT_BADGE);
        lblBadge.setOpaque(true);
        lblBadge.setBackground(UITheme.DANGER_BG);
        lblBadge.setForeground(UITheme.DANGER_TEXT);
        lblBadge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        topPanel.add(lblTitle, BorderLayout.WEST);
        topPanel.add(lblBadge, BorderLayout.EAST);

        // Danh sách thông báo mẫu
        List<NotificationItem> list = new ArrayList<>();
        list.add(new NotificationItem(
            "Cảnh báo quá hạn (BR-06)",
            "Phiếu mượn PM001 có cuốn 'Clean Code' trễ 2 ngày. Phí phạt hiện tại: 4.000 VNĐ.",
            "Hôm nay 00:00",
            true
        ));
        list.add(new NotificationItem(
            "Nhắc hạn trả sách sắp tới",
            "Đầu sách 'Thiết Kế Mẫu Phần Mềm' cần hoàn trả trước 17:00 ngày 29/10/2026.",
            "Hôm qua 09:30",
            false
        ));
        list.add(new NotificationItem(
            "Chào mừng bạn đọc mới",
            "Tài khoản của bạn đã được kích hoạt thành công trên hệ thống XimiLib.",
            "25/10/2026",
            false
        ));

        JPanel listPanel = new JPanel();
        listPanel.setOpaque(false);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        for (NotificationItem item : list) {
            JPanel itemRow = createItemRow(item);
            listPanel.add(itemRow);
            listPanel.add(Box.createVerticalStrut(8));
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        // Footer nút Đóng
        JButton btnClose = UITheme.createPillButton("Đóng Hộp Thư", UITheme.TINT_LIGHT, UITheme.TEXT_MAIN);
        btnClose.setFont(UITheme.FONT_SMALL);
        btnClose.addActionListener(e -> UiModalHelper.closePopup());

        container.add(topPanel, BorderLayout.NORTH);
        container.add(scroll, BorderLayout.CENTER);
        container.add(btnClose, BorderLayout.SOUTH);

        add(container, BorderLayout.CENTER);
    }

    private JPanel createItemRow(NotificationItem item) {
        JPanel row = new JPanel(new BorderLayout(8, 4));
        row.setBackground(item.isWarning() ? new Color(254, 242, 242) : UITheme.BG_LIGHT);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(item.isWarning() ? new Color(254, 202, 202) : UITheme.BORDER, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JLabel lblItemTitle = new JLabel(item.title());
        lblItemTitle.setFont(UITheme.FONT_BOLD);
        lblItemTitle.setForeground(item.isWarning() ? UITheme.DANGER_TEXT : UITheme.TEXT_MAIN);

        JLabel lblMsg = new JLabel("<html><body style='width: 250px; font-size: 11px; color: " + UITheme.HEX_TEXT_MUTED + ";'>" + item.message() + "</body></html>");

        JLabel lblTime = new JLabel(item.time());
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblTime.setForeground(UITheme.TEXT_MUTED);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(lblItemTitle, BorderLayout.WEST);
        top.add(lblTime, BorderLayout.EAST);

        row.add(top, BorderLayout.NORTH);
        row.add(lblMsg, BorderLayout.CENTER);

        return row;
    }
}
