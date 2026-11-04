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

        lblRatingAverage = new JLabel("Đang tải điểm đánh giá...");
        lblRatingAverage.setIcon(com.ximilib.presentation.util.IconFactory.createStarIcon(new Color(245, 158, 11), 14));
        lblRatingAverage.setIconTextGap(6);
        lblRatingAverage.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblRatingAverage.setForeground(new Color(180, 83, 9)); // Amber color

        details.add(lblTitle);
        details.add(Box.createVerticalStrut(4));
        details.add(lblAuthor);
        details.add(lblCategory);
        details.add(Box.createVerticalStrut(4));
        details.add(lblRatingAverage);

        // Mô tả tóm tắt ngắn ở phần đầu
        String shortDesc = getShortBookSynopsis(book);
        JLabel lblDesc = new JLabel("<html><p style='width:240px; color:#475569; font-size:11px; margin-top:4px;'>"
            + shortDesc + "</p></html>");
        lblDesc.setFont(UITheme.FONT_SMALL);
        details.add(Box.createVerticalStrut(4));
        details.add(lblDesc);

        topInfoPanel.add(lblCover, BorderLayout.WEST);
        topInfoPanel.add(details, BorderLayout.CENTER);
        add(topInfoPanel, BorderLayout.NORTH);

        // 2. Tabbed / Phân vùng: Tóm tắt nội dung, Vị trí Kệ sách & Bình luận
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);

        // Tab 1: Khối Mô tả tóm tắt nội dung sách chuyên sâu
        JPanel descTab = createDescriptionPanel();
        tabbedPane.addTab("Tóm tắt nội dung", descTab);

        // Tab 2: Vị trí kệ sách & Cuốn vật lý
        JPanel shelfPanel = createShelfLocationPanel();
        tabbedPane.addTab("Vị trí Kệ sách & Tồn kho", shelfPanel);

        // Tab 3: Bình luận & Đánh giá (UC-8)
        JPanel commentsTab = createCommentsPanel();
        tabbedPane.addTab("Đánh giá & Bình luận", commentsTab);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createDescriptionPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(12, 14, 12, 14));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        JLabel lblHeader = new JLabel("Tóm tắt nội dung tác phẩm:");
        lblHeader.setFont(UITheme.FONT_BOLD);
        lblHeader.setForeground(UITheme.TEXT_MAIN);
        lblHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(lblHeader);
        contentPanel.add(Box.createVerticalStrut(8));

        String fullSynopsis = getRichBookSynopsis(book);
        JTextArea txtSynopsis = new JTextArea(fullSynopsis);
        txtSynopsis.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSynopsis.setForeground(new Color(51, 65, 85));
        txtSynopsis.setLineWrap(true);
        txtSynopsis.setWrapStyleWord(true);
        txtSynopsis.setEditable(false);
        txtSynopsis.setOpaque(false);
        txtSynopsis.setBorder(null);
        txtSynopsis.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(txtSynopsis);

        contentPanel.add(Box.createVerticalStrut(12));

        // Khối thông tin bổ sung: Nhà xuất bản, ISBN, thể loại, tình trạng
        JPanel metaBox = new JPanel(new GridLayout(2, 2, 8, 6));
        metaBox.setOpaque(false);
        metaBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        metaBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        metaBox.add(new JLabel("• Nhà xuất bản: " + (book.getPublisherName() != null ? book.getPublisherName() : "NXB Bách Khoa")));
        metaBox.add(new JLabel("• Mã ISBN: " + (book.getIsbn() != null ? book.getIsbn() : "978-604-56-4275-7")));
        metaBox.add(new JLabel("• Thể loại: " + (book.getCategoryName() != null ? book.getCategoryName() : "Chung")));
        metaBox.add(new JLabel("• Tình trạng: " + (book.getAvailableCopies() > 0 ? "Còn sách (" + book.getAvailableCopies() + " cuốn sẵn sàng)" : "Tạm hết sách")));
        contentPanel.add(metaBox);

        if (book.getAvailableCopies() > 0) {
            contentPanel.add(Box.createVerticalStrut(12));
            JButton btnBorrow = UITheme.createPillButton("Đăng Ký Mượn Sách", UITheme.PRIMARY, Color.WHITE);
            btnBorrow.setFont(UITheme.FONT_BOLD);
            btnBorrow.setAlignmentX(Component.LEFT_ALIGNMENT);
            btnBorrow.setPreferredSize(new Dimension(280, 36));
            btnBorrow.setMaximumSize(new Dimension(300, 36));
            btnBorrow.addActionListener(e -> handleBorrowBook());
            contentPanel.add(btnBorrow);
        }

        JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
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
            btnBorrow.addActionListener(e -> handleBorrowBook());
            panel.add(btnBorrow);
        } else {
            panel.add(Box.createVerticalStrut(14));
            JButton btnReserve = UITheme.createPillButton("Đặt Trước Sách (Khi có lại)", UITheme.TINT, UITheme.ON_TINT);
            btnReserve.setFont(UITheme.FONT_BOLD);
            btnReserve.setAlignmentX(Component.LEFT_ALIGNMENT);
            btnReserve.setPreferredSize(new Dimension(280, 38));
            btnReserve.setMaximumSize(new Dimension(300, 38));
            btnReserve.addActionListener(e -> handleReserveBook());
            panel.add(btnReserve);
        }

        return panel;
    }

    private void handleReserveBook() {
        com.ximilib.domain.model.User user = UserSession.getCurrentUser();
        if (user != null && user.isGuest()) {
            UiModalHelper.closeModal("BOOK_DETAIL");
            if (com.ximilib.presentation.view.MainDashboardFrame.getInstance() != null) {
                com.ximilib.presentation.view.MainDashboardFrame.getInstance().promptGuestLogin(
                    "Bạn đang duyệt thư viện với tư cách Khách viếng thăm.<br><br>"
                    + "Để <b>Đặt trước</b> cuốn sách <b>" + book.getTitle() + "</b> khi có bản sao mới, vui lòng <b>Đăng nhập</b> tài khoản bạn đọc."
                );
            }
            return;
        }
        UiModalHelper.showToastInfo(this, "Yêu cầu đặt trước cuốn sách '" + book.getTitle() + "' đã được ghi nhận vào hàng chờ ưu tiên!");
    }

    private void handleBorrowBook() {
        com.ximilib.domain.model.User user = UserSession.getCurrentUser();
        if (user != null && user.isGuest()) {
            UiModalHelper.closeModal("BOOK_DETAIL");
            if (com.ximilib.presentation.view.MainDashboardFrame.getInstance() != null) {
                com.ximilib.presentation.view.MainDashboardFrame.getInstance().promptGuestLogin(
                    "Bạn đang duyệt thư viện với tư cách Khách viếng thăm.<br><br>"
                    + "Để đăng ký mượn cuốn sách <b>" + book.getTitle() + "</b>, vui lòng <b>Đăng nhập</b> tài khoản bạn đọc hoặc liên hệ thủ thư tại quầy để được cấp thẻ thư viện."
                );
            }
            return;
        }
        UiModalHelper.closeModal("BOOK_DETAIL");
        if (com.ximilib.presentation.view.MainDashboardFrame.getInstance() != null) {
            com.ximilib.presentation.view.MainDashboardFrame.getInstance().openBorrowRegistration(book);
        }
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
                setText(value + " Sao");
                setIcon(com.ximilib.presentation.util.IconFactory.createStarIcon(new Color(245, 158, 11), 12));
                setIconTextGap(6);
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
        com.ximilib.domain.model.User user = UserSession.getCurrentUser();
        if (user == null || user.isGuest()) {
            UiModalHelper.closeModal("BOOK_DETAIL");
            if (com.ximilib.presentation.view.MainDashboardFrame.getInstance() != null) {
                com.ximilib.presentation.view.MainDashboardFrame.getInstance().promptGuestLogin(
                    "Vui lòng đăng nhập để gửi bình luận"
                );
            } else {
                UiModalHelper.showToastWarning(this, "Vui lòng đăng nhập để gửi bình luận");
            }
            return;
        }

        String content = txtNewComment.getText().trim();
        if (content.isEmpty()) {
            UiModalHelper.showToastWarning(this, "Vui lòng nhập nội dung nhận xét!");
            return;
        }

        String userId = user.getId();
        int stars = (Integer) cmbRating.getSelectedItem();

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                commentPort.addComment(book.getBookId(), userId, content, stars);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    txtNewComment.setText("");
                    UiModalHelper.showToastSuccess(BookDetailModalPanel.this, "Cảm ơn bạn đã gửi đánh giá sách!");
                    loadLocationAndCommentsAsync();
                } catch (Exception ex) {
                    UiModalHelper.showToastError(BookDetailModalPanel.this, "Lỗi gửi đánh giá: " + ex.getMessage());
                }
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
                    lblRatingAverage.setText(String.format("Đánh giá: %.1f / 5.0 (%d lượt nhận xét)", data.getAverageRating(), data.getTotalReviews()));

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

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        leftHeader.setOpaque(false);

        JLabel lblName = new JLabel(item.author());
        lblName.setFont(UITheme.FONT_BOLD);
        lblName.setForeground(UITheme.TEXT_MAIN);
        leftHeader.add(lblName);

        JLabel lblStars = new JLabel(item.stars() + " / 5");
        lblStars.setIcon(com.ximilib.presentation.util.IconFactory.createStarIcon(new Color(245, 158, 11), 12));
        lblStars.setIconTextGap(4);
        lblStars.setFont(UITheme.FONT_BOLD);
        lblStars.setForeground(new Color(180, 83, 9));
        leftHeader.add(lblStars);

        JLabel lblDate = new JLabel(item.date());
        lblDate.setFont(UITheme.FONT_BADGE);
        lblDate.setForeground(UITheme.TEXT_MUTED);

        header.add(leftHeader, BorderLayout.WEST);
        header.add(lblDate, BorderLayout.EAST);

        JLabel lblText = new JLabel("<html>" + item.text() + "</html>");
        lblText.setFont(UITheme.FONT_REGULAR);
        lblText.setForeground(UITheme.TEXT_MAIN);

        card.add(header, BorderLayout.NORTH);
        card.add(lblText, BorderLayout.CENTER);
        return card;
    }

    private String getShortBookSynopsis(BookSummaryDto book) {
        String desc = book.getDescription();
        if (desc != null && !desc.trim().isEmpty()) {
            return desc.length() > 140 ? desc.substring(0, 137) + "..." : desc;
        }
        String title = (book.getTitle() != null) ? book.getTitle().toLowerCase() : "";
        if (title.contains("sapiens") || title.contains("lược sử")) {
            return "Khám phá lịch sử loài người từ thời kỳ đồ đá đến hiện đại và tương lai giống loài qua góc nhìn nhân chủng học.";
        } else if (title.contains("atomic") || title.contains("thói quen")) {
            return "Phương pháp khoa học xây dựng thói quen tốt và loại bỏ thói quen xấu qua quy tắc cải thiện 1% mỗi ngày.";
        } else if (title.contains("educated") || title.contains("được học")) {
            return "Hồi ký xúc động về hành trình vượt qua nghịch cảnh để vươn tới tri thức của tác giả Tara Westover.";
        } else if (title.contains("dune") || title.contains("xứ cát")) {
            return "Kiệt tác viễn tưởng về hành tinh sa mạc Arrakis, cuộc tranh giành hương dược và số phận của Paul Atreides.";
        } else {
            return "Tác phẩm tiêu biểu thuộc danh mục " + (book.getCategoryName() != null ? book.getCategoryName() : "Thư viện")
                 + ", phục vụ bạn đọc tra cứu tại Thư viện ĐHBK Đà Nẵng.";
        }
    }

    private String getRichBookSynopsis(BookSummaryDto book) {
        String desc = book.getDescription();
        if (desc != null && desc.trim().length() > 30) {
            return desc.trim();
        }

        String title = (book.getTitle() != null) ? book.getTitle().toLowerCase() : "";
        String cat = (book.getCategoryName() != null) ? book.getCategoryName() : "Chuyên ngành";
        String author = (book.getAuthor() != null) ? book.getAuthor() : "Tác giả uy tín";

        if (title.contains("sapiens") || title.contains("lược sử loài người")) {
            return "Cuốn sách đưa độc giả vào chuyến du hành khám phá toàn diện lịch sử tiến hóa của loài người từ thời kỳ đồ đá cho đến kỷ nguyên hiện đại và cách mạng công nghệ sinh học.\n\n"
                 + "Tác giả Yuval Noah Harari phân tích sâu sắc cách Homo sapiens vươn lên làm chủ hành tinh thông qua 3 cuộc cách mạng lớn: Cách mạng Nhận thức, Cách mạng Nông nghiệp và Cách mạng Khoa học. Tác phẩm kích thích tư duy về tôn giáo, tiền tệ, nhà nước và tương lai giống loài.";
        } else if (title.contains("atomic") || title.contains("thói quen") || title.contains("habits")) {
            return "Cuốn sách chia sẻ phương pháp kiến tạo thói quen tốt và triệt tiêu thói quen tiêu cực thông qua quy tắc cải thiện 1% mỗi ngày.\n\n"
                 + "Tác giả James Clear đúc kết 4 quy luật thay đổi hành vi then chốt: Khiến nó rõ ràng, Khiến nó hấp dẫn, Khiến nó dễ dàng và Khiến nó thỏa mãn. Đây là cẩm nang phát triển bản thân thiết thực cho học tập và công việc.";
        } else if (title.contains("educated") || title.contains("được học")) {
            return "Hồi ký tự truyện đầy xúc động và truyền cảm hứng của Tara Westover về sức mạnh biến đổi của giáo dục.\n\n"
                 + "Sinh ra trong gia đình biệt lập tại vùng núi Idaho, cô chưa từng được đến trường hay khám bệnh suốt thời thơ ấu. Bằng nghị lực tự học phi thường, cô thi đỗ đại học và sau đó nhận bằng Tiến sĩ tại Đại học Cambridge danh giá.";
        } else if (title.contains("dune") || title.contains("xứ cát")) {
            return "Thiên sử thi khoa học viễn tưởng kinh điển vĩ đại bậc nhất mọi thời đại của Frank Herbert.\n\n"
                 + "Lấy bối cảnh hành tinh sa mạc Arrakis – nơi duy nhất sản sinh hương dược 'spice melange' quý giá nhất vũ trụ. Câu chuyện khắc họa cuộc đấu tranh sinh tồn, chính trị vương quyền, tôn giáo và sự trỗi dậy của Paul Atreides.";
        } else if (title.contains("1984")) {
            return "Kiệt tác văn học thế kỷ 20 của George Orwell cảnh báo về xã hội toàn trị dưới sự kiểm soát ngặt nghèo của 'Anh Cả' (Big Brother).\n\n"
                 + "Tác phẩm khắc họa cuộc đấu tranh nội tâm của Winston Smith nhằm gìn giữ sự thật lịch sử, ký ức chân thực và quyền tự do tư tưởng của con người.";
        } else if (title.contains("clean code") || title.contains("clean architecture") || title.contains("design patterns") || title.contains("lập trình") || title.contains("java") || title.contains("refactoring")) {
            return "Cẩm nang chuyên sâu và tiêu chuẩn ngành dành cho các kỹ sư phần mềm, lập trình viên và sinh viên công nghệ thông tin.\n\n"
                 + "Sách hướng dẫn cách viết mã nguồn sạch, dễ đọc, dễ kiểm thử và có khả năng mở rộng lâu dài; áp dụng thuần thục các nguyên lý SOLID, mẫu thiết kế hướng đối tượng và kỹ thuật tối ưu hóa kiến trúc ứng dụng.";
        } else {
            return "Tác phẩm tiêu biểu thuộc danh mục " + cat + " của tác giả " + author + ".\n\n"
                 + "Sách được tuyển chọn và đưa vào phục vụ học tập, tra cứu và nghiên cứu chuyên sâu tại Thư viện ĐHBK Đà Nẵng. Nội dung cung cấp nền tảng lý luận vững chắc, kết hợp với các ví dụ thực tiễn sinh động, giúp bạn đọc mở rộng thế giới quan và nâng cao kiến thức chuyên môn.";
        }
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
        detailPanel.setPreferredSize(new Dimension(550, 490));
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
