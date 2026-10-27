package com.ximilib.presentation.component;

import com.ximilib.application.dto.StaffDashboardDto.NewBookTileDto;
import com.ximilib.gui.util.UITheme;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * BookCoverTile: Thành phần hiển thị bìa sách tỉ lệ 2:3 (~110x165px) chuẩn LibRack/Goodreads.
 * - Ảnh bìa tải từ Hinhanh hoặc resources.
 * - Fallback: Ô màu nền pastel chọn xác định theo mã sách, chữ cái đầu tên sách.
 * - Không có icon trái tim, không có chấm tròn.
 * - Pill "Hết sách" nếu hết bản sao khả dụng.
 * - Tên sách tối đa 2 dòng kèm tooltip đầy đủ.
 */
public class BookCoverTile extends JPanel {

    private static final int COVER_WIDTH = 110;
    private static final int COVER_HEIGHT = 165;

    // Cache thumbnail đã scale (220x330 - 2x retina) để tối ưu tốc độ load ảnh và tiết kiệm bộ nhớ
    private static final java.util.Map<String, BufferedImage> THUMBNAIL_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    // Bảng màu pastel chọn xác định theo mã sách (Deterministic Pastels)
    private static final Color[] PASTEL_PALETTE = {
        new Color(254, 226, 226), // Rose light
        new Color(254, 243, 199), // Amber light
        new Color(220, 252, 231), // Emerald light
        new Color(224, 231, 255), // Indigo light
        new Color(243, 232, 255), // Purple light
        new Color(255, 237, 213), // Orange light
        new Color(207, 250, 254)  // Cyan light
    };

    private static final Color[] PASTEL_TEXT_PALETTE = {
        new Color(159, 18, 57),
        new Color(146, 64, 14),
        new Color(22, 101, 52),
        new Color(55, 48, 163),
        new Color(107, 33, 168),
        new Color(154, 52, 18),
        new Color(21, 94, 117)
    };

    private final NewBookTileDto book;
    private final Consumer<NewBookTileDto> onSelect;
    private BufferedImage loadedCover = null;
    private boolean isHover = false;

    public BookCoverTile(String coverPath, String title, String author, int availableCopies, Runnable onSelect) {
        this(new NewBookTileDto("", title, author, coverPath, availableCopies), dto -> {
            if (onSelect != null) onSelect.run();
        });
    }

    public BookCoverTile(NewBookTileDto book, Consumer<NewBookTileDto> onSelect) {
        this.book = book;
        this.onSelect = onSelect;

        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(COVER_WIDTH + 14, 240));
        setMaximumSize(new Dimension(COVER_WIDTH + 14, 240));
        setBorder(BorderFactory.createEmptyBorder(4, 4, 6, 4));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setToolTipText(book.title() + " — " + book.author());

