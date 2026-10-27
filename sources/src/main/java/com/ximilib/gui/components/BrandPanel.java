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
 * - Nền gradient hồng pastel LibRack, tối dần ở đáy.
 * - Họa tiết nghệ thuật vector sách & thư viện bằng Java2D.
 * - Hỗ trợ nạp ảnh 'login-hero' nếu có.
 * - Carousel 3 slide tự động chuyển mỗi 5 giây, có 3 chấm chỉ báo tương tác được.
 * - Chuyển cảnh 60 FPS siêu mượt (Slide & Fade + Morphing Dots) với đường cong Cubic Ease-Out.
 * - Tự động dừng Timer khi đóng cửa sổ.
 */
public class BrandPanel extends JPanel {

    public record SlideItem(String title, String description) {}

    private static final int ANIM_DURATION_MS = 380; // Thời lượng chuyển cảnh chuẩn UI/UX
    private static final int TIMER_FPS_DELAY = 16;  // ~60 FPS mượt mà

    private final List<SlideItem> slides = new ArrayList<>();
    private int currentSlideIndex = 0;
    private int targetSlideIndex = 0;
    private boolean isTransitioning = false;
    private long animStartTime = 0;
    private float animProgress = 0f;
    private int animDirection = 1; // +1: trượt từ phải sang trái, -1: trượt từ trái sang phải

    private javax.swing.Timer carouselTimer;
    private javax.swing.Timer animTimer;
    private BufferedImage heroImage = null;

    private final SlideContentPanel slideContentPanel;
    private final DotsIndicatorPanel dotsIndicatorPanel;

