package com.ximilib.presentation.component;

import com.ximilib.application.port.ReportPort.DailyMetric;
import com.ximilib.presentation.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * TrendChartPanel: Biểu đồ Java2D phẳng vẽ xu hướng lượt mượn sách và doanh thu phạt theo thời gian (UC-7).
 * Không sử dụng thư viện ngoài, vẽ hoàn toàn bằng Java2D và bảng màu UITheme (FlatLaf LibRack).
 */
public class TrendChartPanel extends JPanel {

    private static final Color ACCENT_COLOR = Color.decode("#F59E0B"); // Màu cam vàng hổ phách cho tiền phạt
    private final List<DailyMetric> metrics = new ArrayList<>();
    private final NumberFormat currencyFmt = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public TrendChartPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(600, 220));
        setMinimumSize(new Dimension(300, 180));
    }

    public void setMetrics(List<DailyMetric> data) {
        this.metrics.clear();
        if (data != null) {
            this.metrics.addAll(data);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 1. Nền Card bo góc
        g2.setColor(UITheme.CARD_BG);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 16, 16));
        g2.setColor(UITheme.CARD_BORDER);
        g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 16, 16));

        // 2. Chú thích (Legend)
        int legendY = 24;
        g2.setFont(UITheme.FONT_BOLD);
        g2.setColor(UITheme.PRIMARY);
        g2.fillRect(w - 280, legendY - 10, 12, 12);
        g2.setColor(UITheme.TEXT_MAIN);
        g2.drawString("Lượt mượn", w - 262, legendY);

        g2.setColor(ACCENT_COLOR);
        g2.fillRect(w - 160, legendY - 10, 12, 12);
        g2.setColor(UITheme.TEXT_MAIN);
        g2.drawString("Tiền phạt (k)", w - 142, legendY);

        // Tiêu đề biểu đồ
        g2.setFont(UITheme.FONT_TITLE.deriveFont(Font.BOLD, 14f));
        g2.setColor(UITheme.TEXT_MAIN);
        g2.drawString("XU HƯỚNG MƯỢN SÁCH & DOANH THU PHẠT", 20, legendY);

        if (metrics.isEmpty()) {
            g2.setFont(UITheme.FONT_REGULAR);
            g2.setColor(UITheme.TEXT_MUTED);
            String msg = "Chưa có dữ liệu thống kê trong giai đoạn này";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2 + 10);
            g2.dispose();
            return;
        }

        // 3. Vùng vẽ biểu đồ
        int padLeft = 45;
        int padRight = 30;
        int padTop = 45;
        int padBottom = 40;
        int plotW = w - padLeft - padRight;
        int plotH = h - padTop - padBottom;

        // Trục hoành đường đáy
        g2.setColor(UITheme.CARD_BORDER);
        g2.drawLine(padLeft, padTop + plotH, padLeft + plotW, padTop + plotH);

        // Tìm max lượt mượn để scale cột
        int maxBorrow = 5;
        for (DailyMetric m : metrics) {
            if (m.borrowCount() > maxBorrow) maxBorrow = m.borrowCount();
        }
        maxBorrow = ((maxBorrow / 5) + 1) * 5; // Làm tròn lên bội số của 5

        int n = metrics.size();
        float groupW = (float) plotW / n;
        float barW = Math.max(8f, groupW * 0.35f);

        // Vẽ các cột và nhãn ngày
        for (int i = 0; i < n; i++) {
            DailyMetric m = metrics.get(i);
            float groupX = padLeft + i * groupW;
            float centerX = groupX + groupW / 2;

            // Cột lượt mượn (Primary Blue)
            float barH = (float) m.borrowCount() / maxBorrow * (plotH - 20);
            float barX = centerX - barW / 2;
            float barY = padTop + plotH - barH;

            // Vẽ thanh mượn
            g2.setColor(new Color(UITheme.PRIMARY.getRed(), UITheme.PRIMARY.getGreen(), UITheme.PRIMARY.getBlue(), 210));
            g2.fill(new RoundRectangle2D.Float(barX, barY, barW, barH, 6, 6));

            // Số liệu trên đầu cột mượn nếu > 0
            if (m.borrowCount() > 0) {
                g2.setFont(UITheme.FONT_SMALL);
                g2.setColor(UITheme.PRIMARY);
                String valStr = String.valueOf(m.borrowCount());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(valStr, centerX - fm.stringWidth(valStr) / 2, barY - 4);
            }

            // Nhãn ngày tháng ở đáy
            g2.setFont(UITheme.FONT_SMALL);
            g2.setColor(UITheme.TEXT_MUTED);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(m.dateLabel(), centerX - fm.stringWidth(m.dateLabel()) / 2, padTop + plotH + 20);
        }

        // 4. Vẽ đường xu hướng tiền phạt (Accent / Amber Line Overlay)
        BigDecimal maxFine = new BigDecimal("50000");
        for (DailyMetric m : metrics) {
            if (m.fineRevenue() != null && m.fineRevenue().compareTo(maxFine) > 0) {
                maxFine = m.fineRevenue();
            }
        }

        int[] polyX = new int[n];
        int[] polyY = new int[n];
        for (int i = 0; i < n; i++) {
            DailyMetric m = metrics.get(i);
            float centerX = padLeft + i * groupW + groupW / 2;
            float fineRatio = (m.fineRevenue() != null && maxFine.compareTo(BigDecimal.ZERO) > 0)
                    ? m.fineRevenue().divide(maxFine, 4, java.math.RoundingMode.HALF_UP).floatValue()
                    : 0f;
            float lineY = padTop + plotH - (fineRatio * (plotH - 25));
            polyX[i] = (int) centerX;
            polyY[i] = (int) lineY;
        }

        // Vẽ đường nối nét đứt hoặc nét liền Accent
        g2.setColor(ACCENT_COLOR);
        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawPolyline(polyX, polyY, n);

        // Vẽ các điểm nút (dots) trên đường doanh thu
        for (int i = 0; i < n; i++) {
            g2.setColor(UITheme.CARD_BG);
            g2.fillOval(polyX[i] - 4, polyY[i] - 4, 8, 8);
            g2.setColor(ACCENT_COLOR);
            g2.drawOval(polyX[i] - 4, polyY[i] - 4, 8, 8);
        }

        g2.dispose();
    }
}