        initUI();
        loadCoverImageAsync();
    }

    private void initUI() {
        // 1. Khung bìa sách 110x165 tỉ lệ 2:3
        JPanel coverPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                int w = getWidth();
                int h = getHeight();

                // Nền bo góc 8px
                if (loadedCover != null) {
                    g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, w, h, 8, 8));
                    g2.drawImage(loadedCover, 0, 0, w, h, null);
                    g2.setClip(null);
                } else {
                    int paletteIndex = Math.abs(book.bookId().hashCode()) % PASTEL_PALETTE.length;
                    g2.setColor(PASTEL_PALETTE[paletteIndex]);
                    g2.fillRoundRect(0, 0, w, h, 8, 8);

                    // Gáy sách mô phỏng tinh tế
                    g2.setColor(new Color(0, 0, 0, 20));
                    g2.fillRect(0, 0, 5, h);

                    // Chữ cái đầu của tên sách
                    String initial = (!book.title().isBlank())
                        ? String.valueOf(book.title().charAt(0)).toUpperCase()
                        : "B";
                    g2.setColor(PASTEL_TEXT_PALETTE[paletteIndex]);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 28));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(initial, (w - fm.stringWidth(initial)) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
                }

                // Viền ngoài (Highlight khi hover)
                g2.setColor(isHover ? UITheme.PRIMARY : UITheme.BORDER);
                g2.setStroke(new BasicStroke(isHover ? 1.6f : 1.0f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);

                // Nếu hết sách: Pill "Hết sách" ở góc dưới
                if (book.availableCopies() <= 0) {
                    int pillH = 18;
                    int pillW = 62;
                    int pillX = (w - pillW) / 2;
                    int pillY = h - pillH - 6;
                    g2.setColor(new Color(0, 0, 0, 160));
                    g2.fillRoundRect(pillX, pillY, pillW, pillH, pillH, pillH);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    FontMetrics fm = g2.getFontMetrics();
                    String label = "Hết sách";
                    g2.drawString(label, pillX + (pillW - fm.stringWidth(label)) / 2, pillY + 13);
                }

                g2.dispose();
            }
        };
        coverPanel.setPreferredSize(new Dimension(COVER_WIDTH, COVER_HEIGHT));
        coverPanel.setMaximumSize(new Dimension(COVER_WIDTH, COVER_HEIGHT));
        coverPanel.setMinimumSize(new Dimension(COVER_WIDTH, COVER_HEIGHT));
        coverPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 2. Tên sách (tối đa 2 dòng)
        JLabel lblTitle = new JLabel("<html><body style='width: 105px; font-weight: bold; line-height: 1.2;'>"
            + escapeHtml(book.title()) + "</body></html>");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(UITheme.TEXT_MAIN);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 3. Tác giả (12px textMuted, 1 dòng cắt gọn)
        String authorText = book.author() != null ? book.author() : "Chưa rõ";
        if (authorText.length() > 18) {
            authorText = authorText.substring(0, 16) + "…";
        }
        JLabel lblAuthor = new JLabel(authorText);
        lblAuthor.setFont(UITheme.FONT_BADGE);
        lblAuthor.setForeground(UITheme.TEXT_MUTED);
        lblAuthor.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(coverPanel);
        add(Box.createVerticalStrut(6));
        add(lblTitle);
        add(Box.createVerticalStrut(2));
        add(lblAuthor);

        // Sự kiện click mở chi tiết
        MouseAdapter clickAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && onSelect != null) {
                    onSelect.accept(book);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                isHover = true;
                coverPanel.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHover = false;
                coverPanel.repaint();
            }
        };

        addMouseListener(clickAdapter);
        coverPanel.addMouseListener(clickAdapter);
        lblTitle.addMouseListener(clickAdapter);
        lblAuthor.addMouseListener(clickAdapter);
    }

    private void loadCoverImageAsync() {
        String path = book.coverPath();
        if (path == null || path.isBlank()) {
            return;
        }

        // 1. Kiểm tra cache thumbnail bộ nhớ (tốc độ 0ms)
        BufferedImage cached = THUMBNAIL_CACHE.get(path);
        if (cached != null) {
            this.loadedCover = cached;
            repaint();
            return;
        }

        SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() {
                try {
                    BufferedImage raw = null;
                    // 1. Thử load từ classpath
                    String resPath = path.startsWith("/") ? path : "/assets/covers/" + path;
                    URL url = getClass().getResource(resPath);
                    if (url != null) {
                        raw = ImageIO.read(url);
                    }

                    // 2. Thử load từ thư mục dự án
                    if (raw == null) {
                        File f = new File(path);
                        if (f.exists()) {
                            raw = ImageIO.read(f);
                        }
                    }

                    if (raw == null) {
                        File fRes = new File("sources/src/main/resources/assets/covers/" + new File(path).getName());
                        if (fRes.exists()) {
                            raw = ImageIO.read(fRes);
                        }
                    }

                    if (raw == null) {
                        File fRes2 = new File("src/main/resources/assets/covers/" + new File(path).getName());
                        if (fRes2.exists()) {
                            raw = ImageIO.read(fRes2);
                        }
                    }

                    if (raw != null) {
                        // Tạo thumbnail kích thước 220x330 (2x retina) tối ưu tốc độ vẽ và bộ nhớ
                        BufferedImage thumb = new BufferedImage(COVER_WIDTH * 2, COVER_HEIGHT * 2, BufferedImage.TYPE_INT_RGB);
                        Graphics2D g2 = thumb.createGraphics();
                        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.drawImage(raw, 0, 0, COVER_WIDTH * 2, COVER_HEIGHT * 2, null);
                        g2.dispose();
                        raw.flush();

                        THUMBNAIL_CACHE.put(path, thumb);
                        return thumb;
                    }
                } catch (Exception ignored) {
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    BufferedImage img = get();
                    if (img != null) {
                        loadedCover = img;
                        repaint();
                    }
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
