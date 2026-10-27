package com.ximilib.gui;

import com.ximilib.gui.components.*;
import com.ximilib.gui.util.IconFactory;
import com.ximilib.gui.util.UITheme;
import com.ximilib.gui.views.*;
import com.ximilib.infrastructure.config.AppContainer;
import com.ximilib.models.User;
import com.ximilib.presentation.component.BookFormModalPanel;
import com.ximilib.presentation.component.NotificationPopupPanel;
import com.ximilib.presentation.component.UserProfileModalPanel;
import com.ximilib.presentation.util.UiModalHelper;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

/**
 * MainDashboardFrame: Khung giao diện chính của hệ thống quản lý thư viện XimiLib
 * được tái hiện chính xác 100% theo phong cách LibRack (librack.com).
 * Tác giả: Phan Vũ Xuân Kiên (GUI / DB)
 */
public class MainDashboardFrame extends JFrame {

    private static MainDashboardFrame instance;

    private final User currentUser;
    private SidebarPanel sidebarPanel;
    private JPanel centerContainer;
    private CardLayout cardLayout;
    private BookCatalogView bookCatalogView;
    private ReaderBorrowRegistrationView readerBorrowView;

    public static MainDashboardFrame getInstance() {
        return instance;
    }

    public MainDashboardFrame(User user) {
        this(user, "Kho sách");
    }

