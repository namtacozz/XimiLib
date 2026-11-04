package com.ximilib.presentation.component;

import com.ximilib.application.dto.StaffDashboardDto.NewBookTileDto;
import com.ximilib.presentation.util.UITheme;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.*;

public class BookCoverTile extends JPanel {

    private static final int COVER_WIDTH = 112;
    private static final int COVER_HEIGHT = 168;

    private static final java.util.Map<String, BufferedImage> THUMBNAIL_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    private static final Color[] PASTEL_PALETTE = {
        new Color(254, 226, 226),
        new Color(254, 243, 199),
        new Color(220, 252, 231),
        new Color(224, 231, 255),
        new Color(243, 232, 255),
        new Color(255, 237, 213),
        new Color(207, 250, 254)
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
    private final Consumer<NewBookTileDto> onBorrow;
    private BufferedImage loadedCover = null;
    private boolean isHover = false;
    private JPanel coverPanel;
    private JPanel overlayPanel;

    public BookCoverTile(String coverPath, String title, String author, int availableCopies, Runnable onSelect) {
        this(new NewBookTileDto("", title, author, coverPath, availableCopies), dto -> {
            if (onSelect != null) onSelect.run();
        }, null);
    }

    public BookCoverTile(NewBookTileDto book, Consumer<NewBookTileDto> onSelect) {
        this(book, onSelect, null);
    }

    public BookCoverTile(NewBookTileDto book, Consumer<NewBookTileDto> onSelect, Consumer<NewBookTileDto> onBorrow) {
        this.book = book;
        this.onSelect = onSelect;
        this.onBorrow = onBorrow;

        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(COVER_WIDTH + 14, 218));
        setMaximumSize(new Dimension(COVER_WIDTH + 14, 218));
        setMinimumSize(new Dimension(COVER_WIDTH + 14, 218));
        setBorder(BorderFactory.createEmptyBorder(2, 4, 4, 4));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setToolTipText(book.title() + " — " + book.author());

        initUI();
        loadCoverImageAsync();
    }