    public BrandPanel() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(36, 40, 36, 40));

        initSlides();
        loadHeroImageIfPresent();

        // Cụm tiêu đề, mô tả và chấm chỉ báo ở đáy
        JPanel bottomContainer = new JPanel();
        bottomContainer.setOpaque(false);
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.Y_AXIS));

        slideContentPanel = new SlideContentPanel();
        dotsIndicatorPanel = new DotsIndicatorPanel();

        bottomContainer.add(slideContentPanel);
        bottomContainer.add(Box.createVerticalStrut(18));
        bottomContainer.add(dotsIndicatorPanel);

        add(bottomContainer, BorderLayout.SOUTH);

        // Tạm dừng chuyển cảnh tự động khi người dùng di chuột vào để đọc nội dung
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (carouselTimer != null && carouselTimer.isRunning()) {
                    carouselTimer.stop();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                restartCarouselTimer();
            }
        });

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
            startSlideTransition(nextIndex, 1);
        });
        carouselTimer.start();
    }

    private void restartCarouselTimer() {
        if (carouselTimer != null) {
            carouselTimer.restart();
        }
    }

    public void goToSlide(int targetIndex) {
        if (targetIndex >= 0 && targetIndex < slides.size()) {
            int dir = (targetIndex >= currentSlideIndex) ? 1 : -1;
            startSlideTransition(targetIndex, dir);
        }
    }

    private void startSlideTransition(int targetIndex, int direction) {
        if (targetIndex == currentSlideIndex && !isTransitioning) {
            return;
        }

        if (isTransitioning) {
            currentSlideIndex = targetSlideIndex;
        }

        targetSlideIndex = targetIndex;
        animDirection = direction;
        animStartTime = System.currentTimeMillis();
        animProgress = 0.0f;
        isTransitioning = true;

        if (animTimer != null && animTimer.isRunning()) {
            animTimer.stop();
        }

        // Chạy animation 60 FPS
        animTimer = new javax.swing.Timer(TIMER_FPS_DELAY, e -> {
            long elapsed = System.currentTimeMillis() - animStartTime;
            animProgress = Math.min(1.0f, (float) elapsed / ANIM_DURATION_MS);

            slideContentPanel.repaint();
            dotsIndicatorPanel.repaint();

            if (animProgress >= 1.0f) {
                currentSlideIndex = targetSlideIndex;
                isTransitioning = false;
                animTimer.stop();
                slideContentPanel.repaint();
                dotsIndicatorPanel.repaint();
            }
        });
        animTimer.start();
    }

    public void stopTimer() {
        if (carouselTimer != null && carouselTimer.isRunning()) {
            carouselTimer.stop();
        }
        if (animTimer != null && animTimer.isRunning()) {
            animTimer.stop();
        }
    }

    @Override
    public void removeNotify() {
        stopTimer();
        super.removeNotify();
    }

    /**
     * Component hiển thị tiêu đề và mô tả với hiệu ứng Slide & Fade mượt mà 60 FPS
     */
    private class SlideContentPanel extends JPanel {
        public SlideContentPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(340, 92));
            setMinimumSize(new Dimension(240, 92));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            int w = getWidth();
            if (!isTransitioning) {
                drawSlide(g2, slides.get(currentSlideIndex), 1.0f, 0, w);
            } else {
                // Đường cong Cubic Ease-Out: f(t) = 1 - (1 - t)^3
                float ease = 1.0f - (float) Math.pow(1.0f - animProgress, 3);

                // Slide cũ trượt sang bên và mờ dần
                float exitAlpha = Math.max(0.0f, 1.0f - ease);
                float exitOffset = -animDirection * (ease * 32.0f);
                drawSlide(g2, slides.get(currentSlideIndex), exitAlpha, exitOffset, w);

                // Slide mới trượt vào vị trí và sáng dần
                float enterAlpha = Math.min(1.0f, ease);
                float enterOffset = animDirection * ((1.0f - ease) * 32.0f);
                drawSlide(g2, slides.get(targetSlideIndex), enterAlpha, enterOffset, w);
            }
            g2.dispose();
        }

        private void drawSlide(Graphics2D g2, SlideItem slide, float alpha, float offsetX, int maxWidth) {
            if (alpha <= 0.01f) return;

            Graphics2D gSlide = (Graphics2D) g2.create();
            gSlide.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            gSlide.translate(offsetX, 0);

            // Tiêu đề lớn
            Font titleFont = new Font("Segoe UI", Font.BOLD, 22);
            gSlide.setFont(titleFont);
            gSlide.setColor(Color.WHITE);
            FontMetrics fmTitle = gSlide.getFontMetrics();
            int titleY = fmTitle.getAscent();
            gSlide.drawString(slide.title(), 0, titleY);

            // Đoạn mô tả với màu tint tương phản cao
            Font descFont = UITheme.FONT_REGULAR != null ? UITheme.FONT_REGULAR : new Font("Segoe UI", Font.PLAIN, 14);
            gSlide.setFont(descFont);
            gSlide.setColor(UITheme.TINT != null ? UITheme.TINT : new Color(254, 205, 211));
            FontMetrics fmDesc = gSlide.getFontMetrics();

            List<String> lines = wrapText(slide.description(), fmDesc, maxWidth > 50 ? maxWidth : 320);
            int descY = titleY + 12 + fmDesc.getAscent();
            for (String line : lines) {
                gSlide.drawString(line, 0, descY);
                descY += fmDesc.getHeight() + 2;
            }

            gSlide.dispose();
        }

        private List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
            List<String> lines = new ArrayList<>();
            String[] words = text.split(" ");
            StringBuilder sb = new StringBuilder();
            for (String w : words) {
                String test = (sb.length() == 0) ? w : sb + " " + w;
                if (fm.stringWidth(test) <= maxWidth) {
                    sb = new StringBuilder(test);
                } else {
                    if (sb.length() > 0) lines.add(sb.toString());
                    sb = new StringBuilder(w);
                }
            }
            if (sb.length() > 0) lines.add(sb.toString());
            return lines;
        }
    }

    /**
     * Component hiển thị các chấm chỉ báo (Dots) với hiệu ứng Morphing co giãn mượt mà
     */
    private class DotsIndicatorPanel extends JPanel {
        public DotsIndicatorPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(140, 16));
            setMinimumSize(new Dimension(100, 16));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
            setAlignmentX(Component.LEFT_ALIGNMENT);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int clicked = getDotIndexAt(e.getX());
                    if (clicked >= 0 && clicked != currentSlideIndex) {
                        int dir = (clicked > currentSlideIndex) ? 1 : -1;
                        startSlideTransition(clicked, dir);
                        restartCarouselTimer();
                    }
                }
            });

            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int hovered = getDotIndexAt(e.getX());
                    setCursor(hovered >= 0 ? new Cursor(Cursor.HAND_CURSOR) : new Cursor(Cursor.DEFAULT_CURSOR));
                }
            });
        }

        private float getDotWidth(int index, float ease) {
            if (!isTransitioning) {
                return (index == currentSlideIndex) ? 24.0f : 8.0f;
            }
            if (index == currentSlideIndex) {
                // Đang thu nhỏ: 24 -> 8
                return 24.0f - 16.0f * ease;
            } else if (index == targetSlideIndex) {
                // Đang mở rộng: 8 -> 24
                return 8.0f + 16.0f * ease;
            } else {
                return 8.0f;
            }
        }

        private int getDotAlpha(int index, float ease) {
            if (!isTransitioning) {
                return (index == currentSlideIndex) ? 255 : 120;
            }
            if (index == currentSlideIndex) {
                return Math.max(120, (int) (255 - 135 * ease));
            } else if (index == targetSlideIndex) {
                return Math.min(255, (int) (120 + 135 * ease));
            } else {
                return 120;
            }
        }

        private int getDotIndexAt(int mouseX) {
            float ease = isTransitioning ? (1.0f - (float) Math.pow(1.0f - animProgress, 3)) : 0f;
            float currentX = 0f;
            for (int i = 0; i < slides.size(); i++) {
                float w = getDotWidth(i, ease);
                if (mouseX >= currentX - 2 && mouseX <= currentX + w + 2) {
                    return i;
                }
                currentX += w + 8f;
            }
            return -1;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            float ease = isTransitioning ? (1.0f - (float) Math.pow(1.0f - animProgress, 3)) : 0f;
            float currentX = 0f;

            for (int i = 0; i < slides.size(); i++) {
                float dotW = getDotWidth(i, ease);
                int alpha = getDotAlpha(i, ease);

                g2.setColor(new Color(255, 255, 255, alpha));
                g2.fillRoundRect((int) Math.round(currentX), 4, (int) Math.round(dotW), 8, 8, 8);

                currentX += dotW + 8f;
            }
            g2.dispose();
        }
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
