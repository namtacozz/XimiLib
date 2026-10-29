package com.ximilib.gui.views;

import com.ximilib.application.dto.BookSearchCriteria;
import com.ximilib.application.dto.BookSummaryDto;
import com.ximilib.application.dto.PagedResult;
import com.ximilib.application.service.BookCatalogService;
import com.ximilib.application.service.CirculationService;
import com.ximilib.domain.model.LibraryPolicy;
import com.ximilib.domain.model.LoanType;
import com.ximilib.domain.repository.CirculationRepository.BookCopyInfo;
import com.ximilib.domain.repository.CirculationRepository.UserCardInfo;
import com.ximilib.presentation.component.SearchField;
import com.ximilib.presentation.util.UITheme;
import com.ximilib.domain.model.User;
import com.ximilib.presentation.util.UiModalHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * ReaderBorrowRegistrationView: Giao diện "Đăng ký mượn sách" trực tuyến dành cho Độc giả.
 * Đáp ứng đầy đủ bản Đặc tả SRS 3.2.5 (UC-5: Độc giả đặt mượn sách qua ứng dụng).
 * Tuân thủ quy tắc: R-01, R-02, R-06, BR-02 (Tối đa 3 cuốn 14 ngày), BR-04 (Tại chỗ trả 17:00), BR-07 (Chặn khi nợ phạt).
 */
public class ReaderBorrowRegistrationView extends JPanel {

    private final CirculationService circulationService;
    private final BookCatalogService bookCatalogService;
    private final User currentUser;
    private final Runnable onSuccessNavigate;

    // Header thẻ độc giả
    private JLabel lblCardHolderName;
    private JLabel lblCardNumber;
    private JLabel lblCardStatus;
    private JLabel lblActiveLoansCount;

    // Tìm kiếm & Danh sách chọn sách
    private SearchField txtBookSearch;
    private DefaultListModel<BookSummaryDto> bookListModel;
    private JList<BookSummaryDto> bookList;
    private javax.swing.Timer searchTimer;

    // Khung chi tiết đăng ký mượn
    private JLabel lblCover;
    private JLabel lblBookTitle;
    private JLabel lblBookMeta;
    private JLabel lblBookLocation;
    private JComboBox<String> cmbAvailableCopies;
    private final List<BookCopyInfo> currentCopies = new ArrayList<>();

    // Tùy chọn mượn
    private JRadioButton radVeNha;
    private JRadioButton radTaiCho;
    private JLabel lblDueDatePreview;
    private JTextField txtNotes;
    private JButton btnConfirmBorrow;

    private BookSummaryDto selectedBook;
    private UserCardInfo userCard;

    public ReaderBorrowRegistrationView(CirculationService circulationService,
                                        BookCatalogService bookCatalogService,
                                        User currentUser,
                                        Runnable onSuccessNavigate) {
        this.circulationService = circulationService;
        this.bookCatalogService = bookCatalogService;
        this.currentUser = currentUser;
        this.onSuccessNavigate = onSuccessNavigate;

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(18, 28, 28, 28));