    private void initUI() {

        coverPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                int w = getWidth();
                int h = getHeight();

                if (loadedCover != null) {
                    g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, w, h, 8, 8));
                    g2.drawImage(loadedCover, 0, 0, w, h, null);
                    g2.setClip(null);
                } else {
                    int paletteIndex = Math.abs(book.bookId().hashCode()) % PASTEL_PALETTE.length;
                    g2.setColor(PASTEL_PALETTE[paletteIndex]);
                    g2.fillRoundRect(0, 0, w, h, 8, 8);

                    g2.setColor(new Color(0, 0, 0, 20));
                    g2.fillRect(0, 0, 5, h);

                    String initial = (!book.title().isBlank())
                        ? String.valueOf(book.title().charAt(0)).toUpperCase()
                        : "B";
                    g2.setColor(PASTEL_TEXT_PALETTE[paletteIndex]);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 28));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(initial, (w - fm.stringWidth(initial)) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
                }

                g2.setColor(isHover ? UITheme.PRIMARY : UITheme.BORDER);
                g2.setStroke(new BasicStroke(isHover ? 1.6f : 1.0f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);

                if (book.availableCopies() <= 0 && !isHover) {
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
        coverPanel.setOpaque(false);
        coverPanel.setLayout(new GridBagLayout());
        coverPanel.setPreferredSize(new Dimension(COVER_WIDTH, COVER_HEIGHT));
        coverPanel.setMaximumSize(new Dimension(COVER_WIDTH, COVER_HEIGHT));
        coverPanel.setMinimumSize(new Dimension(COVER_WIDTH, COVER_HEIGHT));
        coverPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        overlayPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(15, 23, 42, 195));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        overlayPanel.setOpaque(false);
        overlayPanel.setPreferredSize(new Dimension(COVER_WIDTH, COVER_HEIGHT));
        overlayPanel.setMaximumSize(new Dimension(COVER_WIDTH, COVER_HEIGHT));
        overlayPanel.setMinimumSize(new Dimension(COVER_WIDTH, COVER_HEIGHT));
        overlayPanel.setLayout(new GridBagLayout());
        overlayPanel.setVisible(false);

        JPanel btnGroup = new JPanel();
        btnGroup.setLayout(new BoxLayout(btnGroup, BoxLayout.Y_AXIS));
        btnGroup.setOpaque(false);

        JButton btnDetail = UITheme.createPillButton("Xem chi tiết", Color.WHITE, UITheme.TEXT_MAIN);
        btnDetail.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnDetail.setPreferredSize(new Dimension(96, 26));
        btnDetail.setMinimumSize(new Dimension(96, 26));
        btnDetail.setMaximumSize(new Dimension(96, 26));
        btnDetail.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnDetail.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDetail.addActionListener(e -> {
            if (onSelect != null) onSelect.accept(book);
        });

        JButton btnBorrow = UITheme.createPillButton("Mượn sách", UITheme.PRIMARY, Color.WHITE);
        btnBorrow.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnBorrow.setPreferredSize(new Dimension(96, 26));
        btnBorrow.setMinimumSize(new Dimension(96, 26));
        btnBorrow.setMaximumSize(new Dimension(96, 26));
        btnBorrow.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBorrow.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBorrow.addActionListener(e -> {
            if (onBorrow != null) {
                onBorrow.accept(book);
            } else if (onSelect != null) {
                onSelect.accept(book);
            }
        });

        btnGroup.add(btnDetail);
        btnGroup.add(Box.createVerticalStrut(10));
        btnGroup.add(btnBorrow);
        overlayPanel.add(btnGroup);

        coverPanel.add(overlayPanel);

        String rawTitle = book.title() != null ? book.title().trim() : "";
        String displayTitle = rawTitle;
        if (displayTitle.length() > 17) {
            displayTitle = displayTitle.substring(0, 15) + "…";
        }
        JLabel lblTitle = new JLabel(displayTitle, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(UITheme.TEXT_MAIN);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitle.setToolTipText(rawTitle);

        String authorText = book.author() != null ? book.author().trim() : "Chưa rõ";
        if (authorText.length() > 20) {
            authorText = authorText.substring(0, 18) + "…";
        }
        JLabel lblAuthor = new JLabel(authorText, SwingConstants.CENTER);
        lblAuthor.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblAuthor.setForeground(UITheme.TEXT_MUTED);
        lblAuthor.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblAuthor.setToolTipText(authorText);

        add(coverPanel);
        add(Box.createVerticalStrut(5));
        add(lblTitle);
        add(Box.createVerticalStrut(2));
        add(lblAuthor);

        MouseAdapter hoverAdapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setHoverState(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), BookCoverTile.this);
                if (!contains(p)) {
                    setHoverState(false);
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), BookCoverTile.this);
                if (contains(p) && !isHover) {
                    setHoverState(true);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && onSelect != null) {
                    onSelect.accept(book);
                }
            }
        };

        addMouseListener(hoverAdapter);
        addMouseMotionListener(hoverAdapter);

        coverPanel.addMouseListener(hoverAdapter);
        coverPanel.addMouseMotionListener(hoverAdapter);

        overlayPanel.addMouseListener(hoverAdapter);
        overlayPanel.addMouseMotionListener(hoverAdapter);

        btnGroup.addMouseListener(hoverAdapter);
        btnGroup.addMouseMotionListener(hoverAdapter);

        lblTitle.addMouseListener(hoverAdapter);
        lblAuthor.addMouseListener(hoverAdapter);
    }

    private void setHoverState(boolean hover) {
        if (this.isHover != hover) {
            this.isHover = hover;
            if (overlayPanel != null) {
                overlayPanel.setVisible(hover);
            }
            if (coverPanel != null) {
                coverPanel.repaint();
            }
            repaint();
        }
    }

    private void loadCoverImageAsync() {
        String path = book.coverPath();
        if (path == null || path.isBlank()) {
            return;
        }

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

                    String fileName = new File(path).getName();
                    URL url = getClass().getResource("/assets/covers/" + fileName);
                    if (url == null) {
                        url = getClass().getResource(path.startsWith("/") ? path : "/" + path);
                    }
                    if (url != null) {
                        raw = ImageIO.read(url);
                    }

                    if (raw == null) {
                        File f = new File(path);
                        if (f.exists()) {
                            raw = ImageIO.read(f);
                        }
                    }

                    if (raw == null) {
                        File fRes = new File("sources/src/main/resources/assets/covers/" + fileName);
                        if (fRes.exists()) {
                            raw = ImageIO.read(fRes);
                        }
                    }

                    if (raw == null) {
                        File fRes2 = new File("src/main/resources/assets/covers/" + fileName);
                        if (fRes2.exists()) {
                            raw = ImageIO.read(fRes2);
                        }
                    }

                    if (raw != null) {

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
