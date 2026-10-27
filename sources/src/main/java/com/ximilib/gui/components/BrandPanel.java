package com.ximilib.gui.components;

import com.ximilib.gui.util.UITheme;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * BrandPanel: Panel nhận diện thương hiệu bên trái của màn hình Đăng Nhập.
 * - Nền gradient xanh lục ngọc bích LibRack, tối dần ở đáy.
 * - Họa tiết nghệ thuật vector sách & thư viện bằng Java2D.
 * - Hỗ trợ nạp ảnh 'login-hero' nếu có.
 * - Carousel 3 slide tự động chuyển mỗi 5 giây, có 3 chấm chỉ báo tương tác được.
 * - Tự động dừng Timer khi đóng cửa sổ.
 */
public class BrandPanel extends JPanel {

    public record SlideItem(String title, String description) {}

    private final List<SlideItem> slides = new ArrayList<>();
    private int currentSlideIndex = 0;
    private float slideAlpha = 1.0f;
    private javax.swing.Timer carouselTimer;
    private javax.swing.Timer fadeTimer;
    private BufferedImage heroImage = null;

    private final JLabel lblSlideTitle;
    private final JLabel lblSlideDesc;
    private final JPanel dotsPanel;
    private final List<JPanel> dotIndicators = new ArrayList<>();

