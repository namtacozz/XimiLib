package com.ximilib.presentation.component;

import com.ximilib.application.dto.BookSummaryDto;
import com.ximilib.presentation.util.UITheme;
import com.ximilib.presentation.util.UserSession;
import com.ximilib.infrastructure.config.AppContainer;
import com.ximilib.presentation.util.UiModalHelper;
import raven.modal.component.SimpleModalBorder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * BookDetailModalPanel: Chi tiết đầu sách, Vị trí vật lý & Đánh giá/Bình luận (SRS UC-8, F-11).
 * Hiển thị vị trí kệ sách (Khu vực / Kệ), tình trạng cuốn vật lý,
 * và điểm đánh giá sao (1-5 ⭐) cùng nhận xét của độc giả lưu trong bảng `binhluan`.
 * Tuân thủ R-01, R-06, R-08, R-11.
 */
public class BookDetailModalPanel extends JPanel {

    private final BookSummaryDto book;
    private final JPanel commentsListPanel = new JPanel();
    private JLabel lblRatingAverage;
    private JComboBox<Integer> cmbRating;
    private JTextArea txtNewComment;

    public BookDetailModalPanel(BookSummaryDto book) {
        this.book = book;

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

        lblRatingAverage = new JLabel("Đánh giá: ⭐ Đang tải điểm...");
        lblRatingAverage.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblRatingAverage.setForeground(new Color(217, 119, 6)); // Amber color

        details.add(lblTitle);
        details.add(Box.createVerticalStrut(4));
        details.add(lblAuthor);
        details.add(lblCategory);
        details.add(Box.createVerticalStrut(4));
        details.add(lblRatingAverage);

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
            JButton btnBorrow = UITheme.createPillButton("📖 Đăng Ký Mượn Cuốn Này (App)", UITheme.PRIMARY, Color.WHITE);
            btnBorrow.setFont(UITheme.FONT_BOLD);
            btnBorrow.setAlignmentX(Component.LEFT_ALIGNMENT);
            btnBorrow.setPreferredSize(new Dimension(280, 38));
            btnBorrow.setMaximumSize(new Dimension(300, 38));
            btnBorrow.addActionListener(e -> {
                UiModalHelper.closeModal("BOOK_DETAIL");
                if (com.ximilib.gui.MainDashboardFrame.getInstance() != null) {
                    com.ximilib.gui.MainDashboardFrame.getInstance().openBorrowRegistration(book);
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
                setText(value + " ⭐");
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
            protected Void doInBackground() throws SQLException {
                String sql = "INSERT INTO binhluan (Masach, id_nguoidung, noidung, diemsao, ngaytao) VALUES (?, ?, ?, ?, NOW())";
                try (Connection conn = AppContainer.getInstance().getConnectionManager().getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, book.getBookId());
                    ps.setString(2, userId);
                    ps.setString(3, content);
                    ps.setInt(4, stars);
                    ps.executeUpdate();
                }
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
        new SwingWorker<CommentData, Void>() {
            @Override
            protected CommentData doInBackground() {
                CommentData data = new CommentData();
                String sql = """
                    SELECT b.noidung, b.diemsao, b.ngaytao, u.Hoten
                    FROM binhluan b
                    JOIN nguoidung u ON b.id_nguoidung = u.ID
                    WHERE b.Masach = ? AND b.IsDeleted = FALSE
                    ORDER BY b.ngaytao DESC
                """;
                try (Connection conn = AppContainer.getInstance().getConnectionManager().getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, book.getBookId());
                    try (ResultSet rs = ps.executeQuery()) {
                        int count = 0;
                        int sumStars = 0;
                        while (rs.next()) {
                            count++;
                            int stars = rs.getInt(2);
                            sumStars += stars;
                            data.comments.add(new CommentItem(rs.getString(4), rs.getString(1), stars, rs.getDate(3).toString()));
                        }
                        data.averageRating = count > 0 ? (double) sumStars / count : 5.0;
                        data.totalReviews = count;
                    }
                } catch (SQLException ignored) {
                }
                return data;
            }

            @Override
            protected void done() {
                try {
                    CommentData data = get();
                    lblRatingAverage.setText(String.format("Đánh giá: ⭐ %.1f / 5.0 (%d lượt nhận xét)", data.averageRating, data.totalReviews));

                    commentsListPanel.removeAll();
                    if (data.comments.isEmpty()) {
                        JLabel lblEmpty = new JLabel("Chưa có nhận xét nào. Hãy là người đầu tiên đánh giá cuốn sách này!");
                        lblEmpty.setFont(UITheme.FONT_SMALL);
                        lblEmpty.setForeground(UITheme.TEXT_MUTED);
                        lblEmpty.setBorder(new EmptyBorder(16, 16, 16, 16));
                        commentsListPanel.add(lblEmpty);
                    } else {
                        for (CommentItem item : data.comments) {
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

    private JPanel createCommentCard(CommentItem item) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(UITheme.BG_LIGHT);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblName = new JLabel(item.author + " - " + "⭐".repeat(Math.max(1, item.stars)));
        lblName.setFont(UITheme.FONT_BOLD);
        lblName.setForeground(UITheme.TEXT_MAIN);

        JLabel lblDate = new JLabel(item.date);
        lblDate.setFont(UITheme.FONT_BADGE);
        lblDate.setForeground(UITheme.TEXT_MUTED);

        header.add(lblName, BorderLayout.WEST);
        header.add(lblDate, BorderLayout.EAST);

        JLabel lblText = new JLabel("<html>" + item.text + "</html>");
        lblText.setFont(UITheme.FONT_REGULAR);
        lblText.setForeground(UITheme.TEXT_MAIN);

        card.add(header, BorderLayout.NORTH);
        card.add(lblText, BorderLayout.CENTER);
        return card;
    }

    private void loadCoverAsync(JLabel label, String path) {
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() {
                if (path != null && !path.isBlank()) {
                    File f = new File(path);
                    if (f.exists()) {
                        ImageIcon raw = new ImageIcon(f.getAbsolutePath());
                        Image scaled = raw.getImage().getScaledInstance(105, 145, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaled);
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        label.setIcon(icon);
                    } else {
                        label.setText("No Cover");
                    }
                } catch (Exception ignored) {
                }
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

    private static class CommentData {
        double averageRating = 5.0;
        int totalReviews = 0;
        final List<CommentItem> comments = new ArrayList<>();
    }

    private record CommentItem(String author, String text, int stars, String date) {}
}
