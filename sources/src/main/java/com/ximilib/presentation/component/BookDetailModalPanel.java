package com.ximilib.presentation.component;

import com.ximilib.application.dto.BookSummaryDto;
import com.ximilib.application.port.BookCommentPort;
import com.ximilib.presentation.util.UITheme;
import com.ximilib.presentation.util.UserSession;
import com.ximilib.infrastructure.config.AppContainer;
import com.ximilib.presentation.util.UiModalHelper;
import raven.modal.component.SimpleModalBorder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * BookDetailModalPanel: Chi tiết đầu sách, Vị trí vật lý & Đánh giá/Bình luận (SRS UC-8, F-11).
 * Hiển thị vị trí kệ sách (Khu vực / Kệ), tình trạng cuốn vật lý,
 * và điểm đánh giá sao (1-5 ⭐) cùng nhận xét của độc giả lưu trong bảng `binhluan`.
 * Tuân thủ R-01, R-06, R-08, R-11.
 */
public class BookDetailModalPanel extends JPanel {

    private final BookSummaryDto book;
    private final BookCommentPort commentPort;
    private final JPanel commentsListPanel = new JPanel();
    private JLabel lblRatingAverage;
    private JComboBox<Integer> cmbRating;
    private JTextArea txtNewComment;

    public BookDetailModalPanel(BookSummaryDto book) {
        this(book, AppContainer.getInstance().getBookCommentPort());
    }

    public BookDetailModalPanel(BookSummaryDto book, BookCommentPort commentPort) {
        this.book = book;
        this.commentPort = commentPort != null ? commentPort : AppContainer.getInstance().getBookCommentPort();

        setPreferredSize(new Dimension(680, 580));
        setBackground(UITheme.CARD_BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(16, 20, 20, 20));

        initUI();
        loadLocationAndCommentsAsync();
    }

    private void initUI() {
        // 1. Thông tin đầu sách (Cover + Chi tiết + Vị trí)
        JPanel topInfoPanel = new JPanel(new BorderLayout(16, 0));
        topInfoPanel.setOpaque(false);

        // Bìa sách
        JLabel lblCover = new JLabel();
        lblCover.setPreferredSize(new Dimension(105, 145));
        lblCover.setBorder(BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1));
        lblCover.setHorizontalAlignment(SwingConstants.CENTER);
        loadCoverAsync(lblCover, book.getCoverImage());

        // Chi tiết
        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("<html><b>" + book.getTitle() + "</b></html>");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(UITheme.TEXT_MAIN);

        JLabel lblAuthor = new JLabel("Tác giả: " + (book.getAuthor() != null ? book.getAuthor() : "Không rõ"));
        lblAuthor.setFont(UITheme.FONT_SMALL);
        lblAuthor.setForeground(UITheme.TEXT_MUTED);

        JLabel lblCategory = new JLabel("Thể loại: " + (book.getCategoryName() != null ? book.getCategoryName() : "Không rõ"));
        lblCategory.setFont(UITheme.FONT_SMALL);
        lblCategory.setForeground(UITheme.TEXT_MUTED);

        lblRatingAverage = new JLabel("Đánh giá: ★ Đang tải điểm...");
        lblRatingAverage.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblRatingAverage.setForeground(new Color(217, 119, 6)); // Amber color

        details.add(lblTitle);
        details.add(Box.createVerticalStrut(4));
        details.add(lblAuthor);
        details.add(lblCategory);
        details.add(Box.createVerticalStrut(4));
        details.add(lblRatingAverage);

        // Mô tả tóm tắt nội dung sách
        String desc = book.getDescription();
        if (desc == null || desc.trim().isEmpty()) {
            desc = "Tác phẩm tiêu biểu thuộc danh mục " + (book.getCategoryName() != null ? book.getCategoryName() : "Thư viện")
                 + ", được lưu trữ và phục vụ bạn đọc tại Thư viện ĐHBK Đà Nẵng.";
        }
        JLabel lblDesc = new JLabel("<html><p style='width:240px; color:#475569; font-size:11px; margin-top:4px;'>"
            + desc + "</p></html>");
        lblDesc.setFont(UITheme.FONT_SMALL);
        details.add(Box.createVerticalStrut(4));
        details.add(lblDesc);

        topInfoPanel.add(lblCover, BorderLayout.WEST);
        topInfoPanel.add(details, BorderLayout.CENTER);
        add(topInfoPanel, BorderLayout.NORTH);

