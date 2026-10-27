package com.ximilib.presentation.component;

import com.ximilib.gui.util.UITheme;
import com.ximilib.presentation.util.UiModalHelper;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
        container.setLayout(new BorderLayout(0, 12));
        container.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        container.setPreferredSize(new Dimension(520, 480));

        // Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Thông Báo Hệ Thống");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightHeader.setOpaque(false);

        JLabel lblBadge = new JLabel(" 2 Mới ");
        lblBadge.setFont(UITheme.FONT_BADGE);
        lblBadge.setOpaque(true);
        lblBadge.setBackground(UITheme.DANGER_BG);
        lblBadge.setForeground(UITheme.DANGER_TEXT);
        lblBadge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        JButton btnCloseX = createCloseVectorButton();

        rightHeader.add(lblBadge);
        rightHeader.add(btnCloseX);

        topPanel.add(lblTitle, BorderLayout.WEST);
        topPanel.add(rightHeader, BorderLayout.EAST);

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

        // Bảng danh sách cuộn: cài đặt Scrollable để ép bề ngang luôn khớp Viewport, triệt tiêu 100% cuộn ngang
        ScrollableListPanel listPanel = new ScrollableListPanel();
        listPanel.setOpaque(false);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        for (NotificationItem item : list) {
            JPanel itemRow = createItemRow(item);
            listPanel.add(itemRow);
            listPanel.add(Box.createVerticalStrut(10));
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(14);

        // Footer nút Đóng
        JButton btnClose = UITheme.createPillButton("Đóng Hộp Thư", UITheme.TINT_LIGHT, UITheme.TEXT_MAIN);
        btnClose.setFont(UITheme.FONT_SMALL);
        btnClose.setPreferredSize(new Dimension(Integer.MAX_VALUE, 34));
        btnClose.addActionListener(e -> UiModalHelper.closePopup());

        container.add(topPanel, BorderLayout.NORTH);
        container.add(scroll, BorderLayout.CENTER);
        container.add(btnClose, BorderLayout.SOUTH);

        add(container, BorderLayout.CENTER);
    }

    private JPanel createItemRow(NotificationItem item) {
        JPanel row = new JPanel(new BorderLayout(8, 6));
        row.setBackground(item.isWarning() ? new Color(254, 242, 242) : UITheme.BG_LIGHT);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(item.isWarning() ? new Color(254, 202, 202) : UITheme.BORDER, 1),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        JLabel lblItemTitle = new JLabel(item.title());
        lblItemTitle.setFont(UITheme.FONT_BOLD);
        lblItemTitle.setForeground(item.isWarning() ? UITheme.DANGER_TEXT : UITheme.TEXT_MAIN);

        JLabel lblMsg = new JLabel("<html><body style='width: 430px; font-size: 11px; line-height: 1.4; color: " + UITheme.HEX_TEXT_MUTED + ";'>" + item.message() + "</body></html>");

        JLabel lblTime = new JLabel(item.time());
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTime.setForeground(UITheme.TEXT_MUTED);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(lblItemTitle, BorderLayout.WEST);
        top.add(lblTime, BorderLayout.EAST);

        row.add(top, BorderLayout.NORTH);
        row.add(lblMsg, BorderLayout.CENTER);

        return row;
    }

    private JButton createCloseVectorButton() {
        JButton btn = new JButton() {
            private boolean isHover = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        isHover = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        isHover = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

                int w = getWidth();
                int h = getHeight();

                if (isHover) {
                    g2.setColor(new Color(0, 0, 0, 18));
                    g2.fillOval(1, 1, w - 2, h - 2);
                }

                g2.setColor(isHover ? UITheme.TEXT_MAIN : UITheme.TEXT_MUTED);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int pad = 7;
                g2.drawLine(pad, pad, w - pad, h - pad);
                g2.drawLine(w - pad, pad, pad, h - pad);

                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(26, 26));
        btn.setMaximumSize(new Dimension(26, 26));
        btn.setMinimumSize(new Dimension(26, 26));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Đóng");
        btn.addActionListener(e -> UiModalHelper.closePopup());
        return btn;
    }

    private static class ScrollableListPanel extends JPanel implements Scrollable {
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 32;
        }
    }
}
