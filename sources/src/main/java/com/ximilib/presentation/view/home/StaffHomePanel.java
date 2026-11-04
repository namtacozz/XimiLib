package com.ximilib.presentation.view.home;

import com.ximilib.application.dto.BookSummaryDto;
import com.ximilib.application.dto.StaffDashboardDto;
import com.ximilib.application.service.BookCatalogService;
import com.ximilib.application.service.DashboardService;
import com.ximilib.domain.event.BookStockChangedEvent;
import com.ximilib.domain.event.DomainEventListener;
import com.ximilib.domain.event.DomainEventPublisher;
import com.ximilib.domain.model.LoanDueClassification;
import com.ximilib.presentation.component.KpiCard;
import com.ximilib.presentation.util.UITheme;
import com.ximilib.infrastructure.config.AppContainer;
import com.ximilib.domain.model.User;
import com.ximilib.presentation.component.BookCoverTile;
import com.ximilib.presentation.component.BookDetailModalPanel;
import com.ximilib.presentation.component.EmptyStatePanel;
import com.ximilib.presentation.component.SectionHeader;
import com.ximilib.presentation.util.Illustrations;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * StaffHomePanel: Trang chủ vận hành dành cho Thủ thư và Quản trị viên.
 * - Mô hình dashboard theo chuẩn Stripe / Linear.
 * - 100% dữ liệu nạp từ CSDL MySQL qua DashboardService ngoài EDT.
 * - Hỗ trợ đầy đủ trạng thái Đang tải, Rỗng, Lỗi kèm nút Thử lại độc lập cho từng khối.
 */
public class StaffHomePanel extends JPanel {

    private final DashboardService dashboardService;
    private final BookCatalogService bookCatalogService;
    private final User currentUser;
    private final Consumer<String> navigationHandler;

    // 4 Thẻ KPI
    private KpiCard cardTotalBooks;
    private KpiCard cardActiveLoans;
    private KpiCard cardMembers;
    private KpiCard cardOverdue;

    // Khối Cần xử lý hôm nay
    private SectionHeader todoHeader;
    private final TodoTableModel todoTableModel = new TodoTableModel();
    private JTable tblTodo;
    private JPanel pnlTodoContainer;
    private CardLayout todoCardLayout;

    // Khối Chờ nhận sách
    private SectionHeader pendingHeader;
    private JPanel pnlPendingContainer;
    private CardLayout pendingCardLayout;

    // Khối Sách mới nhập
    private JPanel pnlNewBooksContainer;
    private CardLayout newBooksCardLayout;
    private JScrollPane scrollNewBooks;
    private Timer carouselTimer;
    private int scrollTarget = -1;

    private DomainEventListener<BookStockChangedEvent> stockListener;

    public StaffHomePanel(DashboardService dashboardService,
                          BookCatalogService bookCatalogService,
                          User currentUser,
                          Consumer<String> navigationHandler) {
        this.dashboardService = dashboardService != null ? dashboardService : AppContainer.getInstance().getDashboardService();
        this.bookCatalogService = bookCatalogService != null ? bookCatalogService : AppContainer.getInstance().getBookCatalogService();
        this.currentUser = currentUser;
        this.navigationHandler = navigationHandler;

        setOpaque(false);
        setLayout(new BorderLayout());

        initUI();
        registerEventObserver();
        loadAllDataAsync();
    }

    private void initUI() {
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setOpaque(false);
        scrollContent.setBorder(new EmptyBorder(10, 24, 10, 24));

        // 1. Dòng chào tinh gọn & Chip giờ đóng cửa
        scrollContent.add(createGreetingBar());
        scrollContent.add(Box.createVerticalStrut(10));

        // 2. Hàng 4 thẻ số liệu động (KPI Cards)
        scrollContent.add(createKpiRow());
        scrollContent.add(Box.createVerticalStrut(12));

        // 3. Khối trung tâm: Cần xử lý hôm nay (2/3) + Thao tác nhanh & Chờ nhận sách (1/3)
        scrollContent.add(createMiddleSection());
        scrollContent.add(Box.createVerticalStrut(12));

        // 4. Dải ngang: Sách mới nhập (6 bìa sách 2:3)
        scrollContent.add(createNewBooksSection());

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
    }

    // =========================================================================
    // 1. DÒNG CHÀO & CHIP GIỜ
    // =========================================================================

