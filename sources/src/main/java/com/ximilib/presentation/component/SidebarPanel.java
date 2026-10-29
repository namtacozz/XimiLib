package com.ximilib.presentation.component;

import com.ximilib.presentation.util.IconFactory;
import com.ximilib.presentation.util.UITheme;
import com.ximilib.presentation.util.UiModalHelper;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SidebarPanel: Thanh điều hướng bên trái phân quyền động theo vai trò (Admin, Thủ thư, Độc giả).
 * - Tối đa 6-7 mục menu trực tiếp, không nhóm tiêu đề thừa, không thanh cuộn tại độ phân giải 768px.
 * - Icon vector chuẩn Graphics2D độc lập font hệ thống.
 * - Footer tinh gọn chỉ chứa nút "Trợ giúp".
 */
public class SidebarPanel extends JPanel {

    public interface NavigationListener {
        void onNavigate(String routeName);
    }

    private NavigationListener navigationListener;
    private final List<JButton> menuButtons = new ArrayList<>();
    private JButton activeButton = null;
    private final String roleName;

    public SidebarPanel() {
        this("Quản Trị");
    }

    public SidebarPanel(String roleName) {
        this.roleName = (roleName != null && !roleName.isEmpty()) ? roleName : "Quản Trị";
        setPreferredSize(new Dimension(230, 0));
        setBackground(UITheme.CARD_BG);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER));

        initUI();
    }

    private void initUI() {
        // 1. Header: Logo XimiLib + Tên trường
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 16));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        JLabel logoIcon = new JLabel(com.ximilib.presentation.util.AppLogo.getIcon(32));
        logoIcon.setPreferredSize(new Dimension(32, 32));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("XimiLib");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(UITheme.TEXT_MAIN);

        JLabel lblSub = new JLabel("Thư viện ĐHBK Đà Nẵng");
        lblSub.setFont(UITheme.FONT_BADGE);
        lblSub.setForeground(UITheme.TEXT_MUTED);

        titleBox.add(lblTitle);
        titleBox.add(lblSub);

        headerPanel.add(logoIcon);
        headerPanel.add(titleBox);

        // 2. Menu Items (Không dùng scrollbar, cách đều nhau)
        JPanel menuContainer = new JPanel();
        menuContainer.setLayout(new BoxLayout(menuContainer, BoxLayout.Y_AXIS));
        menuContainer.setOpaque(false);
        menuContainer.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        String normRole = (roleName != null) ? roleName.trim().toUpperCase() : "";
        boolean isAdmin = normRole.contains("QUẢN TRỊ") || normRole.contains("ADMIN");
        boolean isLibrarian = normRole.contains("THỦ THƯ") || normRole.contains("LIBRARIAN");

        // Menu 1: Trang chủ (mọi vai trò)
        JButton btnHome = createMenuItem("Trang chủ", IconFactory.createHomeIcon(Color.WHITE, 16), true);
        menuContainer.add(btnHome);
        activeButton = btnHome;
        menuContainer.add(Box.createVerticalStrut(6));

        if (isAdmin) {
            // ADMIN (6 mục tiếp theo = 7 mục tổng cộng)
            menuContainer.add(createMenuItem("Kho sách", IconFactory.createBookIcon(UITheme.TEXT_MUTED, 16), false));
            menuContainer.add(Box.createVerticalStrut(6));
            menuContainer.add(createMenuItem("Mượn – Trả", IconFactory.createCirculationIcon(UITheme.TEXT_MUTED, 16), false));
            menuContainer.add(Box.createVerticalStrut(6));
            menuContainer.add(createMenuItem("Hồ sơ độc giả & Thẻ", IconFactory.createUsersIcon(UITheme.TEXT_MUTED, 16), false));
            menuContainer.add(Box.createVerticalStrut(6));
            menuContainer.add(createMenuItem("Thống kê thư viện", IconFactory.createChartIcon(UITheme.TEXT_MUTED, 16), false));
            menuContainer.add(Box.createVerticalStrut(6));
            menuContainer.add(createMenuItem("Quản trị hệ thống", IconFactory.createSettingsIcon(UITheme.TEXT_MUTED, 16), false));
            menuContainer.add(Box.createVerticalStrut(6));
            menuContainer.add(createMenuItem("Hộp thư góp ý", IconFactory.createHelpIcon(UITheme.TEXT_MUTED, 16), false));
        } else if (isLibrarian) {
            // THỦ THƯ (5 mục tiếp theo = 6 mục tổng cộng)
            menuContainer.add(createMenuItem("Kho sách", IconFactory.createBookIcon(UITheme.TEXT_MUTED, 16), false));
            menuContainer.add(Box.createVerticalStrut(6));
            menuContainer.add(createMenuItem("Mượn – Trả", IconFactory.createCirculationIcon(UITheme.TEXT_MUTED, 16), false));
            menuContainer.add(Box.createVerticalStrut(6));
            menuContainer.add(createMenuItem("Hồ sơ độc giả & Thẻ", IconFactory.createUsersIcon(UITheme.TEXT_MUTED, 16), false));
            menuContainer.add(Box.createVerticalStrut(6));
            menuContainer.add(createMenuItem("Thống kê thư viện", IconFactory.createChartIcon(UITheme.TEXT_MUTED, 16), false));
            menuContainer.add(Box.createVerticalStrut(6));
            menuContainer.add(createMenuItem("Hộp thư góp ý", IconFactory.createHelpIcon(UITheme.TEXT_MUTED, 16), false));
        } else {
            // ĐỘC GIẢ (3 mục tiếp theo = 4 mục tổng cộng)
            menuContainer.add(createMenuItem("Khám phá sách", IconFactory.createBookIcon(UITheme.TEXT_MUTED, 16), false));
            menuContainer.add(Box.createVerticalStrut(6));
            menuContainer.add(createMenuItem("Sách của tôi", IconFactory.createCirculationIcon(UITheme.TEXT_MUTED, 16), false));
            menuContainer.add(Box.createVerticalStrut(6));
            menuContainer.add(createMenuItem("Hộp thư góp ý", IconFactory.createHelpIcon(UITheme.TEXT_MUTED, 16), false));
        }

        // 3. Footer Sidebar: Chỉ có nút Trợ giúp
        JPanel footerPanel = new JPanel();
        footerPanel.setOpaque(false);
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER),
            BorderFactory.createEmptyBorder(12, 12, 14, 12)
        ));

        JButton btnHelp = createMenuItem("Trợ giúp", IconFactory.createHelpIcon(UITheme.TEXT_MUTED, 16), false);
        btnHelp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        footerPanel.add(btnHelp);

        add(headerPanel, BorderLayout.NORTH);
        add(menuContainer, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JButton createMenuItem(String title, Icon icon, boolean isActive) {
        JButton btn = new JButton(title, icon) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean isCurrent = (activeButton == this);
                if (isCurrent) {
                    g2.setColor(UITheme.PRIMARY);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    setForeground(Color.WHITE);
                } else if (getModel().isRollover()) {
                    g2.setColor(UITheme.TINT_LIGHT);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    setForeground(UITheme.PRIMARY);
                } else {
                    setForeground(UITheme.TEXT_MAIN);
                }
                if (getIcon() instanceof IconFactory.DynamicIcon dynamicIcon) {
                    dynamicIcon.setColor(isCurrent ? Color.WHITE : (getModel().isRollover() ? UITheme.PRIMARY : UITheme.TEXT_MUTED));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(UITheme.FONT_REGULAR.deriveFont(Font.BOLD, 13f));
        btn.setForeground(isActive ? Color.WHITE : UITheme.TEXT_MAIN);
        btn.setIconTextGap(12);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setPreferredSize(new Dimension(206, 38));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(4, 12, 4, 12));

        btn.addActionListener(e -> {
            if ("Trợ giúp".equals(title.trim())) {
                if (navigationListener != null) {
                    navigationListener.onNavigate("Trợ giúp");
                }
                return;
            }

            for (JButton b : menuButtons) {
                b.setForeground(UITheme.TEXT_MAIN);
                if (b.getIcon() instanceof IconFactory.DynamicIcon dynamicIcon) {
                    dynamicIcon.setColor(UITheme.TEXT_MUTED);
                }
            }
            activeButton = btn;
            btn.setForeground(Color.WHITE);
            if (btn.getIcon() instanceof IconFactory.DynamicIcon dynamicIcon) {
                dynamicIcon.setColor(Color.WHITE);
            }
            repaint();

            if (navigationListener != null) {
                navigationListener.onNavigate(title.trim());
            }
        });

        menuButtons.add(btn);
        return btn;
    }

    public void setNavigationListener(NavigationListener listener) {
        this.navigationListener = listener;
    }

    public void setActiveRoute(String routeName) {
        if (routeName == null) return;
        for (JButton b : menuButtons) {
            boolean isTarget = b.getText().trim().equalsIgnoreCase(routeName.trim());
            if (isTarget) {
                activeButton = b;
                b.setForeground(Color.WHITE);
                if (b.getIcon() instanceof IconFactory.DynamicIcon dynamicIcon) {
                    dynamicIcon.setColor(Color.WHITE);
                }
            } else {
                b.setForeground(UITheme.TEXT_MAIN);
                if (b.getIcon() instanceof IconFactory.DynamicIcon dynamicIcon) {
                    dynamicIcon.setColor(UITheme.TEXT_MUTED);
                }
            }
        }
        repaint();
    }
}
