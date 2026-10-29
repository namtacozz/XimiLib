package com.ximilib.gui;

import com.ximilib.presentation.component.DigitalCardDialog;
import com.ximilib.presentation.component.SearchField;
import com.ximilib.presentation.component.SidebarPanel;
import com.ximilib.presentation.util.IconFactory;
import com.ximilib.presentation.util.UITheme;
import com.ximilib.gui.views.*;
import com.ximilib.gui.views.home.ReaderHomePanel;
import com.ximilib.gui.views.home.StaffHomePanel;
import com.ximilib.infrastructure.config.AppContainer;
import com.ximilib.domain.model.User;
import com.ximilib.presentation.component.HelpGuideModalPanel;
import com.ximilib.presentation.component.NotificationPopupPanel;
import com.ximilib.presentation.component.TabbedViewPanel;
import com.ximilib.presentation.component.UserProfileModalPanel;
import com.ximilib.presentation.util.UiModalHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * MainDashboardFrame: Khung giao diện chính của hệ thống thư viện XimiLib.
 * - Điều hướng Sidebar tinh gọn (tối đa 6-7 mục), không thanh cuộn tại 768px.
 * - Gộp các tiểu màn hình theo TabBar (Kho sách, Mượn – Trả, Quản trị, Sách của tôi).
 * - TopBar hiện đại: Avatar cá nhân, Tìm kiếm toàn cục, Nút thao tác theo vai trò, Chuông thông báo động.
 * - Trang chủ phân quyền (StaffHomePanel cho Thủ thư/Admin, ReaderHomePanel cho Độc giả).
 */
public class MainDashboardFrame extends JFrame {

    private static MainDashboardFrame instance;

    private final User currentUser;
    private SidebarPanel sidebarPanel;
    private JPanel centerContainer;
    private CardLayout cardLayout;

    private BookCatalogView bookCatalogView;
    private ReaderBorrowRegistrationView readerBorrowView;
    private TabbedViewPanel readerTabbedView;
    private TabbedViewPanel bookTabbedView;
    private TabbedViewPanel circulationTabbedView;
    private TabbedViewPanel adminTabbedView;

    private int unreadNotifications = 0;
    private JPanel bellPanel;

    public static MainDashboardFrame getInstance() {
        return instance;
    }

    public MainDashboardFrame(User user) {
        this(user, "Trang chủ");
    }

    public MainDashboardFrame(User user, String initialRoute) {
        instance = this;
        this.currentUser = (user != null) ? user : new User(
            "ND002", "kien.phan", "123456",
            "Phan Vũ Xuân Kiên", "kien.phan@dut.udn.vn", "0905 123 456",
            "Thủ Thư"
        );

        setTitle("XimiLib – Quản lý Thư viện");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1260, 850);
        setMinimumSize(new Dimension(1050, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.PAGE_BG);

        UiModalHelper.initMainFrame(this);
        com.ximilib.presentation.util.AppLogo.applyWindowIcons(this);

        initUI();
        loadUnreadNotificationsAsync();

        if (initialRoute != null) {
            navigateTo(initialRoute);
        }
    }

