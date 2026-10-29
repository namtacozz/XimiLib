package com.ximilib.presentation.view.home;

import com.ximilib.application.dto.BookSearchCriteria;
import com.ximilib.application.dto.BookSummaryDto;
import com.ximilib.application.dto.ReaderDashboardDto;
import com.ximilib.application.dto.StaffDashboardDto;
import com.ximilib.application.service.BookCatalogService;
import com.ximilib.application.service.CirculationService;
import com.ximilib.application.service.DashboardService;
import com.ximilib.domain.model.LoanDueClassification;
import com.ximilib.presentation.util.UITheme;
import com.ximilib.presentation.util.UserSession;
import com.ximilib.domain.model.User;
import com.ximilib.presentation.component.BookCoverTile;
import com.ximilib.presentation.component.BookDetailModalPanel;
import com.ximilib.presentation.component.SectionHeader;
import com.ximilib.presentation.component.EmptyStatePanel;
import com.ximilib.presentation.util.Illustrations;
import com.ximilib.presentation.util.UiModalHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * ReaderHomePanel: Trang chủ vận hành dành cho Độc giả theo phong cách Libby / Goodreads.
 * - Lời chào cá nhân hóa, mã thẻ và trạng thái thẻ.
 * - Chip giờ mở cửa đóng lúc 17:00 theo quy định SRS.
 * - Banner cảnh báo nợ phạt / quá hạn nổi bật (nền đỏ nhạt, chữ đỏ).
 * - Khối sách đang mượn hiển thị bìa 2:3, hạn trả và nút gia hạn trực tuyến.
 * - Khối sách mới nhập với bìa 2:3 có thể nhấn để xem chi tiết.
 * - 100% dữ liệu truy vấn từ MySQL qua DashboardService ngoài EDT.
 */
public class ReaderHomePanel extends JPanel {

    private final DashboardService dashboardService;
    private final CirculationService circulationService;
    private final BookCatalogService bookCatalogService;
    private final Consumer<String> navigationCallback;

    private final JLabel lblGreeting = new JLabel();
    private final JLabel lblCardTag = new JLabel();
    private final JLabel lblOpeningStatus = new JLabel();

    private final JPanel alertBanner = new JPanel(new BorderLayout(12, 0));
    private final JLabel lblAlertMessage = new JLabel();
    private final JButton btnAlertAction = new JButton("Chi tiết nợ phạt");

