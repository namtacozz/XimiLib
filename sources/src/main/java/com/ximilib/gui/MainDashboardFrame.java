package com.ximilib.gui;

import com.ximilib.gui.components.*;
import com.ximilib.gui.util.IconFactory;
import com.ximilib.gui.util.UITheme;
import com.ximilib.models.User;
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

    private final User currentUser;

    public MainDashboardFrame(User user) {
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

        initUI();
    }

    public MainDashboardFrame(String username, String userRole) {
        this(new User("ND002", username, "123456", userRole, username + "@dut.udn.vn", "0905 123 456", userRole));
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

    private void initUI() {
        setLayout(new BorderLayout());

        // 1. THANH ĐIỀU HƯỚNG BÊN TRÁI (Sidebar phong cách LibMan nhưng giữ chuẩn màu LibRack)
        add(new SidebarPanel(currentUser.getRole()), BorderLayout.WEST);

        // 2. THANH TOP BAR (Header tìm kiếm + Thao tác + Tài khoản)
        add(createTopBar(), BorderLayout.NORTH);

        // 3. NỘI DUNG CHÍNH (Cuộn được)
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
        boolean isReader = currentUser.getRole().contains("Độc Giả") || currentUser.getRole().contains("Bạn Đọc");
        int kpiCols = isReader ? 3 : 4;
        JPanel kpiRow = new JPanel(new GridLayout(1, kpiCols, 16, 0));
        kpiRow.setOpaque(false);
        kpiRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        kpiRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));

        if (isReader) {
            // Thẻ KPI dành riêng cho Bạn Đọc (Ảnh 3)
            kpiRow.add(new KpiCard(KpiCard.CardType.TOTAL_BOOKS, "12,847", "Tổng số sách thư viện", "Tra cứu"));
            kpiRow.add(new KpiCard(KpiCard.CardType.ACTIVE_LOANS, "2", "Sách bạn đang mượn", "Đang giữ"));
            kpiRow.add(new KpiCard(KpiCard.CardType.OVERDUE, "0", "Phiếu quá hạn", "Tốt"));
        } else {
            // Thẻ KPI dành cho Quản Trị & Thủ Thư (Ảnh 1 & 2)
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

        // Khối 4: Sách Mới Nhập (New Arrivals) - Đã bỏ mục Tin tức & Sự kiện theo yêu cầu
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

        // Đặt nội dung vào JScrollPane mượt mà
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(UITheme.BG_LIGHT);

        add(scrollPane, BorderLayout.CENTER);
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
            BorderFactory.createEmptyBorder(12, 28, 12, 28)
        ));

        // Bên trái: Avatar + Lời chào (Nhấp để mở Thông Tin Tài Khoản)
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
                g2.setColor(UITheme.PRIMARY_MINT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.PRIMARY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                String name = currentUser.getFullName();
                String initial = name.isEmpty() ? "U" : String.valueOf(name.charAt(name.lastIndexOf(" ") + 1));
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

        JLabel lblGreeting = new JLabel("Xin chào buổi sáng!");
        lblGreeting.setFont(UITheme.FONT_SMALL);
        lblGreeting.setForeground(UITheme.TEXT_MUTED);

        JLabel lblName = new JLabel(currentUser.getFullName() + " (Tùy chỉnh)");
        lblName.setFont(UITheme.FONT_BOLD);
        lblName.setForeground(UITheme.TEXT_MAIN);

        greetingBox.add(lblGreeting);
        greetingBox.add(lblName);

        userPanel.add(avatar);
        userPanel.add(greetingBox);

        // Sự kiện click vào userPanel để mở bảng thông tin tài khoản
        userPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openProfileDialog();
            }
        });

        // Ở giữa: Thanh tìm kiếm toàn cục chuẩn phong cách hiện đại
        JPanel searchBox = new JPanel(new BorderLayout(8, 0));
        searchBox.setOpaque(false);
        searchBox.setPreferredSize(new Dimension(420, 38));

        SearchField txtSearch = new SearchField("Tìm kiếm sách, tác giả, thể loại, độc giả...");
        txtSearch.addActionListener(e -> {
            String query = txtSearch.getText().trim();
            if (!query.isEmpty()) {
                JOptionPane.showMessageDialog(MainDashboardFrame.this,
                    "Tìm kiếm với từ khóa: \"" + query + "\"\n(Chức năng đang kết nối với BookService tầng BLL & CSDL)",
                    "Kết Quả Tìm Kiếm", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        searchBox.add(txtSearch, BorderLayout.CENTER);

        // Bên phải: Nút thêm sách + Chuông thông báo
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        actionsPanel.setOpaque(false);

        // Nút thêm sách mới
        JButton btnAddBook = UITheme.createPillButton("+ Thêm Sách", UITheme.PRIMARY, Color.WHITE);
        btnAddBook.setFont(UITheme.FONT_SMALL);
        btnAddBook.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                "Chức năng Thêm Sách sẽ kết nối với BookService của bạn Nam (BLL) & BookDAL của bạn Nhật (DAL)!",
                "Thêm Sách Mới", JOptionPane.INFORMATION_MESSAGE);
        });

        // Chuông thông báo có badge đỏ
        JPanel bellPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Nền tròn nhẹ
                g2.setColor(UITheme.BG_LIGHT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                // Chuông vector sắc nét không phụ thuộc emoji font
                IconFactory.drawBell(g2, 10, 10, 18, UITheme.TEXT_MAIN);
                // Badge đỏ số 1
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
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(MainDashboardFrame.this,
                    "Cảnh báo: Có 1 phiếu mượn của độc giả quá hạn cần xử lý tiền phạt!",
                    "Thông Báo Hệ Thống", JOptionPane.WARNING_MESSAGE);
            }
        });

        boolean isReader = currentUser.getRole().contains("Độc Giả") || currentUser.getRole().contains("Bạn Đọc");
        if (!isReader) {
            actionsPanel.add(btnAddBook);
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
                    g2.setColor(new Color(226, 232, 240));
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

        header.add(left, BorderLayout.WEST);
        header.add(lblAction, BorderLayout.EAST);

        return header;
    }

    /**
     * Mở Dialog hiển thị thông tin tài khoản, đổi mật khẩu và nút đăng xuất
     */
    private void openProfileDialog() {
        UserProfileDialog dialog = new UserProfileDialog(
            this,
            currentUser.getFullName(),
            currentUser.getEmail(),
            currentUser.getPhone(),
            currentUser.getRole()
        );
        dialog.setVisible(true);
    }
}