    public MainDashboardFrame(String username, String userRole) {
        this(new User("ND002", username, "123456", username, username + "@dut.udn.vn", "0905 123 456", userRole), "Trang chủ");
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
        String r = routeName.trim();

        if ("Trang chủ".equalsIgnoreCase(r) || "HOME".equalsIgnoreCase(r)) {
            cardLayout.show(centerContainer, "HOME");
            sidebarPanel.setActiveRoute("Trang chủ");
        } else if ("Kho sách".equalsIgnoreCase(r) || "Khám phá sách".equalsIgnoreCase(r) || "CATALOG".equalsIgnoreCase(r)) {
            cardLayout.show(centerContainer, "KHO_SACH");
            sidebarPanel.setActiveRoute(currentUser.isReader() ? "Khám phá sách" : "Kho sách");
        } else if ("Quản lý thể loại".equalsIgnoreCase(r)) {
            if (bookTabbedView != null) bookTabbedView.selectTab(1);
            cardLayout.show(centerContainer, "KHO_SACH");
            sidebarPanel.setActiveRoute("Kho sách");
        } else if ("Thùng rác & Khôi phục".equalsIgnoreCase(r)) {
            if (bookTabbedView != null) bookTabbedView.selectTab(2);
            cardLayout.show(centerContainer, "KHO_SACH");
            sidebarPanel.setActiveRoute("Kho sách");
        } else if ("Mượn – Trả".equalsIgnoreCase(r) || "Bàn lưu hành (Desk)".equalsIgnoreCase(r) || "DESK".equalsIgnoreCase(r)) {
            if (circulationTabbedView != null) circulationTabbedView.selectTab(0);
            cardLayout.show(centerContainer, "MUON_TRA");
            sidebarPanel.setActiveRoute("Mượn – Trả");
        } else if ("Xử lý trả & bồi hoàn".equalsIgnoreCase(r)) {
            if (circulationTabbedView != null) circulationTabbedView.selectTab(1);
            cardLayout.show(centerContainer, "MUON_TRA");
            sidebarPanel.setActiveRoute("Mượn – Trả");
        } else if ("Hồ sơ độc giả & Thẻ".equalsIgnoreCase(r) || "READERS".equalsIgnoreCase(r)) {
            cardLayout.show(centerContainer, "QUAN_LY_DOC_GIA");
            sidebarPanel.setActiveRoute("Hồ sơ độc giả & Thẻ");
        } else if ("Thống kê thư viện".equalsIgnoreCase(r)) {
            cardLayout.show(centerContainer, "BAO_CAO");
            sidebarPanel.setActiveRoute("Thống kê thư viện");
        } else if ("Quản trị hệ thống".equalsIgnoreCase(r) || "Tài khoản & Phân quyền (RBAC)".equalsIgnoreCase(r)) {
            if (adminTabbedView != null) adminTabbedView.selectTab(0);
            cardLayout.show(centerContainer, "QUAN_TRI");
            sidebarPanel.setActiveRoute("Quản trị hệ thống");
        } else if ("Cài đặt quy định (SRS)".equalsIgnoreCase(r)) {
            if (adminTabbedView != null) adminTabbedView.selectTab(1);
            cardLayout.show(centerContainer, "QUAN_TRI");
            sidebarPanel.setActiveRoute("Quản trị hệ thống");
        } else if ("Sách của tôi".equalsIgnoreCase(r) || "Phiếu mượn của tôi".equalsIgnoreCase(r) || "MY_LOANS".equalsIgnoreCase(r) || "MY_FINES".equalsIgnoreCase(r)) {
            if (readerTabbedView != null) readerTabbedView.selectTab(0);
            cardLayout.show(centerContainer, "SACH_CUA_TOI");
            sidebarPanel.setActiveRoute("Sách của tôi");
        } else if ("Đăng ký mượn sách".equalsIgnoreCase(r)) {
            if (readerTabbedView != null) readerTabbedView.selectTab(1);
            cardLayout.show(centerContainer, "SACH_CUA_TOI");
            sidebarPanel.setActiveRoute("Sách của tôi");
        } else if ("Hộp thư góp ý".equalsIgnoreCase(r) || "Góp ý & Phản hồi".equalsIgnoreCase(r)) {
            cardLayout.show(centerContainer, "GOP_Y");
            sidebarPanel.setActiveRoute("Hộp thư góp ý");
        } else if ("Trợ giúp".equalsIgnoreCase(r) || "Hướng dẫn (SRS)".equalsIgnoreCase(r)) {
            HelpGuideModalPanel.show(this);
        } else {
            UiModalHelper.showToastInfo(this, "Mục [" + routeName + "] đang được kết nối!");
        }
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // 1. Sidebar điều hướng
        sidebarPanel = new SidebarPanel(currentUser.getRole());
        sidebarPanel.setNavigationListener(this::navigateTo);

        // 2. Khu vực bên phải
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(createTopBar(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        centerContainer = new JPanel(cardLayout);
        centerContainer.setBackground(UITheme.PAGE_BG);

        boolean isReader = currentUser.isReader();
        String normRole = currentUser.getRole().toUpperCase();
        boolean isAdmin = normRole.contains("ADMIN") || normRole.contains("QUẢN TRỊ");

        // Card 1: HOME
        if (isReader) {
            ReaderHomePanel readerHome = new ReaderHomePanel(
                AppContainer.getInstance().getDashboardService(),
                AppContainer.getInstance().getCirculationService(),
                AppContainer.getInstance().getBookCatalogService(),
                this::navigateTo
            );
            centerContainer.add(readerHome, "HOME");
        } else {
            StaffHomePanel staffHome = new StaffHomePanel(
                AppContainer.getInstance().getDashboardService(),
                AppContainer.getInstance().getBookCatalogService(),
                currentUser,
                this::navigateTo
            );
            centerContainer.add(staffHome, "HOME");
        }

        // Card 2: KHO_SACH (Gộp Đầu sách, Thể loại, Thùng rác)
        bookCatalogView = new BookCatalogView(AppContainer.getInstance().getBookCatalogService(), currentUser);
        if (isReader) {
            centerContainer.add(bookCatalogView, "KHO_SACH");
        } else {
            CategoryManagementView catView = new CategoryManagementView(AppContainer.getInstance().getBookCatalogService());
            TrashAndRestoreView trashView = new TrashAndRestoreView(AppContainer.getInstance().getBookCatalogService());
            bookTabbedView = new TabbedViewPanel(
                List.of("Đầu sách", "Quản lý thể loại", "Thùng rác & Khôi phục"),
                List.of(bookCatalogView, catView, trashView)
            );
            centerContainer.add(bookTabbedView, "KHO_SACH");
        }

        // Card 3: MUON_TRA (Gộp Bàn lưu hành và Xử lý trả / bồi hoàn)
        if (!isReader) {
            CirculationDeskView deskView = new CirculationDeskView(AppContainer.getInstance().getCirculationService());
            ReturnAndCompensationView returnView = new ReturnAndCompensationView(AppContainer.getInstance().getCirculationService());
            circulationTabbedView = new TabbedViewPanel(
                List.of("Bàn lưu hành (Desk)", "Xử lý trả & Bồi hoàn"),
                List.of(deskView, returnView)
            );
            centerContainer.add(circulationTabbedView, "MUON_TRA");

            // Card 4: Hồ sơ độc giả & Thẻ
            centerContainer.add(new PatronManagementView(AppContainer.getInstance().getAuthenticationService()), "QUAN_LY_DOC_GIA");

            // Card 5: Báo cáo & Thống kê
            centerContainer.add(new ReportsDashboardView(AppContainer.getInstance().getConnectionManager()), "BAO_CAO");
        }

        // Card 6: QUAN_TRI (Gộp Quản lý tài khoản RBAC và Cài đặt quy định SRS)
        if (isAdmin) {
            UserRoleManagementView roleView = new UserRoleManagementView(AppContainer.getInstance().getAuthenticationService());
            SystemSettingsView settingsView = new SystemSettingsView(AppContainer.getInstance().getConnectionManager());
            adminTabbedView = new TabbedViewPanel(
                List.of("Tài khoản & Phân quyền (RBAC)", "Cài đặt quy định (SRS)"),
                List.of(roleView, settingsView)
            );
            centerContainer.add(adminTabbedView, "QUAN_TRI");
        }

        // Card 7: SACH_CUA_TOI (Dành riêng cho độc giả)
        if (isReader) {
            readerBorrowView = new ReaderBorrowRegistrationView(
                AppContainer.getInstance().getCirculationService(),
                AppContainer.getInstance().getBookCatalogService(),
                currentUser,
                () -> navigateTo("Phiếu mượn của tôi")
            );
            ReaderMyLoansView myLoansView = new ReaderMyLoansView(AppContainer.getInstance().getCirculationService(), currentUser.getId());
            readerTabbedView = new TabbedViewPanel(
                List.of("Phiếu mượn đang giữ", "Đăng ký mượn mới"),
                List.of(myLoansView, readerBorrowView)
            );
            centerContainer.add(readerTabbedView, "SACH_CUA_TOI");
        }

        // Card 8: GOP_Y
        centerContainer.add(new FeedbackManagementView(AppContainer.getInstance().getConnectionManager(), isReader), "GOP_Y");

        rightPanel.add(centerContainer, BorderLayout.CENTER);
        add(sidebarPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(16, 0));
        topBar.setBackground(UITheme.CARD_BG);
        topBar.setPreferredSize(new Dimension(1100, 68));
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
            BorderFactory.createEmptyBorder(12, 28, 12, 28)
        ));

        // 1. Bên trái: Avatar + Tên người dùng + Vai trò
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        userPanel.setOpaque(false);
        userPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        userPanel.setToolTipText("Bấm để xem thông tin tài khoản, đổi mật khẩu và đăng xuất");

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
                g2.drawString(initial, (getWidth() - fm.stringWidth(initial)) / 2, 25);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(38, 38));
        avatar.setOpaque(false);

