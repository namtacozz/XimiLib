package com.ximilib.gui.components;

import com.ximilib.gui.util.IconFactory;
import com.ximilib.gui.util.UITheme;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import com.ximilib.presentation.util.UiModalHelper;

/**
 * SidebarPanel: Thanh điều hướng bên trái phân quyền động theo 3 vai trò,
 * sử dụng Vector Icon chuẩn Graphics2D loại bỏ hoàn toàn lỗi ô vuông font trên Windows.
 * Tác giả: Phan Vũ Xuân Kiên (GUI / DB)
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
        setPreferredSize(new Dimension(240, 0));
        setBackground(UITheme.CARD_BG);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER));

        initUI();
    }

    private void initUI() {
        // 1. Header Sidebar: Logo XimiLib
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 16));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        // Icon vuông xanh ngọc vẽ vector chuẩn nét
        JPanel logoIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                // Vẽ cuốn sách vector màu trắng
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.8f));
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                g2.drawRoundRect(cx - 9, cy - 8, 8, 16, 2, 2);
                g2.drawRoundRect(cx + 1, cy - 8, 8, 16, 2, 2);
                g2.drawLine(cx - 7, cy - 4, cx - 3, cy - 4);
                g2.drawLine(cx - 7, cy, cx - 3, cy);
                g2.drawLine(cx + 3, cy - 4, cx + 7, cy - 4);
                g2.drawLine(cx + 3, cy, cx + 7, cy);
                g2.dispose();
            }
        };
        logoIcon.setPreferredSize(new Dimension(34, 34));
        logoIcon.setOpaque(false);

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

        // 2. Menu cuộn được (Phân quyền theo Role)
        JPanel menuContainer = new JPanel();
        menuContainer.setLayout(new BoxLayout(menuContainer, BoxLayout.Y_AXIS));
        menuContainer.setOpaque(false);
        menuContainer.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        String normRole = (roleName != null) ? roleName.trim().toUpperCase() : "";
        boolean isAdmin = normRole.contains("QUẢN TRỊ") || normRole.contains("ADMIN");
        boolean isLibrarian = normRole.contains("THỦ THƯ") || normRole.contains("LIBRARIAN");

        // Nhóm 1: MENU CHÍNH
        menuContainer.add(createCategoryLabel("MENU CHÍNH"));
        JButton btnHome = createMenuItem("Trang chủ", IconFactory.createHomeIcon(Color.WHITE, 16), true);
        menuContainer.add(btnHome);
        activeButton = btnHome;
        menuContainer.add(Box.createVerticalStrut(12));

        // Nhóm 2: KHO SÁCH
        menuContainer.add(createCategoryLabel("KHO SÁCH"));
        menuContainer.add(createMenuItem("Kho sách", IconFactory.createBookIcon(UITheme.TEXT_MUTED, 16), false));
        if (isAdmin || isLibrarian) {
            menuContainer.add(Box.createVerticalStrut(4));
            menuContainer.add(createMenuItem("Quản lý thể loại", IconFactory.createTagIcon(UITheme.TEXT_MUTED, 16), false));
        }
        menuContainer.add(Box.createVerticalStrut(12));

        // Nhóm 3: QUẢN LÝ ĐỘC GIẢ & TÀI KHOẢN (Chỉ Quản trị và Thủ thư)
        if (isAdmin || isLibrarian) {
            menuContainer.add(createCategoryLabel(isAdmin ? "QUẢN TRỊ & TÀI KHOẢN" : "QUẢN LÝ ĐỘC GIẢ"));
            menuContainer.add(createMenuItem("Hồ sơ độc giả & Thẻ", IconFactory.createUsersIcon(UITheme.TEXT_MUTED, 16), false));
            if (isAdmin) {
                menuContainer.add(Box.createVerticalStrut(4));
                menuContainer.add(createMenuItem("Tài khoản & Phân quyền (RBAC)", IconFactory.createSettingsIcon(UITheme.TEXT_MUTED, 16), false));
            }
            menuContainer.add(Box.createVerticalStrut(12));
        }

        // Nhóm 4: MƯỢN & TRẢ SÁCH
        menuContainer.add(createCategoryLabel("MƯỢN & TRẢ SÁCH"));
        if (isAdmin || isLibrarian) {
            menuContainer.add(createMenuItem("Bàn lưu hành (Desk)", IconFactory.createCirculationIcon(UITheme.TEXT_MUTED, 16), false));
            menuContainer.add(Box.createVerticalStrut(4));
            menuContainer.add(createMenuItem("Xử lý trả & bồi hoàn", IconFactory.createCheckIcon(UITheme.TEXT_MUTED, 16), false));
            menuContainer.add(Box.createVerticalStrut(4));
            menuContainer.add(createMenuItem("Lịch sử phiếu mượn", IconFactory.createHistoryIcon(UITheme.TEXT_MUTED, 16), false));
        } else {
            menuContainer.add(createMenuItem("Đăng ký mượn sách", IconFactory.createBookIcon(UITheme.TEXT_MUTED, 16), false));
            menuContainer.add(Box.createVerticalStrut(4));
            menuContainer.add(createMenuItem("Phiếu mượn của tôi", IconFactory.createHistoryIcon(UITheme.TEXT_MUTED, 16), false));
            menuContainer.add(Box.createVerticalStrut(4));
            menuContainer.add(createMenuItem("Góp ý & Phản hồi", IconFactory.createHelpIcon(UITheme.TEXT_MUTED, 16), false));
        }
        menuContainer.add(Box.createVerticalStrut(12));

        // Nhóm 5: BÁO CÁO & HỆ THỐNG
        if (isAdmin || isLibrarian) {
            menuContainer.add(createCategoryLabel("BÁO CÁO & HỆ THỐNG"));
            menuContainer.add(createMenuItem("Thống kê thư viện", IconFactory.createChartIcon(UITheme.TEXT_MUTED, 16), false));
            menuContainer.add(Box.createVerticalStrut(4));
            menuContainer.add(createMenuItem("Thùng rác & Khôi phục", IconFactory.createTrashIcon(UITheme.TEXT_MUTED, 16), false));
            menuContainer.add(Box.createVerticalStrut(4));
            menuContainer.add(createMenuItem("Hộp thư góp ý", IconFactory.createHelpIcon(UITheme.TEXT_MUTED, 16), false));

            if (isAdmin) {
                menuContainer.add(Box.createVerticalStrut(4));
                menuContainer.add(createMenuItem("Cài đặt quy định (SRS)", IconFactory.createSettingsIcon(UITheme.TEXT_MUTED, 16), false));
            }
            menuContainer.add(Box.createVerticalStrut(12));
        }

        JScrollPane scrollPane = new JScrollPane(menuContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // 3. Footer Sidebar: Hướng dẫn & Nhãn vai trò
        JPanel footerPanel = new JPanel();
        footerPanel.setOpaque(false);
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER),
            BorderFactory.createEmptyBorder(12, 14, 14, 14)
        ));

        JButton btnHelp = createMenuItem("Hướng dẫn (SRS)", IconFactory.createHelpIcon(UITheme.TEXT_MUTED, 16), false);
        btnHelp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        String displayRole = isAdmin ? "Quản Trị" : (isLibrarian ? "Thủ Thư" : "Độc Giả");
        JLabel lblRole = new JLabel("Vai trò: " + displayRole);
        lblRole.setFont(UITheme.FONT_BADGE);
        lblRole.setForeground(UITheme.ON_TINT);
        lblRole.setBorder(BorderFactory.createEmptyBorder(6, 6, 0, 0));

        footerPanel.add(btnHelp);
        footerPanel.add(Box.createVerticalStrut(4));
        footerPanel.add(lblRole);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JLabel createCategoryLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(UITheme.TEXT_MUTED);
        lbl.setBorder(BorderFactory.createEmptyBorder(4, 10, 6, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
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
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    setForeground(Color.WHITE);
                } else if (getModel().isRollover()) {
                    g2.setColor(UITheme.TINT_LIGHT);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
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

        btn.setFont(UITheme.FONT_BOLD);
        btn.setForeground(isActive ? Color.WHITE : UITheme.TEXT_MAIN);
        btn.setIconTextGap(10);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setPreferredSize(new Dimension(210, 38));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(4, 12, 4, 12));

        btn.addActionListener(e -> {
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
            } else if (!title.equals("Trang chủ")) {
                UiModalHelper.showToastInfo(this, "Mô-đun [" + title.trim() + "] sẽ kết nối với logic tầng BLL & CSDL MySQL!");
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