    private JPanel createGreetingBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        String fullName = (currentUser != null && currentUser.getFullName() != null) ? currentUser.getFullName() : "Thủ thư";
        LocalDate today = LocalDate.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, 'ngày' dd/MM/yyyy", new Locale("vi", "VN"));
        String dateStr = today.format(dtf);
        // Viết hoa chữ cái đầu (Thứ ...)
        dateStr = Character.toUpperCase(dateStr.charAt(0)) + dateStr.substring(1);

        JLabel lblGreeting = new JLabel("Xin chào, " + fullName + "  ·  " + dateStr);
        lblGreeting.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblGreeting.setForeground(UITheme.TEXT_MAIN);

        // Chip đóng/mở cửa theo quy định SRS (Đóng 17:00)
        LocalTime now = LocalTime.now();
        boolean isOpen = now.isBefore(LocalTime.of(17, 0));
        String chipText = isOpen ? "● Mở cửa · đóng lúc 17:00" : "● Đã đóng cửa";
        Color chipBg = isOpen ? UITheme.SUCCESS_BG : UITheme.BG_LIGHT;
        Color chipFg = isOpen ? UITheme.SUCCESS_TEXT : UITheme.TEXT_MUTED;

        JLabel lblChip = new JLabel(chipText);
        lblChip.setFont(UITheme.FONT_BADGE);
        lblChip.setOpaque(true);
        lblChip.setBackground(chipBg);
        lblChip.setForeground(chipFg);
        lblChip.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isOpen ? new Color(187, 247, 208) : UITheme.BORDER, 1),
            BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));

        bar.add(lblGreeting, BorderLayout.WEST);
        bar.add(lblChip, BorderLayout.EAST);
        return bar;
    }

    // =========================================================================
    // 2. 4 THẺ SỐ LIỆU ĐỘNG
    // =========================================================================

    private JPanel createKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));
        row.setPreferredSize(new Dimension(800, 74));

        cardTotalBooks = new KpiCard(
            KpiCard.CardType.TOTAL_BOOKS, "—", "Tổng số đầu sách", "Đang tải…",
            () -> navigate("Kho sách")
        );

        cardActiveLoans = new KpiCard(
            KpiCard.CardType.ACTIVE_LOANS, "—", "Sách đang cho mượn", null,
            () -> navigate("Bàn lưu hành (Desk)")
        );

        cardMembers = new KpiCard(
            KpiCard.CardType.MEMBERS, "—", "Độc giả thành viên", null,
            () -> navigate("Hồ sơ độc giả & Thẻ")
        );

        cardOverdue = new KpiCard(
            KpiCard.CardType.OVERDUE, "—", "Phiếu quá hạn", null,
            () -> navigate("Xử lý trả & bồi hoàn")
        );

        row.add(cardTotalBooks);
        row.add(cardActiveLoans);
        row.add(cardMembers);
        row.add(cardOverdue);
        return row;
    }

    // =========================================================================
    // 3. KHỐI TRUNG TÂM (Cần xử lý 62% + Chờ nhận & Thao tác nhanh 38%)
    // =========================================================================

    private JPanel createMiddleSection() {
        JPanel section = new JPanel(new GridBagLayout());
        section.setOpaque(false);
        section.setPreferredSize(new Dimension(800, 242));
        section.setMinimumSize(new Dimension(650, 220));
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 246));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 14);

        // Nửa trái 62%
        gbc.gridx = 0;
        gbc.weightx = 0.62;
        section.add(createTodoCard(), gbc);

        // Nửa phải 38%
        gbc.gridx = 1;
        gbc.weightx = 0.38;
        gbc.insets = new Insets(0, 0, 0, 0);
        section.add(createRightControlPanel(), gbc);

        return section;
    }

    private JPanel createTodoCard() {
        JPanel card = createWhiteCard();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        todoHeader = new SectionHeader(
            "Cần xử lý hôm nay", null, "Xem tất cả >",
            () -> navigate("Xử lý trả & bồi hoàn")
        );
        card.add(todoHeader, BorderLayout.NORTH);

        todoCardLayout = new CardLayout();
        pnlTodoContainer = new JPanel(todoCardLayout);
        pnlTodoContainer.setOpaque(false);

        // Trạng thái Đang tải
        pnlTodoContainer.add(createStatePlaceholder("Đang tải danh sách cần xử lý…", null), "LOADING");

        // Trạng thái Rỗng
        EmptyStatePanel emptyTodo = new EmptyStatePanel(
            Illustrations.ACTION_SUCCESS,
            70,
            "Hoàn tất xử lý hôm nay",
            "Không còn phiếu mượn hoặc sách quá hạn cần thủ thư xử lý."
        );
        emptyTodo.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        pnlTodoContainer.add(emptyTodo, "EMPTY");

        // Trạng thái Lỗi
        pnlTodoContainer.add(createStatePlaceholder("Không tải được danh sách.", this::loadTodoLoansAsync), "ERROR");

        // Trạng thái Bảng dữ liệu thật
        tblTodo = new JTable(todoTableModel);
        tblTodo.setRowHeight(34);
        tblTodo.setFont(UITheme.FONT_REGULAR);
        tblTodo.setShowHorizontalLines(false);
        tblTodo.setShowVerticalLines(false);
        tblTodo.setIntercellSpacing(new Dimension(0, 0));
        tblTodo.setFillsViewportHeight(true);
        tblTodo.setBackground(UITheme.CARD_BG);
        tblTodo.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblTodo.getTableHeader().setReorderingAllowed(false);
        tblTodo.getTableHeader().setBackground(UITheme.TINT);
        tblTodo.getTableHeader().setForeground(UITheme.TEXT_MAIN);
        tblTodo.getTableHeader().setFont(UITheme.FONT_BOLD);
        tblTodo.getTableHeader().setPreferredSize(new Dimension(0, 28));

        // Render các cột với tỷ lệ co giãn linh hoạt
        tblTodo.getColumnModel().getColumn(0).setPreferredWidth(125);
        tblTodo.getColumnModel().getColumn(1).setPreferredWidth(185);
        tblTodo.getColumnModel().getColumn(2).setPreferredWidth(90);
        tblTodo.getColumnModel().getColumn(3).setPreferredWidth(115);
        tblTodo.getColumnModel().getColumn(4).setPreferredWidth(85);

        tblTodo.getColumnModel().getColumn(3).setCellRenderer(new PillStatusCellRenderer());
        tblTodo.getColumnModel().getColumn(4).setCellRenderer(new ReturnActionCellRenderer());

        tblTodo.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = tblTodo.columnAtPoint(e.getPoint());
                int row = tblTodo.rowAtPoint(e.getPoint());
                if (row >= 0 && col == 4) {
                    navigate("Xử lý trả & bồi hoàn");
                }
            }
        });

        JScrollPane scrollTable = new JScrollPane(tblTodo);
        scrollTable.setBorder(null);
        scrollTable.setOpaque(false);
        scrollTable.getViewport().setOpaque(false);

        pnlTodoContainer.add(scrollTable, "CONTENT");
        card.add(pnlTodoContainer, BorderLayout.CENTER);

        return card;
    }

    private JPanel createRightControlPanel() {
        JPanel rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.setOpaque(false);

        // Khối 1 (Top): Chờ nhận sách (Online) - Đồng hàng và cân đối với "Cần xử lý hôm nay"
        JPanel pnlPending = createWhiteCard();
        pnlPending.setLayout(new BorderLayout(0, 6));
        pnlPending.setBorder(new EmptyBorder(8, 12, 8, 12));
        pnlPending.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlPending.setPreferredSize(new Dimension(300, 142));
        pnlPending.setMinimumSize(new Dimension(240, 128));
        pnlPending.setMaximumSize(new Dimension(Integer.MAX_VALUE, 148));

        pendingHeader = new SectionHeader(
            "Chờ nhận sách (Online)", null, "Xem tất cả >",
            () -> navigate("Bàn lưu hành (Desk)")
        );
        pnlPending.add(pendingHeader, BorderLayout.NORTH);

        pendingCardLayout = new CardLayout();
        pnlPendingContainer = new JPanel(pendingCardLayout);
        pnlPendingContainer.setOpaque(false);

        pnlPendingContainer.add(createStatePlaceholder("Đang tải…", null), "LOADING");
        EmptyStatePanel emptyPending = new EmptyStatePanel(
            Illustrations.STAFF_HOME,
            46,
            "Không có phiếu chờ nhận",
            "Hiện không có bạn đọc nào đặt trước đang chờ lấy sách."
        );
        emptyPending.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        pnlPendingContainer.add(emptyPending, "EMPTY");
        pnlPendingContainer.add(createStatePlaceholder("Lỗi tải phiếu chờ.", this::loadPendingPickupsAsync), "ERROR");

        pnlPending.add(pnlPendingContainer, BorderLayout.CENTER);

        // Khối 2 (Bottom): Thao tác nhanh (4 nút)
        JPanel pnlQuickActions = createWhiteCard();
        pnlQuickActions.setLayout(new BorderLayout(0, 6));
        pnlQuickActions.setBorder(new EmptyBorder(8, 12, 8, 12));
        pnlQuickActions.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlQuickActions.setPreferredSize(new Dimension(300, 92));
        pnlQuickActions.setMinimumSize(new Dimension(240, 85));
        pnlQuickActions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));

        JLabel lblQuickTitle = new JLabel("Thao tác nhanh");
        lblQuickTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblQuickTitle.setForeground(UITheme.TEXT_MAIN);
        pnlQuickActions.add(lblQuickTitle, BorderLayout.NORTH);

        JPanel gridButtons = new JPanel(new GridLayout(2, 2, 6, 6));
        gridButtons.setOpaque(false);

        gridButtons.add(createQuickActionButton("Lập phiếu mượn", UITheme.PRIMARY, Color.WHITE, () -> navigate("Bàn lưu hành (Desk)")));
        gridButtons.add(createQuickActionButton("Trả sách & Phạt", UITheme.TINT, UITheme.ON_TINT, () -> navigate("Xử lý trả & bồi hoàn")));
        gridButtons.add(createQuickActionButton("Thêm độc giả", UITheme.TINT_LIGHT, UITheme.TEXT_MAIN, () -> navigate("Hồ sơ độc giả & Thẻ")));
        gridButtons.add(createQuickActionButton("Quản lý kho", UITheme.TINT_LIGHT, UITheme.TEXT_MAIN, () -> navigate("Kho sách")));

        pnlQuickActions.add(gridButtons, BorderLayout.CENTER);

        rightCol.add(pnlPending);
        rightCol.add(Box.createVerticalStrut(6));
        rightCol.add(pnlQuickActions);

        return rightCol;
    }

    private JButton createQuickActionButton(String text, Color bg, Color fg, Runnable action) {
        JButton btn = UITheme.createPillButton(text, bg, fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setPreferredSize(new Dimension(0, 26));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            if (action != null) action.run();
        });
        return btn;
    }

    // =========================================================================
    // 4. KHỐI SÁCH MỚI NHẬP (6-10 bìa 2:3 lấp đầy dải ngang)
    // =========================================================================

    private JPanel createNewBooksSection() {
        JPanel section = new JPanel(new BorderLayout(0, 8));
        section.setOpaque(false);
        section.setPreferredSize(new Dimension(800, 256));
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        // Bên trái: Tiêu đề "Sách mới nhập" + Badge "Mới"
        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftHeader.setOpaque(false);
        JLabel lblTitle = new JLabel("Sách mới nhập");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(UITheme.TEXT_MAIN);
        leftHeader.add(lblTitle);

        JLabel lblBadge = new JLabel("Mới");
        lblBadge.setFont(UITheme.FONT_BADGE);
        lblBadge.setOpaque(true);
        lblBadge.setBackground(UITheme.TINT);
        lblBadge.setForeground(UITheme.ON_TINT);
        lblBadge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        leftHeader.add(lblBadge);
        headerPanel.add(leftHeader, BorderLayout.WEST);

        // Bên phải: Nút mũi tên chuyển trang ‹ và › (Carousel) + Xem tất cả >
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightHeader.setOpaque(false);

        JButton btnPrev = createCarouselNavButton("‹");
        JButton btnNext = createCarouselNavButton("›");
        btnPrev.setToolTipText("Cuộn sang trái");
        btnNext.setToolTipText("Cuộn sang phải");

        btnPrev.addActionListener(e -> smoothScrollCarousel(-260));
        btnNext.addActionListener(e -> smoothScrollCarousel(260));

        JLabel lblSeeAll = new JLabel("Xem tất cả >");
        lblSeeAll.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSeeAll.setForeground(UITheme.PRIMARY);
        lblSeeAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblSeeAll.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 2));
        lblSeeAll.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                navigate("Kho sách");
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                lblSeeAll.setForeground(UITheme.PRIMARY_HOVER);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                lblSeeAll.setForeground(UITheme.PRIMARY);
            }
        });

        rightHeader.add(btnPrev);
        rightHeader.add(btnNext);
        rightHeader.add(Box.createHorizontalStrut(6));
        rightHeader.add(lblSeeAll);
        headerPanel.add(rightHeader, BorderLayout.EAST);

        section.add(headerPanel, BorderLayout.NORTH);

        newBooksCardLayout = new CardLayout();
        pnlNewBooksContainer = new JPanel(newBooksCardLayout);
        pnlNewBooksContainer.setOpaque(false);

        pnlNewBooksContainer.add(createStatePlaceholder("Đang tải sách mới nhập…", null), "LOADING");
        pnlNewBooksContainer.add(new EmptyStatePanel(
            Illustrations.READER_HOME,
            65,
            "Chưa có sách mới nhập",
            "Dữ liệu sẽ hiển thị ngay khi thủ thư hoàn tất nhập sách mới."
        ), "EMPTY");
        pnlNewBooksContainer.add(createStatePlaceholder("Không tải được sách mới.", this::loadNewBooksAsync), "ERROR");

        section.add(pnlNewBooksContainer, BorderLayout.CENTER);
        return section;
    }

    private JButton createCarouselNavButton(String symbol) {
        JButton btn = new JButton(symbol) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                if (getModel().isPressed()) {
                    g2.setColor(UITheme.TINT);
                    g2.fillOval(1, 1, w - 2, h - 2);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(245, 245, 248));
                    g2.fillOval(1, 1, w - 2, h - 2);
                } else {
                    g2.setColor(Color.WHITE);
                    g2.fillOval(1, 1, w - 2, h - 2);
                }
                g2.setColor(UITheme.BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawOval(1, 1, w - 3, h - 3);

                // Vẽ ký tự mũi tên ở chính giữa tâm nút
                g2.setColor(getModel().isRollover() ? UITheme.PRIMARY : UITheme.TEXT_MAIN);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
                FontMetrics fm = g2.getFontMetrics();
                int textX = (w - fm.stringWidth(symbol)) / 2;
                int textY = (h + fm.getAscent() - fm.getDescent()) / 2 - 1;
                g2.drawString(symbol, textX, textY);

                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(28, 28));
        btn.setMinimumSize(new Dimension(28, 28));
        btn.setMaximumSize(new Dimension(28, 28));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void smoothScrollCarousel(int delta) {
        if (scrollNewBooks == null) return;
        JScrollBar hBar = scrollNewBooks.getHorizontalScrollBar();
        if (hBar == null) return;

        final int max = hBar.getMaximum() - hBar.getVisibleAmount();
        if (max <= 0) return;

        int base = (carouselTimer != null && carouselTimer.isRunning() && scrollTarget >= 0)
            ? scrollTarget
            : hBar.getValue();

        scrollTarget = Math.max(0, Math.min(max, base + delta));
        final int target = scrollTarget;

        if (carouselTimer != null && carouselTimer.isRunning()) {
            carouselTimer.stop();
        }

        final int start = hBar.getValue();
        final int distance = target - start;
        if (distance == 0) return;

        final int frames = 12;
        final int interval = 16;
        final int[] step = {0};

        carouselTimer = new Timer(interval, null);
        carouselTimer.addActionListener(e -> {
            step[0]++;
            float progress = (float) step[0] / frames;
            if (progress >= 1.0f) {
                hBar.setValue(target);
                scrollTarget = -1;
                carouselTimer.stop();
                scrollNewBooks.repaint();
                pnlNewBooksContainer.repaint();
            } else {
                float ease = 1.0f - (float) Math.pow(1.0f - progress, 3);
                hBar.setValue(start + Math.round(distance * ease));
                scrollNewBooks.repaint();
                pnlNewBooksContainer.repaint();
            }
        });
        carouselTimer.start();
    }

    // =========================================================================
    // 5. TRUY VẤN BẤT ĐỒNG BỘ NGOÀI EDT (R-06)
    // =========================================================================

    public void loadAllDataAsync() {
        loadKpisAsync();
        loadTodoLoansAsync();
        loadPendingPickupsAsync();
        loadNewBooksAsync();
    }

    private void loadKpisAsync() {
        new SwingWorker<StaffDashboardDto.StaffKpiSummary, Void>() {
            @Override
            protected StaffDashboardDto.StaffKpiSummary doInBackground() {
                return dashboardService.getStaffKpiSummary();
            }

            @Override
            protected void done() {
                try {
                    StaffDashboardDto.StaffKpiSummary summary = get();
                    SwingUtilities.invokeLater(() -> {
                        cardTotalBooks.updateData(
                            String.format("%,d", summary.totalTitles()),
                            summary.availableCopies() + " cuốn khả dụng"
                        );
                        cardActiveLoans.updateData(String.format("%,d", summary.activeBorrowCopies()), null);
                        cardMembers.updateData(String.format("%,d", summary.activeReaders()), null);
                        cardOverdue.updateData(String.format("%,d", summary.overdueLoans()), null);
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        cardTotalBooks.updateData("!", "Lỗi kết nối");
                        cardActiveLoans.updateData("!", null);
                        cardMembers.updateData("!", null);
                        cardOverdue.updateData("!", null);
                    });
                }
            }
        }.execute();
    }

    private void loadTodoLoansAsync() {
        todoCardLayout.show(pnlTodoContainer, "LOADING");

        new SwingWorker<List<StaffDashboardDto.TodoLoanItem>, Void>() {
            @Override
            protected List<StaffDashboardDto.TodoLoanItem> doInBackground() {
                return dashboardService.getTodoLoans(5);
            }

            @Override
            protected void done() {
                try {
                    List<StaffDashboardDto.TodoLoanItem> list = get();
                    SwingUtilities.invokeLater(() -> {
                        if (list == null || list.isEmpty()) {
                            if (todoHeader != null) todoHeader.setBadge(null);
                            todoCardLayout.show(pnlTodoContainer, "EMPTY");
                        } else {
                            if (todoHeader != null) todoHeader.setBadge(String.valueOf(list.size()));
                            todoTableModel.setItems(list);
                            todoCardLayout.show(pnlTodoContainer, "CONTENT");
                        }
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> todoCardLayout.show(pnlTodoContainer, "ERROR"));
                }
            }
        }.execute();
    }

    private void loadPendingPickupsAsync() {
        pendingCardLayout.show(pnlPendingContainer, "LOADING");

        new SwingWorker<List<StaffDashboardDto.PendingPickupItem>, Void>() {
            @Override
            protected List<StaffDashboardDto.PendingPickupItem> doInBackground() {
                return dashboardService.getPendingPickups(5);
            }

            @Override
            protected void done() {
                try {
                    List<StaffDashboardDto.PendingPickupItem> list = get();
                    SwingUtilities.invokeLater(() -> {
                        if (list == null || list.isEmpty()) {
                            if (pendingHeader != null) pendingHeader.setBadge(null);
                            pendingCardLayout.show(pnlPendingContainer, "EMPTY");
                        } else {
                            if (pendingHeader != null) pendingHeader.setBadge(String.valueOf(list.size()));
                            JPanel contentPanel = new JPanel();
                            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
                            contentPanel.setOpaque(false);

                            for (StaffDashboardDto.PendingPickupItem item : list) {
                                contentPanel.add(createPendingItemRow(item));
                                contentPanel.add(Box.createVerticalStrut(6));
                            }

                            JScrollPane scroll = new JScrollPane(contentPanel);
                            scroll.setBorder(null);
                            scroll.setOpaque(false);
                            scroll.getViewport().setOpaque(false);
                            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

                            pnlPendingContainer.add(scroll, "CONTENT");
                            pendingCardLayout.show(pnlPendingContainer, "CONTENT");
                        }
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> pendingCardLayout.show(pnlPendingContainer, "ERROR"));
                }
            }
        }.execute();
    }

    private JPanel createPendingItemRow(StaffDashboardDto.PendingPickupItem item) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        // 1. Bên trái: Avatar chữ cái tròn
        String initials = "Đ";
        if (item.readerName() != null && !item.readerName().isBlank()) {
            String trimmed = item.readerName().trim();
            int lastSpace = trimmed.lastIndexOf(' ');
            initials = String.valueOf(lastSpace >= 0 ? trimmed.charAt(lastSpace + 1) : trimmed.charAt(0)).toUpperCase();
        }

        final String avatarLetter = initials;
        JLabel lblInitial = new JLabel(avatarLetter, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.TINT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblInitial.setPreferredSize(new Dimension(24, 24));
        lblInitial.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblInitial.setForeground(UITheme.PRIMARY);

        JPanel pnlAvatar = new JPanel(new GridBagLayout());
        pnlAvatar.setOpaque(false);
        pnlAvatar.add(lblInitial);

        // 2. Ở giữa: 2 dòng chữ CĂN THẲNG CÙNG 1 HÀNG DỌC (cột)
        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);

        String title = item.firstBookTitle() != null ? item.firstBookTitle() : "";
        String fullInfo = item.readerName() + " — " + title;
        String displayInfo = fullInfo.length() > 22 ? fullInfo.substring(0, 20) + "…" : fullInfo;

        JLabel lblName = new JLabel(displayInfo);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblName.setForeground(UITheme.TEXT_MAIN);
        lblName.setToolTipText(fullInfo + " (Mã phiếu: " + item.ticketId() + ")");
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Chuỗi hạn nhận (chỉ giữ ĐÚNG 1 chữ "Hạn:", loại bỏ chữ lặp lại)
        String rawNote = item.pickupDeadlineNote() != null ? item.pickupDeadlineNote().trim() : "";
        while (rawNote.toLowerCase().startsWith("hạn:") || rawNote.toLowerCase().startsWith("hạn")) {
            int colonIdx = rawNote.indexOf(':');
            if (colonIdx >= 0) {
                rawNote = rawNote.substring(colonIdx + 1).trim();
            } else {
                rawNote = rawNote.substring(3).trim();
            }
        }
        JLabel lblDeadline = new JLabel("Hạn: " + rawNote);
        lblDeadline.setFont(UITheme.FONT_BADGE);
        lblDeadline.setForeground(UITheme.PRIMARY);
        lblDeadline.setAlignmentX(Component.LEFT_ALIGNMENT);

        textCol.add(Box.createVerticalGlue());
        textCol.add(lblName);
        textCol.add(Box.createVerticalStrut(2));
        textCol.add(lblDeadline);
        textCol.add(Box.createVerticalGlue());

        // 3. Bên phải: Nút "Giao sách" hiển thị 100% đầy đủ không bị đè hay cắt chữ
        JPanel pnlButton = new JPanel(new GridBagLayout());
        pnlButton.setOpaque(false);

        JButton btnDeliver = UITheme.createPillButton("Giao sách", UITheme.PRIMARY, Color.WHITE);
        btnDeliver.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnDeliver.setPreferredSize(new Dimension(88, 26));
        btnDeliver.setMinimumSize(new Dimension(88, 26));
        btnDeliver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDeliver.setMargin(new Insets(2, 8, 2, 8));
        btnDeliver.addActionListener(e -> navigate("Bàn lưu hành (Desk)"));
        pnlButton.add(btnDeliver);

        card.add(pnlAvatar, BorderLayout.WEST);
        card.add(textCol, BorderLayout.CENTER);
        card.add(pnlButton, BorderLayout.EAST);
        return card;
    }

    private void loadNewBooksAsync() {
        newBooksCardLayout.show(pnlNewBooksContainer, "LOADING");

        new SwingWorker<List<StaffDashboardDto.NewBookTileDto>, Void>() {
            @Override
            protected List<StaffDashboardDto.NewBookTileDto> doInBackground() {
                return dashboardService.getNewBooks(10);
            }

            @Override
            protected void done() {
                try {
                    List<StaffDashboardDto.NewBookTileDto> list = get();
                    SwingUtilities.invokeLater(() -> {
                        if (list == null || list.isEmpty()) {
                            newBooksCardLayout.show(pnlNewBooksContainer, "EMPTY");
                        } else {
                            JPanel tilesRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
                            tilesRow.setOpaque(true);
                            tilesRow.setBackground(UITheme.PAGE_BG);

                            for (StaffDashboardDto.NewBookTileDto b : list) {
                                tilesRow.add(new BookCoverTile(
                                    b,
                                    StaffHomePanel.this::openBookDetail,
                                    book -> navigate("Bàn lưu hành (Desk)")
                                ));
                            }

                            scrollNewBooks = new JScrollPane(tilesRow);
                            scrollNewBooks.setBorder(null);
                            scrollNewBooks.setOpaque(true);
                            scrollNewBooks.setBackground(UITheme.PAGE_BG);
                            scrollNewBooks.getViewport().setOpaque(true);
                            scrollNewBooks.getViewport().setBackground(UITheme.PAGE_BG);
                            scrollNewBooks.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
                            scrollNewBooks.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                            scrollNewBooks.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
                            scrollNewBooks.getHorizontalScrollBar().setUnitIncrement(24);
                            scrollNewBooks.addMouseWheelListener(e -> {
                                smoothScrollCarousel(e.getWheelRotation() * 120);
                            });

                            pnlNewBooksContainer.add(scrollNewBooks, "CONTENT");
                            newBooksCardLayout.show(pnlNewBooksContainer, "CONTENT");
                        }
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> newBooksCardLayout.show(pnlNewBooksContainer, "ERROR"));
                }
            }
        }.execute();
    }

    private void openBookDetail(StaffDashboardDto.NewBookTileDto tile) {
        // Mở modal chi tiết sách
        new SwingWorker<BookSummaryDto, Void>() {
            @Override
            protected BookSummaryDto doInBackground() {
                com.ximilib.application.dto.PagedResult<com.ximilib.application.dto.BookSummaryDto> res = bookCatalogService.searchBooks(
                    new com.ximilib.application.dto.BookSearchCriteria(tile.bookId(), null, com.ximilib.application.dto.BookSearchCriteria.AvailabilityFilter.ALL),
                    1, 1
                );
                return (res != null && !res.getItems().isEmpty()) ? res.getItems().get(0) : null;
            }

            @Override
            protected void done() {
                try {
                    BookSummaryDto book = get();
                    if (book != null) {
                        SwingUtilities.invokeLater(() -> BookDetailModalPanel.show(StaffHomePanel.this, book));
                    }
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    private void registerEventObserver() {
        stockListener = event -> SwingUtilities.invokeLater(this::loadAllDataAsync);
        DomainEventPublisher.subscribe(BookStockChangedEvent.class, stockListener);
    }

    private void navigate(String route) {
        if (navigationHandler != null) {
            navigationHandler.accept(route);
        }
    }

    // =========================================================================
    // 6. HELPER COMPONENTS & RENDERERS
    // =========================================================================

    private JPanel createWhiteCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(UITheme.BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        return card;
    }

    private JPanel createStatePlaceholder(String message, Runnable onRetry) {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);

        JLabel lbl = new JLabel(message);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(UITheme.TEXT_MUTED);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.add(lbl);

        if (onRetry != null) {
            box.add(Box.createVerticalStrut(8));
            JButton btnRetry = UITheme.createPillButton("Thử lại", UITheme.TINT, UITheme.ON_TINT);
            btnRetry.setFont(UITheme.FONT_BADGE);
            btnRetry.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnRetry.addActionListener(e -> onRetry.run());
            box.add(btnRetry);
        }

        pnl.add(box);
        return pnl;
    }

    // Table Model cho khối Cần xử lý
    private static class TodoTableModel extends AbstractTableModel {
        private final String[] columns = {"Độc giả", "Sách", "Hạn trả", "Trạng thái", "Thao tác"};
        private List<StaffDashboardDto.TodoLoanItem> items = List.of();
        private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        public void setItems(List<StaffDashboardDto.TodoLoanItem> items) {
            this.items = items != null ? items : List.of();
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return items.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int col) { return columns[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            if (row < 0 || row >= items.size()) return null;
            StaffDashboardDto.TodoLoanItem item = items.get(row);
            return switch (col) {
                case 0 -> item.readerName();
                case 1 -> item.getDisplayBookTitle();
                case 2 -> item.dueDate().format(dtf);
                case 3 -> item.classification();
                case 4 -> "Trả sách";
                default -> null;
            };
        }
    }

    // Renderer cho cột Trạng thái pill
    private static class PillStatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            if (!(value instanceof LoanDueClassification classification)) {
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }

            Color bg = switch (classification.type()) {
                case OVERDUE -> UITheme.DANGER_BG;
                case DUE_TODAY -> UITheme.WARNING_BG;
                case DUE_SOON -> UITheme.SUCCESS_BG;
            };
            Color fg = switch (classification.type()) {
                case OVERDUE -> UITheme.DANGER_TEXT;
                case DUE_TODAY -> UITheme.WARNING_TEXT;
                case DUE_SOON -> UITheme.SUCCESS_TEXT;
            };

            JLabel lbl = new JLabel(classification.label(), SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(bg);
                    g2.fillRoundRect(4, 5, getWidth() - 8, getHeight() - 10, getHeight() - 10, getHeight() - 10);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            lbl.setFont(UITheme.FONT_BADGE);
            lbl.setForeground(fg);
            lbl.setOpaque(false);
            return lbl;
        }
    }

    // Renderer cho nút nhỏ "Trả sách" viền hồng
    private static class ReturnActionCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JPanel pnl = new JPanel(new GridBagLayout());
            pnl.setOpaque(false);

            JLabel btn = new JLabel("Trả sách", SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(UITheme.TINT);
                    g2.fillRoundRect(2, 3, getWidth() - 4, getHeight() - 6, 8, 8);
                    g2.setColor(UITheme.PRIMARY);
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.drawRoundRect(2, 3, getWidth() - 5, getHeight() - 7, 8, 8);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            btn.setFont(UITheme.FONT_BADGE);
            btn.setForeground(UITheme.PRIMARY);
            btn.setPreferredSize(new Dimension(64, 22));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            pnl.add(btn);
            return pnl;
        }
    }
}