        JPanel greetingBox = new JPanel();
        greetingBox.setOpaque(false);
        greetingBox.setLayout(new BoxLayout(greetingBox, BoxLayout.Y_AXIS));

        JLabel lblName = new JLabel(currentUser.getFullName());
        lblName.setFont(UITheme.FONT_REGULAR.deriveFont(Font.BOLD, 13f));
        lblName.setForeground(UITheme.TEXT_MAIN);

        JLabel lblRole = new JLabel(currentUser.getRole());
        lblRole.setFont(UITheme.FONT_REGULAR.deriveFont(11f));
        lblRole.setForeground(UITheme.TEXT_MUTED);

        greetingBox.add(lblName);
        greetingBox.add(lblRole);

        userPanel.add(avatar);
        userPanel.add(greetingBox);

        MouseAdapter openProfileListener = new MouseAdapter() {
            private long lastClick = 0;
            @Override
            public void mousePressed(MouseEvent e) {
                long now = System.currentTimeMillis();
                if (now - lastClick > 400) {
                    lastClick = now;
                    UserProfileModalPanel.show(MainDashboardFrame.this, currentUser);
                }
            }
        };
        userPanel.addMouseListener(openProfileListener);

        // 2. Ở giữa: Thanh tìm kiếm toàn cục
        JPanel searchBox = new JPanel(new BorderLayout(8, 0));
        searchBox.setOpaque(false);
        searchBox.setPreferredSize(new Dimension(420, 38));