    private final JPanel loansContentPanel = new JPanel();
    private final JPanel newBooksGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 12));

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));

    public ReaderHomePanel(DashboardService dashboardService,
                           CirculationService circulationService,
                           BookCatalogService bookCatalogService,
                           Consumer<String> navigationCallback) {
        this.dashboardService = dashboardService;
        this.circulationService = circulationService;
        this.bookCatalogService = bookCatalogService;
        this.navigationCallback = navigationCallback;

        setBackground(UITheme.PAGE_BG);
        setLayout(new BorderLayout());

        initUI();
        loadDashboardDataAsync();
    }

    private void initUI() {
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(UITheme.PAGE_BG);
        scrollContent.setBorder(new EmptyBorder(20, 28, 28, 28));

        // 1. Header: Lời chào + Giờ mở cửa
        scrollContent.add(createHeaderBar());
        scrollContent.add(Box.createVerticalStrut(14));

        // 2. Alert Banner (ẩn mặc định, chỉ hiện khi có nợ phạt / quá hạn)
        setupAlertBanner();
        scrollContent.add(alertBanner);
        scrollContent.add(Box.createVerticalStrut(16));

        // 3. Sách bạn đang mượn
        SectionHeader loanHeader = new SectionHeader(
            "Sách bạn đang mượn",
            "Xem tất cả phiếu mượn",
            () -> navigationCallback.accept("MY_LOANS")
        );
        scrollContent.add(loanHeader);
        scrollContent.add(Box.createVerticalStrut(10));

        loansContentPanel.setOpaque(false);
        loansContentPanel.setLayout(new BoxLayout(loansContentPanel, BoxLayout.Y_AXIS));
        scrollContent.add(loansContentPanel);
        scrollContent.add(Box.createVerticalStrut(24));

        // 4. Sách mới nhập
        SectionHeader booksHeader = new SectionHeader(
            "Sách mới nhập",
            "Xem toàn bộ kho sách",
            () -> navigationCallback.accept("CATALOG")
        );
        scrollContent.add(booksHeader);
        scrollContent.add(Box.createVerticalStrut(10));

        newBooksGrid.setOpaque(false);
        scrollContent.add(newBooksGrid);

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderBar() {
        JPanel bar = new JPanel(new BorderLayout(16, 0));
        bar.setOpaque(false);

        JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftBox.setOpaque(false);

        lblGreeting.setFont(UITheme.FONT_TITLE.deriveFont(Font.BOLD, 20f));
        lblGreeting.setForeground(UITheme.TEXT_MAIN);
        leftBox.add(lblGreeting);

        lblCardTag.setFont(UITheme.FONT_REGULAR.deriveFont(12f));
        lblCardTag.setForeground(UITheme.PRIMARY);
        lblCardTag.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.TINT, 1, true),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));
        leftBox.add(lblCardTag);

        bar.add(leftBox, BorderLayout.WEST);

        // Chip giờ mở cửa đóng lúc 17:00
        boolean isOpen = LocalTime.now().isBefore(LocalTime.of(17, 0));
        lblOpeningStatus.setFont(UITheme.FONT_REGULAR.deriveFont(12f));
        if (isOpen) {
            lblOpeningStatus.setText("● Mở cửa · đóng lúc 17:00");
            lblOpeningStatus.setForeground(UITheme.SUCCESS_GREEN);
        } else {
            lblOpeningStatus.setText("● Đã đóng cửa · mở lại 07:30");
            lblOpeningStatus.setForeground(UITheme.TEXT_MUTED);
        }
        bar.add(lblOpeningStatus, BorderLayout.EAST);

        return bar;
    }

    private void setupAlertBanner() {
        alertBanner.setBackground(UITheme.DANGER_BG);
        alertBanner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#FECACA"), 1, true),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));
        alertBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        alertBanner.setVisible(false);

        lblAlertMessage.setFont(UITheme.FONT_REGULAR.deriveFont(Font.BOLD, 13f));
        lblAlertMessage.setForeground(UITheme.DANGER_TEXT);
        alertBanner.add(lblAlertMessage, BorderLayout.CENTER);

        btnAlertAction.setFont(UITheme.FONT_REGULAR.deriveFont(12f));
        btnAlertAction.setForeground(Color.WHITE);
        btnAlertAction.setBackground(UITheme.DANGER_TEXT);
        btnAlertAction.setFocusPainted(false);
        btnAlertAction.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAlertAction.addActionListener(e -> navigationCallback.accept("MY_FINES"));
        alertBanner.add(btnAlertAction, BorderLayout.EAST);
    }

    public void loadDashboardDataAsync() {
        User currentUser = UserSession.getCurrentUser();
        String userId = (currentUser != null) ? currentUser.getId() : "";
        String fullName = (currentUser != null && currentUser.getFullName() != null) ? currentUser.getFullName() : "Bạn đọc";

        lblGreeting.setText("Xin chào, " + fullName);

        new SwingWorker<ReaderDashboardDto.ReaderDashboardData, Void>() {
            @Override
            protected ReaderDashboardDto.ReaderDashboardData doInBackground() {
                return dashboardService.getReaderDashboardData(userId);
            }

            @Override
            protected void done() {
                try {
                    ReaderDashboardDto.ReaderDashboardData data = get();
                    SwingUtilities.invokeLater(() -> renderDashboard(data));
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    private void renderDashboard(ReaderDashboardDto.ReaderDashboardData data) {
        if (data == null) return;

        // 1. Cập nhật thẻ
        ReaderDashboardDto.ReaderSummary summary = data.summary();
        if (summary != null) {
            String cardText = "Thẻ: " + summary.cardNumber();
            if (summary.cardExpired()) {
                cardText += " (Hết hạn)";
                lblCardTag.setForeground(UITheme.DANGER_TEXT);
            } else if (summary.cardActive()) {
                cardText += " (Hoạt động)";
                lblCardTag.setForeground(UITheme.SUCCESS_GREEN);
            }
            lblCardTag.setText(cardText);

            // 2. Cảnh báo nợ phạt / quá hạn
            if (summary.hasViolationOrDebt()) {
                StringBuilder msg = new StringBuilder("⚠️ Bạn có ");
                if (summary.overdueLoansCount() > 0) {
                    msg.append(summary.overdueLoansCount()).append(" cuốn sách quá hạn. ");
                }
                if (summary.unpaidFineAmount() != null && summary.unpaidFineAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
                    msg.append("Tiền phạt chưa đóng: ").append(currencyFormatter.format(summary.unpaidFineAmount())).append(". ");
                }
                msg.append("Vui lòng xử lý để tiếp tục mượn sách.");
                lblAlertMessage.setText(msg.toString());
                alertBanner.setVisible(true);
            } else {
                alertBanner.setVisible(false);
            }
        }

        // 3. Render danh sách sách đang mượn
        renderActiveLoans(data.activeLoans());

        // 4. Render danh sách sách mới nhập
        renderNewBooks(data.newBooks());

        revalidate();
        repaint();
    }

    private void renderActiveLoans(List<ReaderDashboardDto.ReaderActiveLoan> loans) {
        loansContentPanel.removeAll();

        if (loans == null || loans.isEmpty()) {
            EmptyStatePanel emptyPanel = new EmptyStatePanel(
                Illustrations.EMPTY_BORROW,
                110,
                "Bạn hiện không mượn cuốn sách nào",
                "Khám phá hàng ngàn đầu sách hấp dẫn và cập nhật tại thư viện.",
                "Khám phá kho sách",
                () -> navigationCallback.accept("CATALOG")
            );
            emptyPanel.setBackground(UITheme.CARD_BG);
            emptyPanel.setOpaque(true);
            emptyPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)
            ));
            emptyPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
            loansContentPanel.add(emptyPanel);
        } else {
            for (ReaderDashboardDto.ReaderActiveLoan loan : loans) {
                loansContentPanel.add(createLoanItemCard(loan));
                loansContentPanel.add(Box.createVerticalStrut(10));
            }
        }
    }

    private JPanel createLoanItemCard(ReaderDashboardDto.ReaderActiveLoan loan) {
        JPanel card = new JPanel(new BorderLayout(16, 0));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        // Bìa sách nhỏ (2:3 ~ 46x69px)
        BookCoverTile coverTile = new BookCoverTile(
            loan.coverImage(), loan.bookTitle(), loan.authorName(), 1,
            () -> openBookDetail(loan.bookId())
        );
        card.add(coverTile, BorderLayout.WEST);

        // Chi tiết ở giữa
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(loan.bookTitle());
        lblTitle.setFont(UITheme.FONT_REGULAR.deriveFont(Font.BOLD, 14f));
        lblTitle.setForeground(UITheme.TEXT_MAIN);
        lblTitle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblTitle.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openBookDetail(loan.bookId());
            }
        });

        JLabel lblAuthor = new JLabel(loan.authorName());
        lblAuthor.setFont(UITheme.FONT_REGULAR.deriveFont(12f));
        lblAuthor.setForeground(UITheme.TEXT_MUTED);

        JLabel lblDates = new JLabel(
            "Mượn: " + loan.borrowDate().format(dateFormatter) + "  ·  Hạn trả: " + loan.dueDate().format(dateFormatter)
        );
        lblDates.setFont(UITheme.FONT_REGULAR.deriveFont(12f));
        lblDates.setForeground(UITheme.TEXT_MUTED);

        centerPanel.add(lblTitle);
        centerPanel.add(Box.createVerticalStrut(2));
        centerPanel.add(lblAuthor);
        centerPanel.add(Box.createVerticalStrut(4));
        centerPanel.add(lblDates);

        card.add(centerPanel, BorderLayout.CENTER);

        // Bên phải: Badge hạn + Nút gia hạn
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        rightPanel.setOpaque(false);

        JLabel lblBadge = new JLabel(loan.classification().displayText());
        lblBadge.setFont(UITheme.FONT_REGULAR.deriveFont(Font.BOLD, 11f));
        lblBadge.setOpaque(true);
        lblBadge.setBackground(loan.classification().backgroundColor());
        lblBadge.setForeground(loan.classification().textColor());
        lblBadge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(loan.classification().borderColor(), 1, true),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
        rightPanel.add(lblBadge);

        JButton btnRenew = new JButton("Gia hạn (+14 ngày)");
        btnRenew.setFont(UITheme.FONT_REGULAR.deriveFont(11f));
        btnRenew.setFocusPainted(false);

        if (loan.canRenew()) {
            btnRenew.setBackground(UITheme.CARD_BG);
            btnRenew.setForeground(UITheme.PRIMARY);
            btnRenew.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.PRIMARY, 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));
            btnRenew.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnRenew.addActionListener(e -> handleRenewLoan(loan));
        } else {
            btnRenew.setEnabled(false);
            btnRenew.setForeground(UITheme.TEXT_MUTED);
            btnRenew.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));
            btnRenew.setToolTipText(loan.renewDisabledReason());
        }
        rightPanel.add(btnRenew);

        card.add(rightPanel, BorderLayout.EAST);
        return card;
    }

    private void handleRenewLoan(ReaderDashboardDto.ReaderActiveLoan loan) {
        UiModalHelper.showConfirm(
            this,
            "Xác nhận gia hạn mượn sách",
            "Bạn có muốn gia hạn thêm 14 ngày cho cuốn sách '" + loan.bookTitle() + "' không?\n(Mỗi phiếu chỉ được gia hạn tối đa 1 lần).",
            () -> {
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        circulationService.renewLoan(loan.ticketId(), "Độc giả tự gia hạn qua cổng trực tuyến");
                        return null;
                    }

                    @Override
                    protected void done() {
                        try {
                            get();
                            SwingUtilities.invokeLater(() -> {
                                UiModalHelper.showToastSuccess(ReaderHomePanel.this, "Gia hạn sách thành công thêm 14 ngày!");
                                loadDashboardDataAsync();
                            });
                        } catch (Exception ex) {
                            String err = (ex.getCause() != null) ? ex.getCause().getMessage() : ex.getMessage();
                            SwingUtilities.invokeLater(() -> UiModalHelper.showToastError(ReaderHomePanel.this, "Không thể gia hạn: " + err));
                        }
                    }
                }.execute();
            }
        );
    }

    private void renderNewBooks(List<StaffDashboardDto.NewBookTileDto> books) {
        newBooksGrid.removeAll();

        if (books == null || books.isEmpty()) {
            JLabel lblEmpty = new JLabel("Chưa có sách mới nhập gần đây.");
            lblEmpty.setFont(UITheme.FONT_REGULAR.deriveFont(13f));
            lblEmpty.setForeground(UITheme.TEXT_MUTED);
            newBooksGrid.add(lblEmpty);
        } else {
            for (StaffDashboardDto.NewBookTileDto book : books) {
                BookCoverTile tile = new BookCoverTile(
                    book.coverPath(),
                    book.title(),
                    book.author(),
                    book.availableCopies(),
                    () -> openBookDetail(book.bookId())
                );
                newBooksGrid.add(tile);
            }
        }
    }

    private void openBookDetail(String bookId) {
        new SwingWorker<BookSummaryDto, Void>() {
            @Override
            protected BookSummaryDto doInBackground() {
                var res = bookCatalogService.searchBooks(
                    new BookSearchCriteria(bookId, null, BookSearchCriteria.AvailabilityFilter.ALL),
                    1, 1
                );
                return (res != null && !res.getItems().isEmpty()) ? res.getItems().get(0) : null;
            }

            @Override
            protected void done() {
                try {
                    BookSummaryDto book = get();
                    if (book != null) {
                        SwingUtilities.invokeLater(() -> BookDetailModalPanel.show(ReaderHomePanel.this, book));
                    }
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }
}