        // 2. Tabbed / Phân vùng: Vị trí Kệ sách & Bình luận
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);

        // Tab 1: Vị trí kệ sách & Cuốn vật lý
        JPanel shelfPanel = createShelfLocationPanel();
        tabbedPane.addTab("Vị trí Kệ sách & Tồn kho", shelfPanel);

        // Tab 2: Bình luận & Đánh giá (UC-8)
        JPanel commentsTab = createCommentsPanel();
        tabbedPane.addTab("Đánh giá & Bình luận", commentsTab);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createShelfLocationPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel lblLocHeader = new JLabel("Vị trí phân bổ trên kệ:");
        lblLocHeader.setFont(UITheme.FONT_BOLD);
        panel.add(lblLocHeader);
        panel.add(Box.createVerticalStrut(8));

        JLabel lblZone = new JLabel("• Vị trí: Khu vực A - Tầng 2, Kệ sách chuyên ngành PBL");
        lblZone.setFont(UITheme.FONT_REGULAR);
        panel.add(lblZone);

        JLabel lblStock = new JLabel(String.format("• Số lượng: %d cuốn tổng cộng (%d cuốn có sẵn tại quầy)",
            book.getTotalCopies(), book.getAvailableCopies()));
        lblStock.setFont(UITheme.FONT_REGULAR);
        lblStock.setForeground(UITheme.PRIMARY);
        panel.add(lblStock);

        if (book.getAvailableCopies() > 0) {
            panel.add(Box.createVerticalStrut(14));
            JButton btnBorrow = UITheme.createPillButton("Đăng Ký Mượn Sách", UITheme.PRIMARY, Color.WHITE);
            btnBorrow.setFont(UITheme.FONT_BOLD);
            btnBorrow.setAlignmentX(Component.LEFT_ALIGNMENT);
            btnBorrow.setPreferredSize(new Dimension(280, 38));
            btnBorrow.setMaximumSize(new Dimension(300, 38));
            btnBorrow.addActionListener(e -> {
                UiModalHelper.closeModal("BOOK_DETAIL");
                if (com.ximilib.presentation.view.MainDashboardFrame.getInstance() != null) {
                    com.ximilib.presentation.view.MainDashboardFrame.getInstance().openBorrowRegistration(book);
                }
            });
            panel.add(btnBorrow);
        }

        return panel;
    }

    private JPanel createCommentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Danh sách bình luận cuộn được
        commentsListPanel.setLayout(new BoxLayout(commentsListPanel, BoxLayout.Y_AXIS));
        commentsListPanel.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(commentsListPanel);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1));
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        panel.add(scroll, BorderLayout.CENTER);

        // Khối gửi nhận xét mới
        JPanel addCommentBox = new JPanel(new BorderLayout(8, 6));
        addCommentBox.setOpaque(false);
        addCommentBox.setBorder(new EmptyBorder(8, 0, 0, 0));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topBar.setOpaque(false);
        topBar.add(new JLabel("Đánh giá của bạn:"));

        cmbRating = new JComboBox<>(new Integer[]{5, 4, 3, 2, 1});
        cmbRating.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value + " ★");
                return this;
            }
        });
        topBar.add(cmbRating);
        addCommentBox.add(topBar, BorderLayout.NORTH);

        txtNewComment = new JTextArea(2, 20);
        txtNewComment.setFont(UITheme.FONT_REGULAR);
        txtNewComment.setLineWrap(true);
        txtNewComment.setWrapStyleWord(true);
        JScrollPane txtScroll = new JScrollPane(txtNewComment);
        addCommentBox.add(txtScroll, BorderLayout.CENTER);

        JButton btnSubmit = UITheme.createPillButton("Gửi Đánh Giá", UITheme.PRIMARY, Color.WHITE);
        btnSubmit.setPreferredSize(new Dimension(120, 34));
        btnSubmit.addActionListener(e -> submitComment());
        addCommentBox.add(btnSubmit, BorderLayout.EAST);

        panel.add(addCommentBox, BorderLayout.SOUTH);
        return panel;
    }

    private void submitComment() {
        String content = txtNewComment.getText().trim();
        if (content.isEmpty()) {
            UiModalHelper.showToastWarning(this, "Vui lòng nhập nội dung nhận xét!");
            return;
        }

        String userId = (UserSession.getCurrentUser() != null) ? UserSession.getCurrentUser().getId() : "ND004";
        int stars = (Integer) cmbRating.getSelectedItem();

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                commentPort.addComment(book.getBookId(), userId, content, stars);
                return null;
            }

            @Override
            protected void done() {
                txtNewComment.setText("");
                UiModalHelper.showToastSuccess(BookDetailModalPanel.this, "Cảm ơn bạn đã gửi đánh giá sách!");
                loadLocationAndCommentsAsync();
            }
        }.execute();
    }

    private void loadLocationAndCommentsAsync() {
        new SwingWorker<BookCommentPort.BookCommentData, Void>() {
            @Override
            protected BookCommentPort.BookCommentData doInBackground() {
                return commentPort.getBookComments(book.getBookId());
            }

            @Override
            protected void done() {
                try {
                    BookCommentPort.BookCommentData data = get();
                    lblRatingAverage.setText(String.format("Đánh giá: ★ %.1f / 5.0 (%d lượt nhận xét)", data.getAverageRating(), data.getTotalReviews()));

                    commentsListPanel.removeAll();
                    if (data.getComments().isEmpty()) {
                        JLabel lblEmpty = new JLabel("Chưa có nhận xét nào. Hãy là người đầu tiên đánh giá cuốn sách này!");
                        lblEmpty.setFont(UITheme.FONT_SMALL);
                        lblEmpty.setForeground(UITheme.TEXT_MUTED);
                        lblEmpty.setBorder(new EmptyBorder(16, 16, 16, 16));
                        commentsListPanel.add(lblEmpty);
                    } else {
                        for (BookCommentPort.BookCommentItem item : data.getComments()) {
                            commentsListPanel.add(createCommentCard(item));
                            commentsListPanel.add(Box.createVerticalStrut(6));
                        }
                    }
                    commentsListPanel.revalidate();
                    commentsListPanel.repaint();
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    private JPanel createCommentCard(BookCommentPort.BookCommentItem item) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(UITheme.BG_LIGHT);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblName = new JLabel(item.author() + " - " + "★".repeat(Math.max(1, item.stars())));
        lblName.setFont(UITheme.FONT_BOLD);
        lblName.setForeground(UITheme.TEXT_MAIN);

        JLabel lblDate = new JLabel(item.date());
        lblDate.setFont(UITheme.FONT_BADGE);
        lblDate.setForeground(UITheme.TEXT_MUTED);

        header.add(lblName, BorderLayout.WEST);
        header.add(lblDate, BorderLayout.EAST);

        JLabel lblText = new JLabel("<html>" + item.text() + "</html>");
        lblText.setFont(UITheme.FONT_REGULAR);
        lblText.setForeground(UITheme.TEXT_MAIN);

        card.add(header, BorderLayout.NORTH);
        card.add(lblText, BorderLayout.CENTER);
        return card;
    }

    private ImageIcon createPastelPlaceholder(String title, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color[] pastels = {
            new Color(254, 242, 242), new Color(238, 242, 255), new Color(236, 253, 245),
            new Color(254, 243, 199), new Color(250, 245, 255), new Color(240, 253, 250)
        };
        Color[] texts = {
            new Color(153, 27, 27), new Color(55, 48, 163), new Color(6, 95, 70),
            new Color(146, 64, 14), new Color(107, 33, 168), new Color(17, 94, 89)
        };
        int idx = Math.abs(title.hashCode()) % pastels.length;
        g2.setColor(pastels[idx]);
        g2.fillRect(0, 0, w, h);
        g2.setColor(new Color(0, 0, 0, 15));
        g2.fillRect(0, 0, 6, h);
        String initial = (!title.isBlank()) ? String.valueOf(title.charAt(0)).toUpperCase() : "B";
        g2.setColor(texts[idx]);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 36));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(initial, (w - fm.stringWidth(initial)) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
        g2.dispose();
        return new ImageIcon(img);
    }

    private void loadCoverAsync(JLabel label, String path) {
        label.setIcon(createPastelPlaceholder(book.getTitle(), 105, 145));
        label.setText(null);
        if (path == null || path.isBlank()) return;

        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() {
                try {
                    BufferedImage raw = null;
                    String resPath = path.startsWith("/") ? path : "/assets/covers/" + path;
                    URL url = getClass().getResource(resPath);
                    if (url != null) raw = ImageIO.read(url);
                    if (raw == null) {
                        File f = new File(path);
                        if (f.exists()) raw = ImageIO.read(f);
                    }
                    if (raw == null) {
                        File fRes = new File("sources/src/main/resources/assets/covers/" + new File(path).getName());
                        if (fRes.exists()) raw = ImageIO.read(fRes);
                    }
                    if (raw == null) {
                        File fRes2 = new File("src/main/resources/assets/covers/" + new File(path).getName());
                        if (fRes2.exists()) raw = ImageIO.read(fRes2);
                    }
                    if (raw != null) {
                        Image scaled = raw.getScaledInstance(105, 145, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaled);
                    }
                } catch (Exception ignored) {}
                return null;
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        label.setIcon(icon);
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    public static void show(Component parent, BookSummaryDto book) {
        BookDetailModalPanel detailPanel = new BookDetailModalPanel(book);
        UiModalHelper.showCustomModal(
            parent,
            "BOOK_DETAIL",
            "Chi Tiết Sách & Đánh Giá Độc Giả",
            detailPanel,
            SimpleModalBorder.DEFAULT_OPTION,
            null
        );
    }
}
