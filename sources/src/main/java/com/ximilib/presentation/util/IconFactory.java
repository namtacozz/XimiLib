package com.ximilib.presentation.util;

import java.awt.*;
import java.awt.geom.Path2D;
import javax.swing.Icon;

/**
 * IconFactory: Tạo các icon vector phẳng trực tiếp bằng Graphics2D.
 * Giải quyết triệt để 100% lỗi font không hỗ trợ Emoji trên Windows gây ra ô vuông (tofu glyph '▯').
 * Tác giả: Phan Vũ Xuân Kiên (GUI / DB)
 */
public class IconFactory {

    public interface DynamicIcon extends Icon {
        void setColor(Color c);
    }

    /**
     * Tạo icon kính lúp (Tìm kiếm)
     */
    public static Icon createSearchIcon(Color color, int size) {
        return new VectorIcon(size, size, color, (g2, w, h, c) -> {
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.8f));
            int r = (w - 7) / 2;
            int cx = 2 + r;
            int cy = 2 + r;
            g2.drawOval(cx - r, cy - r, r * 2, r * 2);
            g2.drawLine(cx + r - 1, cy + r - 1, w - 2, h - 2);
        });
    }

    /**
     * Tạo icon Ngôi Nhà 2D (Trang chủ) nhìn thẳng trực diện phẳng,
     * tối giản, hiện đại và đồng bộ với giao diện 2D.
     */
    public static Icon createHomeIcon(Color color, int size) {
        return new VectorIcon(size, size, color, (g2, w, h, c) -> {
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            int cx = w / 2;

            // 1. Ống khói nhỏ 2D trên mái nhà
            g2.fillRect(w - 5, 2, 2, 3);

            // 2. Mái nhà 2D tam giác phẳng
            int[] roofX = {cx, 1, w - 1};
            int[] roofY = {2, 7, 7};
            g2.drawPolygon(roofX, roofY, 3);

            // 3. Thân nhà 2D hình chữ nhật
            g2.drawRect(3, 7, w - 6, h - 8);

            // 4. Cửa chính 2D ở giữa
            int doorW = 4;
            int doorH = 5;
            g2.drawRect(cx - doorW / 2, h - 1 - doorH, doorW, doorH);
            g2.fillRect(cx + 1, h - 1 - doorH / 2, 1, 1); // Núm tay nắm cửa
        });
    }

    /**
     * Tạo icon cuốn sách (Kho sách)
     */
    public static Icon createBookIcon(Color color, int size) {
        return new VectorIcon(size, size, color, (g2, w, h, c) -> {
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.8f));
            // 2 trang sách mở
            g2.drawRoundRect(2, 3, w / 2 - 2, h - 6, 3, 3);
            g2.drawRoundRect(w / 2, 3, w / 2 - 2, h - 6, 3, 3);
            // Các dòng chữ mô phỏng
            g2.drawLine(4, 7, w / 2 - 3, 7);
            g2.drawLine(4, 10, w / 2 - 3, 10);
            g2.drawLine(w / 2 + 3, 7, w - 4, 7);
            g2.drawLine(w / 2 + 3, 10, w - 4, 10);
        });
    }

    /**
     * Tạo icon nhãn thẻ (Thể loại)
     */
    public static Icon createTagIcon(Color color, int size) {
        return new VectorIcon(size, size, color, (g2, w, h, c) -> {
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.8f));
            int[] xs = {3, w - 6, w - 2, w - 6, 3};
            int[] ys = {3, 3, h / 2, h - 3, h - 3};
            g2.drawPolygon(xs, ys, 5);
            g2.fillOval(5, h / 2 - 2, 3, 3);
        });
    }

    /**
     * Tạo icon người dùng / độc giả
     */
    public static Icon createUsersIcon(Color color, int size) {
        return new VectorIcon(size, size, color, (g2, w, h, c) -> {
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.8f));
            // Đầu
            g2.drawOval(w / 2 - 3, 2, 6, 6);
            // Vai
            g2.drawArc(2, h / 2 + 1, w - 4, h / 2, 0, 180);
        });
    }

    /**
     * Tạo icon Mặt Trên Bàn Chế Tạo 2D (Crafting Grid 3x3) của Bàn Lưu Hành.
     * Tái hiện chính xác 100% mặt trên cùng của khối Crafting Table người dùng gửi:
     * Khung vuông viền gỗ với 4 chốt góc và lưới chế tạo 3x3 (9 ô vuông phẳng).
     */
    public static Icon createCirculationIcon(Color color, int size) {
        return new VectorIcon(size, size, color, (g2, w, h, c) -> {
            g2.setColor(c);
            
            int pad = 1;
            int boxSize = Math.min(w, h) - pad * 2 - 1;
            int startX = (w - boxSize) / 2;
            int startY = (h - boxSize) / 2;

            // 1. Viền ngoài của mặt bàn 2D
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawRect(startX, startY, boxSize, boxSize);

            // 2. Lưới chế tạo 3x3 (9 ô vuông như mặt trên khối Minecraft)
            g2.setStroke(new BasicStroke(1.0f));
            int step = boxSize / 3;
            
            // 2 đường dọc chia 3 cột
            g2.drawLine(startX + step, startY, startX + step, startY + boxSize);
            g2.drawLine(startX + step * 2, startY, startX + step * 2, startY + boxSize);
            
            // 2 đường ngang chia 3 hàng
            g2.drawLine(startX, startY + step, startX + boxSize, startY + step);
            g2.drawLine(startX, startY + step * 2, startX + boxSize, startY + step * 2);

            // 3. Bốn chốt đinh tán gỗ đặc trưng ở 4 góc của mặt trên Crafting Table
            g2.fillRect(startX + 1, startY + 1, 2, 2);
            g2.fillRect(startX + boxSize - 3, startY + 1, 2, 2);
            g2.fillRect(startX + 1, startY + boxSize - 3, 2, 2);
            g2.fillRect(startX + boxSize - 3, startY + boxSize - 3, 2, 2);
        });
    }

    /**
     * Tạo icon xử lý trả & bồi hoàn (dấu tích trong ô)
     */
    public static Icon createCheckIcon(Color color, int size) {
        return new VectorIcon(size, size, color, (g2, w, h, c) -> {
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawRoundRect(2, 2, w - 4, h - 4, 4, 4);
            g2.drawLine(4, h / 2, w / 2 - 1, h - 5);
            g2.drawLine(w / 2 - 1, h - 5, w - 4, 5);
        });
    }

    /**
     * Tạo icon danh sách / lịch sử phiếu mượn
     */
    public static Icon createHistoryIcon(Color color, int size) {
        return new VectorIcon(size, size, color, (g2, w, h, c) -> {
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawRoundRect(2, 2, w - 4, h - 4, 3, 3);
            g2.drawLine(5, 5, w - 5, 5);
            g2.drawLine(5, 8, w - 5, 8);
            g2.drawLine(5, 11, w - 7, 11);
        });
    }

    /**
     * Tạo icon biểu đồ cột (Thống kê)
     */
    public static Icon createChartIcon(Color color, int size) {
        return new VectorIcon(size, size, color, (g2, w, h, c) -> {
            g2.setColor(c);
            // Trục tọa độ
            g2.drawLine(2, h - 2, w - 2, h - 2);
            // 3 cột
            g2.fillRect(4, h - 7, 3, 5);
            g2.fillRect(9, h - 11, 3, 9);
            g2.fillRect(14, h - 14, 3, 12);
        });
    }

    /**
     * Tạo icon thùng rác & khôi phục
     */
    public static Icon createTrashIcon(Color color, int size) {
        return new VectorIcon(size, size, color, (g2, w, h, c) -> {
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.6f));
            // Nắp thùng
            g2.drawLine(3, 4, w - 3, 4);
            g2.drawLine(w / 2 - 2, 2, w / 2 + 2, 2);
            // Thân thùng
            g2.drawRoundRect(4, 5, w - 8, h - 7, 2, 2);
            g2.drawLine(w / 2 - 2, 7, w / 2 - 2, h - 4);
            g2.drawLine(w / 2 + 2, 7, w / 2 + 2, h - 4);
        });
    }

    /**
     * Tạo icon bánh răng (Cài đặt quy định)
     */
    /**
     * Tạo icon cây bút chì / chỉnh sửa (Edit/Action)
     */
    public static Icon createEditIcon(Color color, int size) {
        return new VectorIcon(size, size, color, (g2, w, h, c) -> {
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int x1 = 3, y1 = h - 4;
            int x2 = w - 4, y2 = 3;
            g2.drawLine(x1 + 3, y1, x2, y2 + 3);
            g2.drawLine(x1, y1 - 3, x2 - 3, y2);
            g2.drawLine(x2 - 3, y2, x2, y2 + 3);
            g2.drawLine(x1, y1 - 3, x1, y1);
            g2.drawLine(x1 + 3, y1, x1, y1);
            g2.drawLine(x1, h - 1, w - 2, h - 1);
        });
    }

    /**
     * Tạo icon mũi tên sang trái (Chevron Left cho Phân trang)
     */
    public static Icon createChevronLeftIcon(Color color, int size) {
        return new VectorIcon(size, size, color, (g2, w, h, c) -> {
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = w / 2;
            int cy = h / 2;
            g2.drawLine(cx + 2, cy - 4, cx - 2, cy);
            g2.drawLine(cx - 2, cy, cx + 2, cy + 4);
        });
    }

    /**
     * Tạo icon mũi tên sang phải (Chevron Right cho Phân trang)
     */
    public static Icon createChevronRightIcon(Color color, int size) {
        return new VectorIcon(size, size, color, (g2, w, h, c) -> {
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = w / 2;
            int cy = h / 2;
            g2.drawLine(cx - 2, cy - 4, cx + 2, cy);
            g2.drawLine(cx + 2, cy, cx - 2, cy + 4);
        });
    }

    public static Icon createSettingsIcon(Color color, int size) {
        return new VectorIcon(size, size, color, (g2, w, h, c) -> {
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawOval(4, 4, w - 8, h - 8);
            // Các chấu răng
            for (int i = 0; i < 4; i++) {
                double rad = Math.toRadians(i * 45);
                int cx = w / 2;
                int cy = h / 2;
                int x1 = (int) (cx + Math.cos(rad) * (w / 2 - 1));
                int y1 = (int) (cy + Math.sin(rad) * (h / 2 - 1));
                int x2 = (int) (cx - Math.cos(rad) * (w / 2 - 1));
                int y2 = (int) (cy - Math.sin(rad) * (h / 2 - 1));
                g2.drawLine(x1, y1, x2, y2);
            }
            g2.setColor(UITheme.CARD_BG);
            g2.fillOval(w / 2 - 2, h / 2 - 2, 5, 5);
        });
    }

    /**
     * Tạo icon hỏi chấm tròn (Hướng dẫn)
     */
    public static Icon createHelpIcon(Color color, int size) {
        return new VectorIcon(size, size, color, (g2, w, h, c) -> {
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.6f));
            g2.drawOval(2, 2, w - 4, h - 4);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString("?", (w - fm.stringWidth("?")) / 2, h - 4);
        });
    }

    /**
     * Tạo icon chuông thông báo vector phẳng hiện đại với đường viền khép kín,
     * kết nối liền mạch giữa vòm trên, hai bên thân và vành chuông bên dưới.
     */
    public static void drawBell(Graphics2D g2, int x, int y, int size, Color color) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(Math.max(1.5f, size * 0.09f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        double s = size / 24.0;

        // Vòng nhỏ đỉnh chuông
        g2.drawOval((int) Math.round(x + 10.5 * s), (int) Math.round(y + 0.8 * s),
                    (int) Math.round(3.0 * s), (int) Math.round(3.0 * s));

        // Thân chuông kết nối liền mạch từ vòm trên qua hai thành viền xuống vành xòe dưới
        Path2D.Double bell = new Path2D.Double();
        bell.moveTo(x + 7.0 * s, y + 8.5 * s);
        // Vòm đỉnh chuông
        bell.curveTo(x + 7.0 * s, y + 4.5 * s, x + 9.0 * s, y + 3.2 * s, x + 12.0 * s, y + 3.2 * s);
        bell.curveTo(x + 15.0 * s, y + 3.2 * s, x + 17.0 * s, y + 4.5 * s, x + 17.0 * s, y + 8.5 * s);
        // Viền bên phải nối từ vòm xuống vành xòe bên dưới
        bell.curveTo(x + 17.0 * s, y + 14.0 * s, x + 20.5 * s, y + 15.5 * s, x + 20.5 * s, y + 17.5 * s);
        // Vành ngang đáy chuông
        bell.lineTo(x + 3.5 * s, y + 17.5 * s);
        // Viền bên trái nối từ vành đáy xòe trở lên vòm trên
        bell.curveTo(x + 3.5 * s, y + 15.5 * s, x + 7.0 * s, y + 14.0 * s, x + 7.0 * s, y + 8.5 * s);
        bell.closePath();
        g2.draw(bell);

        // Quả lắc chuông phía dưới
        Path2D.Double clapper = new Path2D.Double();
        clapper.moveTo(x + 9.8 * s, y + 19.5 * s);
        clapper.curveTo(x + 10.2 * s, y + 22.0 * s, x + 13.8 * s, y + 22.0 * s, x + 14.2 * s, y + 19.5 * s);
        g2.draw(clapper);
    }

    /**
     * Lớp VectorIcon nội bộ
     */
    private static class VectorIcon implements DynamicIcon {
        private final int width;
        private final int height;
        private Color color;
        private final Painter painter;

        public VectorIcon(int width, int height, Color color, Painter painter) {
            this.width = width;
            this.height = height;
            this.color = color;
            this.painter = painter;
        }

        @Override
        public void setColor(Color c) {
            this.color = c;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            painter.paint(g2, width, height, color);
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return width; }

        @Override
        public int getIconHeight() { return height; }
    }

    @FunctionalInterface
    public interface Painter {
        void paint(Graphics2D g2, int w, int h, Color color);
    }
}