    public BrandPanel() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(36, 40, 36, 40));

        initSlides();
        loadHeroImageIfPresent();

        // 1. Phần tiêu đề và mô tả ở đáy
        JPanel bottomContainer = new JPanel();
        bottomContainer.setOpaque(false);
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.Y_AXIS));

        lblSlideTitle = new JLabel(slides.get(0).title());
        lblSlideTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblSlideTitle.setForeground(Color.WHITE);
        lblSlideTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblSlideDesc = new JLabel("<html>" + slides.get(0).description() + "</html>");
        lblSlideDesc.setFont(UITheme.FONT_REGULAR);
        lblSlideDesc.setForeground(UITheme.TINT); // Hồng pastel nhạt tương phản cao trên nền tối
        lblSlideDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 2. Cụm 3 chấm chỉ báo trang (Carousel dots)
        dotsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        dotsPanel.setOpaque(false);
        dotsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (int i = 0; i < slides.size(); i++) {
            final int index = i;
            JPanel dot = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    boolean isActive = (index == currentSlideIndex);
                    if (isActive) {
                        g2.setColor(Color.WHITE);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                    } else {
                        g2.setColor(new Color(255, 255, 255, 120));
                        g2.fillOval(0, 0, getHeight(), getHeight());
                    }
                    g2.dispose();
                }
            };
            dot.setOpaque(false);
            dot.setCursor(new Cursor(Cursor.HAND_CURSOR));
            dot.setPreferredSize(new Dimension(index == 0 ? 24 : 8, 8));
            dot.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    goToSlide(index);
                    restartCarouselTimer();
                }
            });
            dotIndicators.add(dot);
            dotsPanel.add(dot);
        }

        bottomContainer.add(lblSlideTitle);
        bottomContainer.add(Box.createVerticalStrut(10));
        bottomContainer.add(lblSlideDesc);
        bottomContainer.add(Box.createVerticalStrut(20));
        bottomContainer.add(dotsPanel);

        add(bottomContainer, BorderLayout.SOUTH);

        initTimers();
    }

    private void initSlides() {
        slides.add(new SlideItem(
            "Tra cứu sách nhanh chóng",
            "Tìm theo tên sách, tác giả, thể loại ngay trên ứng dụng với dữ liệu thời gian thực."
        ));
        slides.add(new SlideItem(
            "Mượn trả minh bạch",
            "Theo dõi sách đang mượn, hạn trả và lịch sử mượn trả của bạn một cách rõ ràng."
        ));
        slides.add(new SlideItem(
            "Quản lý thư viện hiệu quả",
            "Hỗ trợ thủ thư lập phiếu, xử lý trả sách và thống kê định kỳ chuẩn xác."
        ));
    }

    private void loadHeroImageIfPresent() {
        String[] possiblePaths = {
            "resources/images/login-hero.jpg",
            "resources/images/login-hero.png",
            "/assets/login-hero.jpg",
            "/assets/librack_exact_dashboard.jpg"
        };

        for (String path : possiblePaths) {
            try {
                if (path.startsWith("/")) {
                    InputStream is = getClass().getResourceAsStream(path);
                    if (is != null) {
                        heroImage = ImageIO.read(is);
                        break;
                    }
                } else {
                    File file = new File(path);
                    if (file.exists() && file.isFile()) {
                        heroImage = ImageIO.read(file);
                        break;
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    private void initTimers() {
        // Timer tự chuyển slide mỗi 5000ms
        carouselTimer = new javax.swing.Timer(5000, e -> {
            int nextIndex = (currentSlideIndex + 1) % slides.size();
            startFadeTransition(nextIndex);
        });
        carouselTimer.start();
    }

    private void restartCarouselTimer() {
        if (carouselTimer != null) {
            carouselTimer.restart();
        }
    }

    private void goToSlide(int targetIndex) {
        if (targetIndex != currentSlideIndex) {
            startFadeTransition(targetIndex);
        }
    }

    private void startFadeTransition(int targetIndex) {
        if (fadeTimer != null && fadeTimer.isRunning()) {
            fadeTimer.stop();
        }

        // Hiệu ứng mờ dần nhẹ
        fadeTimer = new javax.swing.Timer(25, null);
        fadeTimer.addActionListener(e -> {
            slideAlpha -= 0.15f;
            if (slideAlpha <= 0.05f) {
                slideAlpha = 0.05f;
                currentSlideIndex = targetIndex;
                lblSlideTitle.setText(slides.get(currentSlideIndex).title());
                lblSlideDesc.setText("<html>" + slides.get(currentSlideIndex).description() + "</html>");
                updateDotSizes();

                // Đảo chiều fade in
                fadeTimer.stop();
                javax.swing.Timer fadeInTimer = new javax.swing.Timer(25, null);
                fadeInTimer.addActionListener(inEvt -> {
                    slideAlpha += 0.15f;
                    if (slideAlpha >= 1.0f) {
                        slideAlpha = 1.0f;
                        fadeInTimer.stop();
                    }
                    lblSlideTitle.setForeground(new Color(1f, 1f, 1f, slideAlpha));
                    lblSlideDesc.setForeground(new Color(
                        UITheme.TINT.getRed() / 255f,
                        UITheme.TINT.getGreen() / 255f,
                        UITheme.TINT.getBlue() / 255f,
                        slideAlpha
                    ));
                    repaint();
                });
                fadeInTimer.start();
            }
            lblSlideTitle.setForeground(new Color(1f, 1f, 1f, Math.max(0.05f, slideAlpha)));
            lblSlideDesc.setForeground(new Color(
                UITheme.TINT.getRed() / 255f,
                UITheme.TINT.getGreen() / 255f,
                UITheme.TINT.getBlue() / 255f,
                Math.max(0.05f, slideAlpha)
            ));
            repaint();
        });
        fadeTimer.start();
    }

    private void updateDotSizes() {
        for (int i = 0; i < dotIndicators.size(); i++) {
            JPanel dot = dotIndicators.get(i);
            boolean isActive = (i == currentSlideIndex);
            dot.setPreferredSize(new Dimension(isActive ? 24 : 8, 8));
            dot.revalidate();
            dot.repaint();
        }
    }

    public void stopTimer() {
        if (carouselTimer != null && carouselTimer.isRunning()) {
            carouselTimer.stop();
        }
        if (fadeTimer != null && fadeTimer.isRunning()) {
            fadeTimer.stop();
        }
    }

    @Override
    public void removeNotify() {
        stopTimer();
        super.removeNotify();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth();
        int h = getHeight();
        int arc = 20;

        // Clip vùng bo góc bên trái (Top-left và Bottom-left)
        java.awt.geom.Path2D.Double clipPath = new java.awt.geom.Path2D.Double();
        clipPath.moveTo(arc, 0);
        clipPath.lineTo(w, 0);
        clipPath.lineTo(w, h);
        clipPath.lineTo(arc, h);
        clipPath.curveTo(0, h, 0, h - arc, 0, h - arc);
        clipPath.lineTo(0, arc);
        clipPath.curveTo(0, 0, arc, 0, arc, 0);
        clipPath.closePath();
        g2.clip(clipPath);

        // 1. Nền Gradient hồng pastel dọc: pastelMid (trên) -> #F472B6 (giữa) -> #9D174D (đáy)
        LinearGradientPaint bgGradient = new LinearGradientPaint(
            0, 0, 0, h,
            new float[]{0.0f, 0.45f, 1.0f},
            new Color[]{UITheme.PASTEL_MID, UITheme.BRAND_GRADIENT_MID, UITheme.PRIMARY_PRESSED}
        );
        g2.setPaint(bgGradient);
        g2.fillRect(0, 0, w, h);

        // 2. Nếu có ảnh cover, vẽ ảnh dạng cover với lớp phủ tối
        if (heroImage != null) {
            double imgAspect = (double) heroImage.getWidth() / heroImage.getHeight();
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
            g2.drawImage(heroImage, dx, dy, dw, dh, null);

            // Phủ lớp gradient hồng pastel tối bảo đảm chữ trắng đạt chuẩn WCAG >= 4.5:1
            GradientPaint overlay = new GradientPaint(
                0, 0, new Color(UITheme.PASTEL_MID.getRed(), UITheme.PASTEL_MID.getGreen(), UITheme.PASTEL_MID.getBlue(), 120),
                0, h, new Color(UITheme.PRIMARY_PRESSED.getRed(), UITheme.PRIMARY_PRESSED.getGreen(), UITheme.PRIMARY_PRESSED.getBlue(), 230)
            );
            g2.setPaint(overlay);
            g2.fillRect(0, 0, w, h);
        }

        // 3. Họa tiết nghệ thuật vector Java2D: Vòm sách & Khung kệ cách điệu
        drawVectorArtPatterns(g2, w, h);

        g2.dispose();
        super.paintComponent(g);
    }

    private void drawVectorArtPatterns(Graphics2D g2, int w, int h) {
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(new Color(255, 255, 255, 28));

        // Vòng tròn ánh sáng mờ lớn ở góc trên
        g2.drawOval(-40, -40, 260, 260);
        g2.drawOval(-10, -10, 200, 200);

        // Biểu tượng cuốn sách mở nghệ thuật ở nửa trên
        int cx = w / 2;
        int cy = h / 3 - 20;

        // Vòng tròn hào quang sách
        g2.setColor(new Color(255, 255, 255, 35));
        g2.fillOval(cx - 50, cy - 50, 100, 100);

        g2.setColor(new Color(255, 255, 255, 180));
        g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Cuốn sách mở đôi
        java.awt.geom.Path2D.Double leftPage = new java.awt.geom.Path2D.Double();
        leftPage.moveTo(cx, cy + 18);
        leftPage.curveTo(cx - 15, cy + 12, cx - 35, cy + 14, cx - 40, cy + 6);
        leftPage.lineTo(cx - 40, cy - 18);
        leftPage.curveTo(cx - 35, cy - 10, cx - 15, cy - 12, cx, cy - 6);
        leftPage.closePath();
        g2.draw(leftPage);

        java.awt.geom.Path2D.Double rightPage = new java.awt.geom.Path2D.Double();
        rightPage.moveTo(cx, cy + 18);
        rightPage.curveTo(cx + 15, cy + 12, cx + 35, cy + 14, cx + 40, cy + 6);
        rightPage.lineTo(cx + 40, cy - 18);
        rightPage.curveTo(cx + 35, cy - 10, cx + 15, cy - 12, cx, cy - 6);
        rightPage.closePath();
        g2.draw(rightPage);

        // Các đường vân trang sách
        g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(255, 255, 255, 120));
        g2.drawLine(cx - 32, cy - 8, cx - 8, cy - 3);
        g2.drawLine(cx - 32, cy - 1, cx - 8, cy + 4);
        g2.drawLine(cx - 32, cy + 6, cx - 8, cy + 11);

        g2.drawLine(cx + 8, cy - 3, cx + 32, cy - 8);
        g2.drawLine(cx + 8, cy + 4, cx + 32, cy - 1);
        g2.drawLine(cx + 8, cy + 11, cx + 32, cy + 6);

        // Hạt bụi sao li ti mờ
        g2.setColor(new Color(255, 255, 255, 80));
        g2.fillOval(cx - 70, cy - 30, 4, 4);
        g2.fillOval(cx + 65, cy - 20, 3, 3);
        g2.fillOval(cx - 45, cy + 45, 3, 3);
        g2.fillOval(cx + 55, cy + 40, 5, 5);
        g2.fillOval(cx, cy - 45, 4, 4);
    }
}