        SearchField txtSearch = new SearchField("Tìm kiếm sách, tác giả, thể loại...");
        txtSearch.addActionListener(e -> {
            String query = txtSearch.getText().trim();
            if (!query.isEmpty()) {
                if (bookCatalogView != null) {
                    bookCatalogView.setSearchKeyword(query);
                }
                navigateTo(currentUser.isReader() ? "Khám phá sách" : "Kho sách");
            }
        });
        searchBox.add(txtSearch, BorderLayout.CENTER);

        // 3. Bên phải: Nút hành động chính + Chuông thông báo
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        actionsPanel.setOpaque(false);

        boolean isReader = currentUser.isReader();
        if (!isReader) {
            JButton btnCreateLoan = UITheme.createPillButton("+ Lập phiếu mượn", UITheme.PRIMARY, Color.WHITE);
            btnCreateLoan.setFont(UITheme.FONT_REGULAR.deriveFont(Font.BOLD, 12f));
            btnCreateLoan.addActionListener(e -> navigateTo("Mượn – Trả"));
            actionsPanel.add(btnCreateLoan);
        } else {
            JButton btnDigitalCard = UITheme.createPillButton("Thẻ Thư Viện", UITheme.TINT, UITheme.ON_TINT);
            btnDigitalCard.setFont(UITheme.FONT_REGULAR.deriveFont(Font.BOLD, 12f));
            btnDigitalCard.addActionListener(e -> {
                DigitalCardDialog.show(this, currentUser.getId(), currentUser.getFullName());
            });
            actionsPanel.add(btnDigitalCard);
        }

        // Chuông thông báo
        bellPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PAGE_BG);
                g2.fillOval(0, 0, getWidth(), getHeight());
                IconFactory.drawBell(g2, 10, 10, 18, UITheme.TEXT_MAIN);

                if (unreadNotifications > 0) {
                    g2.setColor(UITheme.DANGER_TEXT);
                    g2.fillOval(22, 4, 14, 14);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 8));
                    String badge = (unreadNotifications > 9) ? "9+" : String.valueOf(unreadNotifications);
                    FontMetrics fm = g2.getFontMetrics();
                    int bx = 22 + (14 - fm.stringWidth(badge)) / 2;
                    int by = 4 + (14 + fm.getAscent() - fm.getDescent()) / 2 - 1;
                    g2.drawString(badge, bx, by);
                }
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
                unreadNotifications = 0;
                bellPanel.repaint();
            }
        });

        actionsPanel.add(bellPanel);

        topBar.add(userPanel, BorderLayout.WEST);
        topBar.add(searchBox, BorderLayout.CENTER);
        topBar.add(actionsPanel, BorderLayout.EAST);

        return topBar;
    }

    private void loadUnreadNotificationsAsync() {
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                try {
                    return AppContainer.getInstance().getDashboardService().getUnreadNotificationCount(currentUser.getId());
                } catch (Exception e) {
                    return 0;
                }
            }

            @Override
            protected void done() {
                try {
                    unreadNotifications = get();
                    if (bellPanel != null) {
                        SwingUtilities.invokeLater(() -> bellPanel.repaint());
                    }
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    public void openBorrowRegistration(com.ximilib.application.dto.BookSummaryDto book) {
        if (readerBorrowView != null && book != null) {
            readerBorrowView.selectBook(book);
        }
        navigateTo("Đăng ký mượn sách");
    }
}