    public MainDashboardFrame(User user, String initialRoute) {
        instance = this;
        this.currentUser = (user != null) ? user : new User(
            "ND002", "kien.phan", "123456",
            "Phan Vũ Xuân Kiên", "kien.phan@dut.udn.vn", "0905 123 456",
            "Thủ Thư (Librarian)"
        );

        setTitle("XimiLib - Bảng Điều Khiển Quản Trị Thư Viện (app.ximilib.vn)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1260, 850);
        setMinimumSize(new Dimension(1050, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_LIGHT);

        // Khởi tạo hệ thống Modal Dialog & GlassPane Popup
        UiModalHelper.initMainFrame(this);

        initUI();

        if (initialRoute != null) {
            navigateTo(initialRoute);
        }
    }

    public MainDashboardFrame(String username, String userRole) {
        this(new User("ND002", username, "123456", username, username + "@dut.udn.vn", "0905 123 456", userRole), "Kho sách");
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getUsername() {
        return currentUser.getUsername();
    }

    public String getUserRole() {
        return currentUser.getRole();
    }

    public void navigateTo(String routeName) {
        if (routeName == null) return;
        if ("Kho sách".equalsIgnoreCase(routeName)) {
            cardLayout.show(centerContainer, "KHO_SACH");
            sidebarPanel.setActiveRoute("Kho sách");
        } else if ("Đăng ký mượn sách".equalsIgnoreCase(routeName)) {
            cardLayout.show(centerContainer, "DANG_KY_MUON");
            sidebarPanel.setActiveRoute("Đăng ký mượn sách");
        } else if ("Trang chủ".equalsIgnoreCase(routeName)) {
            cardLayout.show(centerContainer, "HOME");
            sidebarPanel.setActiveRoute("Trang chủ");
        } else if ("Quản lý thể loại".equalsIgnoreCase(routeName)) {
            cardLayout.show(centerContainer, "THE_LOAI");
            sidebarPanel.setActiveRoute("Quản lý thể loại");
        } else if ("Thùng rác & Khôi phục".equalsIgnoreCase(routeName)) {
            cardLayout.show(centerContainer, "THUNG_RAC");
            sidebarPanel.setActiveRoute("Thùng rác & Khôi phục");
        } else if ("Bàn lưu hành (Desk)".equalsIgnoreCase(routeName) || "Lịch sử phiếu mượn".equalsIgnoreCase(routeName)) {
            cardLayout.show(centerContainer, "BAN_LUU_HANH");
            sidebarPanel.setActiveRoute(routeName);
        } else if ("Xử lý trả & bồi hoàn".equalsIgnoreCase(routeName)) {
            cardLayout.show(centerContainer, "TRA_BOI_HOAN");
            sidebarPanel.setActiveRoute("Xử lý trả & bồi hoàn");
        } else if ("Hồ sơ độc giả & Thẻ".equalsIgnoreCase(routeName)) {
            cardLayout.show(centerContainer, "QUAN_LY_DOC_GIA");
            sidebarPanel.setActiveRoute("Hồ sơ độc giả & Thẻ");
        } else if ("Tài khoản & Phân quyền (RBAC)".equalsIgnoreCase(routeName) || "Tài khoản & Phân quyền".equalsIgnoreCase(routeName)) {
            cardLayout.show(centerContainer, "QUAN_LY_TAI_KHOAN");
            sidebarPanel.setActiveRoute("Tài khoản & Phân quyền (RBAC)");
        } else if ("Phiếu mượn của tôi".equalsIgnoreCase(routeName)) {
            cardLayout.show(centerContainer, "PHIEU_MUON_CUA_TOI");
            sidebarPanel.setActiveRoute("Phiếu mượn của tôi");
        } else if ("Thống kê thư viện".equalsIgnoreCase(routeName)) {
            cardLayout.show(centerContainer, "BAO_CAO");
            sidebarPanel.setActiveRoute("Thống kê thư viện");
        } else if ("Cài đặt quy định (SRS)".equalsIgnoreCase(routeName)) {
            cardLayout.show(centerContainer, "CAI_DAT");
            sidebarPanel.setActiveRoute("Cài đặt quy định (SRS)");
        } else if ("Góp ý & Phản hồi".equalsIgnoreCase(routeName) || "Hộp thư góp ý".equalsIgnoreCase(routeName)) {
            cardLayout.show(centerContainer, "GOP_Y");
            sidebarPanel.setActiveRoute(routeName);
        } else if ("Hướng dẫn (SRS)".equalsIgnoreCase(routeName)) {
            com.ximilib.presentation.component.HelpGuideModalPanel.show(this);
        } else {
            UiModalHelper.showToastInfo(this, "Chức năng [" + routeName + "] đang được kết nối!");
        }
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // 1. CỘT ĐIỀU HƯỚNG BÊN TRÁI (Header logo + Menu)
        sidebarPanel = new SidebarPanel(currentUser.getRole());

        // 2. PHẦN BÊN PHẢI: Gồm TopBar ở trên và Nội dung chính ở dưới
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(createTopBar(), BorderLayout.NORTH);

        // 3. NỘI DUNG CHÍNH (CardLayout)
        cardLayout = new CardLayout();
        centerContainer = new JPanel(cardLayout);
        centerContainer.setBackground(UITheme.BG_LIGHT);

        // Card 1: Màn hình Trang chủ
        JPanel homeContentPanel = createHomeDashboardPanel();
        JScrollPane homeScrollPane = new JScrollPane(homeContentPanel);
        homeScrollPane.setBorder(null);
        homeScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        homeScrollPane.setBackground(UITheme.BG_LIGHT);
        centerContainer.add(homeScrollPane, "HOME");

        // Card 2: Màn hình Kho sách
        bookCatalogView = new BookCatalogView(AppContainer.getInstance().getBookCatalogService(), currentUser);
        centerContainer.add(bookCatalogView, "KHO_SACH");

        // Card 2b: Đăng ký mượn sách dành cho độc giả (SRS UC-5)
        readerBorrowView = new ReaderBorrowRegistrationView(
            AppContainer.getInstance().getCirculationService(),
            AppContainer.getInstance().getBookCatalogService(),
            currentUser,
            () -> navigateTo("Phiếu mượn của tôi")
        );
        centerContainer.add(readerBorrowView, "DANG_KY_MUON");

        // Card 3: Quản lý thể loại
        centerContainer.add(new CategoryManagementView(AppContainer.getInstance().getBookCatalogService()), "THE_LOAI");

        // Card 4: Thùng rác & khôi phục
        centerContainer.add(new TrashAndRestoreView(AppContainer.getInstance().getBookCatalogService()), "THUNG_RAC");

        // Card 5: Bàn lưu hành quầy thủ thư
        centerContainer.add(new CirculationDeskView(AppContainer.getInstance().getCirculationService()), "BAN_LUU_HANH");

        // Card 6: Xử lý trả & bồi hoàn
        centerContainer.add(new ReturnAndCompensationView(AppContainer.getInstance().getCirculationService()), "TRA_BOI_HOAN");

        // Card 7: Quản lý độc giả & thẻ
        centerContainer.add(new PatronManagementView(AppContainer.getInstance().getAuthenticationService()), "QUAN_LY_DOC_GIA");

        // Card 7b: Quản lý tài khoản & Phân quyền RBAC (SRS YC-10)
        centerContainer.add(new UserRoleManagementView(AppContainer.getInstance().getAuthenticationService()), "QUAN_LY_TAI_KHOAN");

        // Card 8: Phiếu mượn của tôi (dành cho độc giả)
        centerContainer.add(new ReaderMyLoansView(AppContainer.getInstance().getCirculationService(), currentUser.getId()), "PHIEU_MUON_CUA_TOI");

        // Card 9: Báo cáo & Thống kê thư viện (SRS UC-7, F-12)
        centerContainer.add(new ReportsDashboardView(AppContainer.getInstance().getConnectionManager()), "BAO_CAO");

        // Card 10: Cài đặt quy định thư viện (SRS)
        centerContainer.add(new SystemSettingsView(AppContainer.getInstance().getConnectionManager()), "CAI_DAT");

        // Card 11: Hộp thư góp ý & phản hồi (F-10)
        centerContainer.add(new FeedbackManagementView(AppContainer.getInstance().getConnectionManager(),
            currentUser.isReader()), "GOP_Y");

        rightPanel.add(centerContainer, BorderLayout.CENTER);
        add(sidebarPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        // Đăng ký bộ điều hướng từ Sidebar
        sidebarPanel.setNavigationListener(this::navigateTo);
    }

    private JPanel createHomeDashboardPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(UITheme.BG_LIGHT);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(18, 28, 28, 28));

        // Khối 1: Banner xanh ngọc (Hero Banner)
        HeroBannerPanel heroBanner = new HeroBannerPanel();
        heroBanner.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(heroBanner);
        contentPanel.add(Box.createVerticalStrut(18));

        // Khối 2: Các thẻ KPI thống kê (Phân quyền theo vai trò)
        boolean isReader = currentUser.isReader();
        int kpiCols = isReader ? 3 : 4;
        JPanel kpiRow = new JPanel(new GridLayout(1, kpiCols, 16, 0));
        kpiRow.setOpaque(false);
        kpiRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        kpiRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));

        if (isReader) {
            kpiRow.add(new KpiCard(KpiCard.CardType.TOTAL_BOOKS, "12,847", "Tổng số sách thư viện", "Tra cứu"));
            kpiRow.add(new KpiCard(KpiCard.CardType.ACTIVE_LOANS, "2", "Sách bạn đang mượn", "Đang giữ"));
            kpiRow.add(new KpiCard(KpiCard.CardType.OVERDUE, "0", "Phiếu quá hạn", "Tốt"));
        } else {
            kpiRow.add(new KpiCard(KpiCard.CardType.TOTAL_BOOKS, "12,847", "Tổng số sách", "▲ +12%"));
            kpiRow.add(new KpiCard(KpiCard.CardType.ACTIVE_LOANS, "1,234", "Đang cho mượn", null));
            kpiRow.add(new KpiCard(KpiCard.CardType.MEMBERS, "3,891", "Bạn đọc thành viên", "▲ +8%"));
            kpiRow.add(new KpiCard(KpiCard.CardType.OVERDUE, "47", "Quá hạn cần thu hồi", "Khẩn cấp"));
        }

        contentPanel.add(kpiRow);
        contentPanel.add(Box.createVerticalStrut(24));

        // Khối 3: Sách Đang Mượn (My Active Loans)
        contentPanel.add(createSectionHeader("Sách Đang Mượn", "3", "Xem tất cả >"));
        contentPanel.add(Box.createVerticalStrut(12));

        JPanel activeLoansRow = new JPanel(new GridLayout(1, 3, 16, 0));
        activeLoansRow.setOpaque(false);
        activeLoansRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        activeLoansRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        activeLoansRow.add(new ActiveLoanCard(
            "Atomic Habits", "James Clear", "Phan Vũ Xuân Kiên",
            "Còn 14 ngày | Hạn 12/04", ActiveLoanCard.StatusType.NORMAL, true
        ));
        activeLoansRow.add(new ActiveLoanCard(
            "Hoàng Tử Bé", "A. Saint-Exupéry", "Lê Võ Thành Nam",
            "Còn 2 ngày | Hạn 30/03", ActiveLoanCard.StatusType.WARNING, true
        ));
        activeLoansRow.add(new ActiveLoanCard(
            "Deep Work", "Cal Newport", "Nguyễn Việt Nhật",
            "Quá hạn 4 ngày | Hạn 24/03", ActiveLoanCard.StatusType.OVERDUE, false
        ));

        contentPanel.add(activeLoansRow);
        contentPanel.add(Box.createVerticalStrut(24));

        // Khối 4: Sách Mới Nhập (New Arrivals)
        contentPanel.add(createSectionHeader("Sách Mới Nhập", "Mới", "Xem tất cả >"));
        contentPanel.add(Box.createVerticalStrut(12));

        JPanel arrivalsRow = new JPanel(new GridLayout(1, 5, 14, 0));
        arrivalsRow.setOpaque(false);
        arrivalsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        arrivalsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 225));

        arrivalsRow.add(new BookArrivalCard("Atomic Habits", "James Clear", new Color(245, 158, 11), true));
        arrivalsRow.add(new BookArrivalCard("Thinking, Fast & Slow", "Daniel Kahneman", new Color(16, 185, 129), true));
        arrivalsRow.add(new BookArrivalCard("The Great Gatsby", "F. Scott Fitzgerald", new Color(30, 58, 138), false));
        arrivalsRow.add(new BookArrivalCard("Nhà Giả Kim", "Paulo Coelho", new Color(220, 38, 38), true));
        arrivalsRow.add(new BookArrivalCard("Clean Architecture", "Robert C. Martin", new Color(79, 70, 229), true));

        contentPanel.add(arrivalsRow);
        return contentPanel;
    }

    /**
     * Tạo thanh Top Bar chuẩn phong cách LibRack
     */
    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(16, 0));
        topBar.setBackground(UITheme.CARD_BG);
        topBar.setPreferredSize(new Dimension(1100, 68));
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
            BorderFactory.createEmptyBorder(12, 40, 12, 28)
        ));

        // Bên trái: Avatar + Lời chào
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        userPanel.setOpaque(false);
        userPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        userPanel.setToolTipText("Bấm để xem thông tin tài khoản, đổi mật khẩu và đăng xuất");

        // Avatar tròn mô phỏng
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PASTEL);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.ON_PASTEL);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                String name = currentUser.getFullName();
                String initial = UITheme.getAvatarInitial(name);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initial, (getWidth() - fm.stringWidth(initial)) / 2, 26);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(38, 38));
        avatar.setOpaque(false);

        JPanel greetingBox = new JPanel();
        greetingBox.setOpaque(false);
        greetingBox.setLayout(new BoxLayout(greetingBox, BoxLayout.Y_AXIS));

        JLabel lblGreeting = new JLabel("Xin chào!");
        lblGreeting.setFont(UITheme.FONT_SMALL);
        lblGreeting.setForeground(UITheme.TEXT_MUTED);

        JLabel lblName = new JLabel(currentUser.getFullName());
        lblName.setFont(UITheme.FONT_BOLD);
        lblName.setForeground(UITheme.TEXT_MAIN);

        greetingBox.add(lblGreeting);
        greetingBox.add(lblName);

        userPanel.add(avatar);
        userPanel.add(greetingBox);

        // Bắt sự kiện chuột trên toàn bộ khu vực avatar, lời chào và tên người dùng
        MouseAdapter openProfileListener = new MouseAdapter() {
            private long lastClick = 0;
            @Override
            public void mousePressed(MouseEvent e) {
                long now = System.currentTimeMillis();
                if (now - lastClick > 400) {
                    lastClick = now;
                    openProfileDialog();
                }
            }
        };
        userPanel.addMouseListener(openProfileListener);
        avatar.addMouseListener(openProfileListener);
        avatar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        greetingBox.addMouseListener(openProfileListener);
        greetingBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblGreeting.addMouseListener(openProfileListener);
        lblGreeting.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblName.addMouseListener(openProfileListener);
        lblName.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Ở giữa: Thanh tìm kiếm toàn cục chuẩn phong cách hiện đại
        JPanel searchBox = new JPanel(new BorderLayout(8, 0));
        searchBox.setOpaque(false);
        searchBox.setPreferredSize(new Dimension(420, 38));

        SearchField txtSearch = new SearchField("Tìm kiếm sách, tác giả, thể loại, độc giả...");
        txtSearch.addActionListener(e -> {
            String query = txtSearch.getText().trim();
            if (!query.isEmpty()) {
                navigateTo("Kho sách");
            }
        });

        searchBox.add(txtSearch, BorderLayout.CENTER);

        // Bên phải: Nút thêm sách / Thẻ số + Chuông thông báo
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        actionsPanel.setOpaque(false);

        // Nút thêm sách mới (Header)
        JButton btnAddBook = UITheme.createPillButton("+ Thêm Sách", UITheme.PRIMARY, Color.WHITE);
        btnAddBook.setFont(UITheme.FONT_SMALL);
        btnAddBook.addActionListener(e -> {
            BookFormModalPanel.show(this, null, () -> navigateTo("Kho sách"));
        });

        // Chuông thông báo có badge đỏ
        JPanel bellPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_LIGHT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                IconFactory.drawBell(g2, 10, 10, 18, UITheme.TEXT_MAIN);
                g2.setColor(UITheme.DANGER_TEXT);
                g2.fillOval(22, 4, 12, 12);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 8));
                g2.drawString("1", 26, 13);
                g2.dispose();
            }
        };
        bellPanel.setPreferredSize(new Dimension(38, 38));
        bellPanel.setOpaque(false);
        bellPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bellPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                UiModalHelper.showQuickPopup(new NotificationPopupPanel());
            }
        });

        boolean isReader = currentUser.isReader();
        if (!isReader) {
            actionsPanel.add(btnAddBook);
        } else {
            JButton btnDigitalCard = UITheme.createPillButton("Thẻ Số", UITheme.TINT, UITheme.ON_TINT);
            btnDigitalCard.setFont(UITheme.FONT_SMALL);
            btnDigitalCard.addActionListener(e -> {
                DigitalCardDialog.show(this, currentUser.getId(), currentUser.getFullName());
            });
            actionsPanel.add(btnDigitalCard);
        }
        actionsPanel.add(bellPanel);

        topBar.add(userPanel, BorderLayout.WEST);
        topBar.add(searchBox, BorderLayout.CENTER);
        topBar.add(actionsPanel, BorderLayout.EAST);

        return topBar;
    }

    private JPanel createSectionHeader(String title, String countBadge, String actionText) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);
        left.add(lblTitle);

        if (countBadge != null && !countBadge.isEmpty()) {
            JLabel lblBadge = new JLabel(countBadge) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(UITheme.CARD_BORDER);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            lblBadge.setFont(UITheme.FONT_BADGE);
            lblBadge.setForeground(UITheme.TEXT_MUTED);
            lblBadge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
            left.add(lblBadge);
        }

        JLabel lblAction = new JLabel(actionText);
        lblAction.setFont(UITheme.FONT_SMALL);
        lblAction.setForeground(UITheme.PRIMARY);
        lblAction.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblAction.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                navigateTo("Kho sách");
            }
        });

        header.add(left, BorderLayout.WEST);
        header.add(lblAction, BorderLayout.EAST);

        return header;
    }

    private void openProfileDialog() {
        UserProfileModalPanel.show(this, currentUser);
    }

    public void openBorrowRegistration(com.ximilib.application.dto.BookSummaryDto book) {
        if (readerBorrowView != null && book != null) {
            readerBorrowView.selectBook(book);
        }
        navigateTo("Đăng ký mượn sách");
    }
}
