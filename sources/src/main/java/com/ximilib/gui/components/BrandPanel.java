package com.ximilib.gui.components;

import com.ximilib.presentation.util.UITheme;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * BrandPanel: Panel hiển thị hình ảnh thương hiệu ở màn hình Đăng Nhập.
 * - Hiển thị ảnh đơn tinh tế plushy-woman-work.jpg.
 * - Lớp phủ gradient tương phản cao đạt chuẩn WCAG >= 4.5:1.
 * - Thông điệp tri thức và nhận diện XimiLib.
 */
public class BrandPanel extends JPanel {

    private static final String IMAGE_PATH = "/images/login/plushy-woman-work.jpg";
    private BufferedImage brandImage = null;

    public BrandPanel() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(36, 40, 36, 40));

        loadBrandImage();

        // Khung thông điệp thương hiệu ở góc dưới
        JPanel bottomContainer = new JPanel();
        bottomContainer.setOpaque(false);
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.Y_AXIS));

        // Pill nhãn thương hiệu
        JLabel lblPill = new JLabel("XimiLib  ·  Library OS") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 38));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(new Color(255, 255, 255, 60));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblPill.setFont(UITheme.FONT_BADGE);
        lblPill.setForeground(Color.WHITE);
        lblPill.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));
        lblPill.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Tiêu đề chính
        JLabel lblTitle = new JLabel("Không gian đọc thân thiện");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Mô tả phụ
        JLabel lblDesc = new JLabel("<html><body style='width: 320px; color: #F1F5F9; font-size: 11px; line-height: 1.4;'>"
            + "Đồng hành cùng bạn đọc và thủ thư trong từng trang sách và câu chuyện mỗi ngày."
            + "</body></html>");
        lblDesc.setFont(UITheme.FONT_REGULAR);
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        bottomContainer.add(lblPill);
        bottomContainer.add(Box.createVerticalStrut(12));
        bottomContainer.add(lblTitle);
        bottomContainer.add(Box.createVerticalStrut(8));
        bottomContainer.add(lblDesc);

        add(bottomContainer, BorderLayout.SOUTH);
    }

    private void loadBrandImage() {
        try (InputStream is = getClass().getResourceAsStream(IMAGE_PATH)) {
            if (is != null) {
                brandImage = ImageIO.read(is);
                return;
            }
        } catch (Exception ignored) {}

        try {
            File f = new File("sources/src/main/resources" + IMAGE_PATH);
            if (f.exists()) {
                brandImage = ImageIO.read(f);
                return;
            }
            File f2 = new File("src/main/resources" + IMAGE_PATH);
            if (f2.exists()) {
                brandImage = ImageIO.read(f2);
            }
        } catch (Exception ignored) {}
    }

    public void stopTimer() {
        // Tĩnh 1 ảnh duy nhất, không dùng timer
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth();
        int h = getHeight();

        // 1. Nền dự phòng màu tím hồng chủ đạo
        g2.setColor(UITheme.PRIMARY);
        g2.fillRect(0, 0, w, h);

        // 2. Vẽ hình ảnh bìa theo tỉ lệ cover fill
        if (brandImage != null) {
            double imgAspect = (double) brandImage.getWidth() / brandImage.getHeight();
            double panelAspect = (double) w / h;
            int dw, dh, dx, dy;
            if (panelAspect > imgAspect) {
                dw = w;
                dh = (int) (w / imgAspect);
                dx = 0;
                dy = (h - dh) / 2;
            } else {
                dh = h;
                dw = (int) (h * imgAspect);
                dx = (w - dw) / 2;
                dy = 0;
            }
            g2.drawImage(brandImage, dx, dy, dw, dh, null);
        }

        // 3. Phủ gradient tối bảo đảm độ tương phản chữ trắng đạt chuẩn WCAG >= 4.5:1
        GradientPaint overlay = new GradientPaint(
            0, 0, new Color(15, 23, 42, 70),
            0, h, new Color(131, 24, 67, 235)
        );
        g2.setPaint(overlay);
        g2.fillRect(0, 0, w, h);

        g2.dispose();
        super.paintComponent(g);
    }
}
