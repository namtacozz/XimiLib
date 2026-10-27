package com.ximilib.gui.components;

import com.ximilib.gui.util.UITheme;
import java.awt.*;
import javax.swing.*;

/**
 * HeroBannerPanel: Banner màu xanh ngọc bích nổi bật mô phỏng theo
 * "Greenfield Public Library" trên LibRack.
 * Tác giả: Phan Vũ Xuân Kiên
 */
public class HeroBannerPanel extends JPanel {

    public HeroBannerPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(16, 0));
        setPreferredSize(new Dimension(800, 78));
        setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        // 1. Bên trái: Icon tòa nhà thư viện trong ô vuông bo góc mờ
        JPanel iconBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 50));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                
                // Vẽ ký hiệu tòa nhà / cuốn sách tối giản
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.0f));
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                // Khung tòa nhà
                g2.drawRoundRect(cx - 10, cy - 12, 20, 24, 4, 4);
                // Các ô cửa sổ
                g2.fillRect(cx - 6, cy - 8, 4, 4);
                g2.fillRect(cx + 2, cy - 8, 4, 4);
                g2.fillRect(cx - 6, cy - 1, 4, 4);
                g2.fillRect(cx + 2, cy - 1, 4, 4);
                g2.fillRect(cx - 3, cy + 6, 6, 6);
                g2.dispose();
            }
        };
        iconBox.setPreferredSize(new Dimension(48, 48));
        iconBox.setOpaque(false);

        // 2. Ở giữa: Tên thư viện & Địa chỉ
        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Thư viện Trường Đại học Bách Khoa - XimiLib");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblAddress = new JLabel("Địa chỉ: 54 Nguyễn Lương Bằng, P. Hoà Khánh Bắc, Q. Liên Chiểu, Đà Nẵng");
        lblAddress.setFont(UITheme.FONT_SMALL);
        lblAddress.setForeground(UITheme.TINT_LIGHT); // Hồng pastel nhạt đạt tương phản cao

        infoPanel.add(Box.createVerticalGlue());
        infoPanel.add(lblTitle);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(lblAddress);
        infoPanel.add(Box.createVerticalGlue());

        // 3. Bên phải: Pill badge "• Đang mở cửa"
        JPanel statusBadge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Nền trắng mờ cao cấp phong cách Glassmorphism đồng bộ tuyệt đối với icon bên trái
                g2.setColor(new Color(255, 255, 255, 45));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                // Viền trắng mờ mảnh tinh tế
                g2.setColor(new Color(255, 255, 255, 90));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                // Chấm tròn nhỏ trắng
                g2.setColor(Color.WHITE);
                g2.fillOval(12, getHeight() / 2 - 3, 7, 7);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        statusBadge.setOpaque(false);
        statusBadge.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 6));
        statusBadge.setPreferredSize(new Dimension(116, 32));
        
        JLabel lblStatus = new JLabel("   Đang mở cửa");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatus.setForeground(Color.WHITE);
        statusBadge.add(lblStatus);

        JPanel rightWrapper = new JPanel(new GridBagLayout());
        rightWrapper.setOpaque(false);
        rightWrapper.add(statusBadge);

        // Lắp ghép các khối
        add(iconBox, BorderLayout.WEST);
        add(infoPanel, BorderLayout.CENTER);
        add(rightWrapper, BorderLayout.EAST);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Vẽ nền Gradient hồng pastel đậm bảo đảm chữ trắng đạt chuẩn WCAG >= 4.5:1
        GradientPaint gradient = new GradientPaint(
            0, 0, UITheme.PRIMARY,
            getWidth(), getHeight(), UITheme.PRIMARY_PRESSED
        );
        g2.setPaint(gradient);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
        g2.dispose();
        super.paintComponent(g);
    }
}