        initUI();
        loadUserCardData();
        searchBooksAsync("");
    }

    private void initUI() {
        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setOpaque(false);

        // 1. Header: Thẻ bạn đọc & Thông tin kiểm tra điều kiện
        mainContainer.add(createCardInfoHeader());
        mainContainer.add(Box.createVerticalStrut(16));

        // 2. Nội dung 2 cột: Cột trái (Tìm & Chọn sách) - Cột phải (Chi tiết & Xác nhận mượn)
        JPanel contentGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        contentGrid.setOpaque(false);

        contentGrid.add(createBookSelectionPanel());
        contentGrid.add(createBorrowConfirmationPanel());

        mainContainer.add(contentGrid);

        JScrollPane scrollPane = new JScrollPane(mainContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createCardInfoHeader() {
        JPanel panel = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(UITheme.CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(14, 20, 14, 20));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JLabel iconBadge = new JLabel("💳");
        iconBadge.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        JPanel infoBox = new JPanel();
        infoBox.setLayout(new BoxLayout(infoBox, BoxLayout.Y_AXIS));
        infoBox.setOpaque(false);

        lblCardHolderName = new JLabel("Độc giả: " + currentUser.getFullName());
        lblCardHolderName.setFont(UITheme.FONT_TITLE);
        lblCardHolderName.setForeground(UITheme.TEXT_MAIN);

        lblCardNumber = new JLabel("Mã thẻ: Đang tải... | Tên đăng nhập: " + currentUser.getUsername());
        lblCardNumber.setFont(UITheme.FONT_SMALL);
        lblCardNumber.setForeground(UITheme.TEXT_MUTED);

        infoBox.add(lblCardHolderName);
        infoBox.add(lblCardNumber);

        left.add(iconBadge);
        left.add(infoBox);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 4));
        right.setOpaque(false);

        lblCardStatus = new JLabel("Trạng thái: Đang kiểm tra");
        lblCardStatus.setFont(UITheme.FONT_BADGE);
        lblCardStatus.setOpaque(true);
        lblCardStatus.setBackground(UITheme.TINT);
        lblCardStatus.setForeground(UITheme.PRIMARY);
        lblCardStatus.setBorder(new EmptyBorder(4, 10, 4, 10));

        lblActiveLoansCount = new JLabel("Đang mượn: 0/3 cuốn về nhà");
        lblActiveLoansCount.setFont(UITheme.FONT_BOLD);
        lblActiveLoansCount.setForeground(UITheme.TEXT_MAIN);

        right.add(lblCardStatus);
        right.add(lblActiveLoansCount);

        panel.add(left, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    private JPanel createBookSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(UITheme.CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Top: Tiêu đề + Ô tìm kiếm sách
        JPanel topBox = new JPanel(new BorderLayout(0, 8));
        topBox.setOpaque(false);

        JLabel lblTitle = new JLabel("1. Tìm & Chọn đầu sách muốn mượn");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);

        txtBookSearch = new SearchField("Nhập tên sách, tác giả hoặc thể loại...");
        txtBookSearch.setPreferredSize(new Dimension(300, 36));
        txtBookSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (searchTimer != null && searchTimer.isRunning()) {
                    searchTimer.restart();
                } else {
                    searchTimer = new javax.swing.Timer(300, evt -> searchBooksAsync(txtBookSearch.getText().trim()));
                    searchTimer.setRepeats(false);
                    searchTimer.start();
                }
            }
        });

        topBox.add(lblTitle, BorderLayout.NORTH);
        topBox.add(txtBookSearch, BorderLayout.SOUTH);

        // Center: Danh sách kết quả sách
        bookListModel = new DefaultListModel<>();
        bookList = new JList<>(bookListModel);
        bookList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookList.setCellRenderer(new BookListRenderer());
        bookList.setBackground(UITheme.CARD_BG);
        bookList.setFixedCellHeight(68);

        bookList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                BookSummaryDto chosen = bookList.getSelectedValue();
                if (chosen != null) {
                    selectBook(chosen);
                }
            }
        });

        JScrollPane scrollList = new JScrollPane(bookList);
        scrollList.setBorder(BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1));
        scrollList.getVerticalScrollBar().setUnitIncrement(14);

        panel.add(topBox, BorderLayout.NORTH);
        panel.add(scrollList, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBorrowConfirmationPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(UITheme.CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 18, 18, 18));

        // Tiêu đề
        JLabel lblHeader = new JLabel("2. Thông tin đăng ký & Xác nhận");
        lblHeader.setFont(UITheme.FONT_TITLE);
        lblHeader.setForeground(UITheme.TEXT_MAIN);
        panel.add(lblHeader, BorderLayout.NORTH);

        // Body: Thẻ sách đang chọn + Form mượn
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        // Thẻ xem trước sách được chọn
        JPanel bookPreviewCard = new JPanel(new BorderLayout(14, 0));
        bookPreviewCard.setOpaque(false);
        bookPreviewCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1),
            new EmptyBorder(12, 12, 12, 12)
        ));

        lblCover = new JLabel("Chưa chọn", SwingConstants.CENTER);
        lblCover.setPreferredSize(new Dimension(80, 110));
        lblCover.setBorder(BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1));

        JPanel bookDetails = new JPanel();
        bookDetails.setLayout(new BoxLayout(bookDetails, BoxLayout.Y_AXIS));
        bookDetails.setOpaque(false);

        lblBookTitle = new JLabel("Vui lòng chọn một cuốn sách từ danh sách");
        lblBookTitle.setFont(UITheme.FONT_TITLE);
        lblBookTitle.setForeground(UITheme.TEXT_MAIN);

        lblBookMeta = new JLabel("Tác giả: -- | Thể loại: --");
        lblBookMeta.setFont(UITheme.FONT_SMALL);
        lblBookMeta.setForeground(UITheme.TEXT_MUTED);

        lblBookLocation = new JLabel("Vị trí kệ: --");
        lblBookLocation.setFont(UITheme.FONT_SMALL);
        lblBookLocation.setForeground(UITheme.PRIMARY);

        bookDetails.add(lblBookTitle);
        bookDetails.add(Box.createVerticalStrut(4));
        bookDetails.add(lblBookMeta);
        bookDetails.add(Box.createVerticalStrut(4));
        bookDetails.add(lblBookLocation);

        bookPreviewCard.add(lblCover, BorderLayout.WEST);
        bookPreviewCard.add(bookDetails, BorderLayout.CENTER);

        body.add(bookPreviewCard);
        body.add(Box.createVerticalStrut(16));

        // Chọn cuốn sách cụ thể (Mã vạch khả dụng)
        JLabel lblCopySelect = new JLabel("Chọn cuốn sách vật lý (mã vạch khả dụng):");
        lblCopySelect.setFont(UITheme.FONT_BOLD);
        lblCopySelect.setForeground(UITheme.TEXT_MAIN);

        cmbAvailableCopies = new JComboBox<>();
        cmbAvailableCopies.setFont(UITheme.FONT_REGULAR);
        cmbAvailableCopies.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        body.add(lblCopySelect);
        body.add(Box.createVerticalStrut(6));
        body.add(cmbAvailableCopies);
        body.add(Box.createVerticalStrut(14));

        // Hình thức mượn
        JLabel lblFormTitle = new JLabel("Hình thức mượn sách (Quy định SRS):");
        lblFormTitle.setFont(UITheme.FONT_BOLD);
        lblFormTitle.setForeground(UITheme.TEXT_MAIN);

        radVeNha = new JRadioButton("Mượn về nhà (Tối đa 14 ngày - BR-02)");
        radTaiCho = new JRadioButton("Đọc tại chỗ (Hoàn trả trước 17:00 cùng ngày - BR-04)");
        radVeNha.setFont(UITheme.FONT_REGULAR);
        radTaiCho.setFont(UITheme.FONT_REGULAR);
        radVeNha.setSelected(true);

        ButtonGroup grp = new ButtonGroup();
        grp.add(radVeNha);
        grp.add(radTaiCho);

        radVeNha.addActionListener(e -> updateDueDatePreview());
        radTaiCho.addActionListener(e -> updateDueDatePreview());

        body.add(lblFormTitle);
        body.add(Box.createVerticalStrut(4));
        body.add(radVeNha);
        body.add(radTaiCho);
        body.add(Box.createVerticalStrut(12));

        // Thời hạn trả dự kiến
        lblDueDatePreview = new JLabel("Hạn trả dự kiến: --");
        lblDueDatePreview.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDueDatePreview.setForeground(new Color(13, 148, 136)); // Teal color
        body.add(lblDueDatePreview);
        body.add(Box.createVerticalStrut(12));

        // Ghi chú
        JLabel lblNotes = new JLabel("Ghi chú mượn (Tùy chọn):");
        lblNotes.setFont(UITheme.FONT_SMALL);
        lblNotes.setForeground(UITheme.TEXT_MUTED);

        txtNotes = new JTextField();
        txtNotes.setFont(UITheme.FONT_REGULAR);
        txtNotes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txtNotes.putClientProperty("JComponent.roundRect", true);

        body.add(lblNotes);
        body.add(Box.createVerticalStrut(4));
        body.add(txtNotes);

        panel.add(body, BorderLayout.CENTER);

        // Nút bấm đăng ký mượn
        btnConfirmBorrow = UITheme.createPillButton("XÁC NHẬN ĐĂNG KÝ MƯỢN", UITheme.PRIMARY, Color.WHITE);
        btnConfirmBorrow.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnConfirmBorrow.setPreferredSize(new Dimension(Integer.MAX_VALUE, 44));
        btnConfirmBorrow.addActionListener(e -> handleConfirmBorrow());

        panel.add(btnConfirmBorrow, BorderLayout.SOUTH);
        updateDueDatePreview();

        return panel;
    }

    public void selectBook(BookSummaryDto book) {
        if (book == null) return;
        this.selectedBook = book;

        lblBookTitle.setText("<html><b>" + book.getTitle() + "</b></html>");
        lblBookMeta.setText("Tác giả: " + (book.getAuthor() != null ? book.getAuthor() : "Không rõ") +
            " | Thể loại: " + (book.getCategoryName() != null ? book.getCategoryName() : "Không rõ"));

        loadCoverAsync(lblCover, book.getCoverImage());
        loadAvailableCopiesAsync(book.getBookId());
    }

    private void updateDueDatePreview() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        if (radVeNha.isSelected()) {
            LocalDateTime due = LocalDateTime.now().plusDays(LibraryPolicy.LOAN_DAYS_HOME).with(LocalTime.of(23, 59));
            lblDueDatePreview.setText("Hạn trả dự kiến: " + due.format(dtf) + " (Thời hạn 14 ngày)");
        } else {
            LocalDateTime due = LocalDateTime.now().with(LocalTime.of(17, 0));
            lblDueDatePreview.setText("Hạn trả dự kiến: " + due.format(dtf) + " (Trước 17:00 hôm nay)");
        }
    }

    private void loadUserCardData() {
        SwingWorker<UserCardInfo, Void> worker = new SwingWorker<>() {
            @Override
            protected UserCardInfo doInBackground() {
                return circulationService.getCardInfo(currentUser.getUsername());
            }

            @Override
            protected void done() {
                try {
                    userCard = get();
                    if (userCard != null) {
                        lblCardNumber.setText("Mã thẻ: " + userCard.cardNumber() + " | Tên đăng nhập: " + currentUser.getUsername());
                        if (userCard.cardActive() && !userCard.cardExpired()) {
                            lblCardStatus.setText("Thẻ Hợp Lệ (Hoạt động)");
                            lblCardStatus.setBackground(new Color(220, 252, 231));
                            lblCardStatus.setForeground(new Color(22, 101, 52));
                        } else {
                            lblCardStatus.setText("Thẻ Bị Khóa hoặc Hết Hạn");
                            lblCardStatus.setBackground(new Color(254, 226, 226));
                            lblCardStatus.setForeground(new Color(153, 27, 27));
                        }
                    } else {
                        lblCardNumber.setText("Chưa tìm thấy hồ sơ thẻ thư viện!");
                        lblCardStatus.setText("Chưa Có Thẻ");
                    }

                    // Tải số lượng sách đang mượn về nhà
                    var loans = circulationService.getReaderLoans(currentUser.getId());
                    long homeLoans = (loans != null) ? loans.stream().filter(l -> l.getStatus() == 1 && l.getLoanType() == LoanType.VE_NHA).count() : 0;
                    lblActiveLoansCount.setText("Đang mượn: " + homeLoans + "/3 cuốn về nhà");
                } catch (Exception ex) {
                    lblCardStatus.setText("Lỗi kiểm tra thẻ");
                }
            }
        };
        worker.execute();
    }

    private void searchBooksAsync(String keyword) {
        SwingWorker<List<BookSummaryDto>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<BookSummaryDto> doInBackground() {
                BookSearchCriteria criteria = new BookSearchCriteria(keyword, null, BookSearchCriteria.AvailabilityFilter.AVAILABLE_ONLY);
                PagedResult<BookSummaryDto> res = bookCatalogService.searchBooks(criteria, 1, 30);
                return res.getItems();
            }

            @Override
            protected void done() {
                try {
                    List<BookSummaryDto> list = get();
                    bookListModel.clear();
                    for (BookSummaryDto b : list) {
                        bookListModel.addElement(b);
                    }
                    if (selectedBook == null && !list.isEmpty()) {
                        bookList.setSelectedIndex(0);
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        };
        worker.execute();
    }

    private void loadAvailableCopiesAsync(String bookId) {
        cmbAvailableCopies.removeAllItems();
        cmbAvailableCopies.addItem("Đang tải danh sách cuốn sách...");
        currentCopies.clear();

        SwingWorker<List<BookCopyInfo>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<BookCopyInfo> doInBackground() {
                return circulationService.getAvailableCopies(bookId);
            }

            @Override
            protected void done() {
                try {
                    List<BookCopyInfo> copies = get();
                    cmbAvailableCopies.removeAllItems();
                    currentCopies.clear();

                    if (copies == null || copies.isEmpty()) {
                        cmbAvailableCopies.addItem("Hết sách khả dụng trên kệ!");
                        lblBookLocation.setText("Tình trạng: Hiện không còn cuốn nào sẵn sàng trên kệ");
                        lblBookLocation.setForeground(UITheme.DANGER_TEXT);
                        btnConfirmBorrow.setEnabled(true);
                        btnConfirmBorrow.setText("ĐẶT TRƯỚC SÁCH KHI CÓ LẠI (RESERVE)");
                        btnConfirmBorrow.setBackground(new Color(217, 119, 6));
                    } else {
                        currentCopies.addAll(copies);
                        for (BookCopyInfo cp : copies) {
                            cmbAvailableCopies.addItem(cp.copyId() + " (" + cp.shelfLocation() + ")");
                        }
                        lblBookLocation.setText("Vị trí kệ: " + copies.get(0).shelfLocation() + " (Còn " + copies.size() + " cuốn sẵn sàng)");
                        lblBookLocation.setForeground(new Color(13, 148, 136));
                        btnConfirmBorrow.setEnabled(true);
                        btnConfirmBorrow.setText("XÁC NHẬN ĐĂNG KÝ MƯỢN");
                        btnConfirmBorrow.setBackground(UITheme.PRIMARY);
                    }
                } catch (Exception e) {
                    cmbAvailableCopies.removeAllItems();
                    cmbAvailableCopies.addItem("Lỗi tải bản sao");
                }
            }
        };
        worker.execute();
    }

    private void handleConfirmBorrow() {
        if (selectedBook == null) {
            UiModalHelper.showToastWarning(this, "Vui lòng chọn một cuốn sách từ danh sách!");
            return;
        }

        // Hết sách trên kệ: Chuyển sang đặt trước sách (Reservation - SRS YC-05)
        if (currentCopies.isEmpty()) {
            btnConfirmBorrow.setEnabled(false);
            btnConfirmBorrow.setText("Đang ghi nhận đặt trước...");
            SwingWorker<Void, Void> reserveWorker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    circulationService.reserveBook(currentUser.getId(), selectedBook.getBookId());
                    return null;
                }
                @Override
                protected void done() {
                    btnConfirmBorrow.setEnabled(true);
                    btnConfirmBorrow.setText("ĐẶT TRƯỚC SÁCH KHI CÓ LẠI (RESERVE)");
                    try {
                        get();
                        UiModalHelper.showToastSuccess(ReaderBorrowRegistrationView.this,
                            "Đã đặt trước sách thành công! Thư viện sẽ ưu tiên giữ sách cho bạn ngay khi có người trả sách (SRS YC-05).");
                    } catch (Exception ex) {
                        UiModalHelper.showToastError(ReaderBorrowRegistrationView.this, ex.getMessage());
                    }
                }
            };
            reserveWorker.execute();
            return;
        }

        if (cmbAvailableCopies.getSelectedIndex() < 0) {
            UiModalHelper.showToastWarning(this, "Vui lòng chọn mã cuốn sách hợp lệ!");
            return;
        }

        int selectedIdx = cmbAvailableCopies.getSelectedIndex();
        if (selectedIdx >= currentCopies.size()) {
            UiModalHelper.showToastWarning(this, "Vui lòng chọn mã cuốn sách hợp lệ!");
            return;
        }

        BookCopyInfo copy = currentCopies.get(selectedIdx);
        LoanType loanType = radVeNha.isSelected() ? LoanType.VE_NHA : LoanType.TAI_CHO;
        String notes = txtNotes.getText().trim();
        if (notes.isEmpty()) {
            notes = "Độc giả đặt mượn online qua App (SRS 3.2.5)";
        }

        btnConfirmBorrow.setEnabled(false);
        btnConfirmBorrow.setText("Đang ghi nhận...");

        final String copyBarcode = copy.copyId();
        final String noteText = notes;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                circulationService.createLoan(
                    currentUser.getUsername(),
                    List.of(copyBarcode),
                    loanType,
                    noteText
                );
                return null;
            }

            @Override
            protected void done() {
                btnConfirmBorrow.setEnabled(true);
                btnConfirmBorrow.setText("XÁC NHẬN ĐĂNG KÝ MƯỢN");
                try {
                    get();
                    UiModalHelper.showToastSuccess(
                        ReaderBorrowRegistrationView.this,
                        "Đặt mượn thành công! Vui lòng đến quầy xuất trình thẻ thư viện trong 24 giờ để nhận sách."
                    );
                    if (onSuccessNavigate != null) {
                        onSuccessNavigate.run();
                    }
                } catch (Exception ex) {
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    UiModalHelper.showToastError(ReaderBorrowRegistrationView.this, msg);
                }
            }
        };
        worker.execute();
    }

    private static final java.util.Map<String, ImageIcon> COVER_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    private void loadCoverAsync(JLabel targetLabel, String coverPath) {
        if (coverPath == null || coverPath.trim().isEmpty()) {
            targetLabel.setText("No Image");
            targetLabel.setIcon(null);
            return;
        }

        ImageIcon cached = COVER_CACHE.get(coverPath);
        if (cached != null) {
            targetLabel.setIcon(cached);
            targetLabel.setText("");
            return;
        }

        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() {
                try {
                    File file = new File("sources/src/main/resources/assets/covers/" + coverPath);
                    if (!file.exists()) {
                        file = new File("src/main/resources/assets/covers/" + coverPath);
                    }
                    if (file.exists()) {
                        ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                        Image img = icon.getImage().getScaledInstance(80, 110, Image.SCALE_SMOOTH);
                        ImageIcon scaled = new ImageIcon(img);
                        COVER_CACHE.put(coverPath, scaled);
                        return scaled;
                    }
                } catch (Exception ignored) {}
                return null;
            }

            @Override
            protected void done() {
                try {
                    ImageIcon ic = get();
                    if (ic != null) {
                        targetLabel.setIcon(ic);
                        targetLabel.setText("");
                    } else {
                        targetLabel.setText("Bìa sách");
                    }
                } catch (Exception e) {
                    targetLabel.setText("Bìa sách");
                }
            }
        };
        worker.execute();
    }

    private static class BookListRenderer extends JPanel implements ListCellRenderer<BookSummaryDto> {
        private final JLabel lblTitle = new JLabel();
        private final JLabel lblMeta = new JLabel();
        private final JLabel lblBadge = new JLabel();

        public BookListRenderer() {
            setLayout(new BorderLayout(8, 2));
            setBorder(new EmptyBorder(8, 12, 8, 12));
            setOpaque(true);

            lblTitle.setFont(UITheme.FONT_BOLD);
            lblMeta.setFont(UITheme.FONT_SMALL);
            lblMeta.setForeground(UITheme.TEXT_MUTED);

            lblBadge.setFont(UITheme.FONT_BADGE);
            lblBadge.setOpaque(true);
            lblBadge.setBorder(new EmptyBorder(2, 6, 2, 6));

            JPanel center = new JPanel();
            center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
            center.setOpaque(false);
            center.add(lblTitle);
            center.add(Box.createVerticalStrut(3));
            center.add(lblMeta);

            add(center, BorderLayout.CENTER);
            add(lblBadge, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends BookSummaryDto> list,
                                                      BookSummaryDto book, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            if (book == null) return this;

            lblTitle.setText(book.getTitle());
            lblMeta.setText((book.getAuthor() != null ? book.getAuthor() : "Không rõ tác giả") + " • " +
                (book.getCategoryName() != null ? book.getCategoryName() : ""));

            int avail = book.getAvailableCopies();
            if (avail > 0) {
                lblBadge.setText("Còn " + avail + " cuốn");
                lblBadge.setBackground(UITheme.TINT);
                lblBadge.setForeground(UITheme.PRIMARY);
            } else {
                lblBadge.setText("Hết sách");
                lblBadge.setBackground(new Color(241, 245, 249));
                lblBadge.setForeground(UITheme.TEXT_MUTED);
            }

            if (isSelected) {
                setBackground(UITheme.TINT_LIGHT);
                lblTitle.setForeground(UITheme.PRIMARY);
            } else {
                setBackground(Color.WHITE);
                lblTitle.setForeground(UITheme.TEXT_MAIN);
            }

            return this;
        }
    }
}
