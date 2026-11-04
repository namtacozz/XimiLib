package com.ximilib.presentation.view;

import com.ximilib.application.dto.UserAccountDto;
import com.ximilib.application.service.AuthenticationService;
import com.ximilib.presentation.component.SearchField;
import com.ximilib.presentation.util.IconFactory;
import com.ximilib.presentation.util.UITheme;
import com.ximilib.infrastructure.config.AppContainer;
import com.ximilib.presentation.util.UiModalHelper;
import raven.modal.component.SimpleModalBorder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserRoleManagementView: Màn hình Quản Lý Tài Khoản & Phân Quyền RBAC (SRS YC-10).
 * Dành riêng cho Quản trị viên (Admin) quản lý danh mục tài khoản nhân viên (Admin, Thủ thư)
 * và độc giả, điều chỉnh quyền truy cập hệ thống và kích hoạt/khóa tài khoản.
 * Tuân thủ R-01, R-08, R-11, SRS YC-10.
 */
public class UserRoleManagementView extends JPanel {

    private final AuthenticationService authService;
    private final UserTableModel tableModel = new UserTableModel();
    private JTable userTable;
    private SearchField txtSearch;
    private JComboBox<String> cmbRoleFilter;

    // KPI labels
    private JLabel lblTotalUsers;
    private JLabel lblTotalAdmins;
    private JLabel lblTotalLibrarians;
    private JLabel lblTotalReaders;

    public UserRoleManagementView() {
        this(AppContainer.getInstance().getAuthenticationService());
    }

    public UserRoleManagementView(AuthenticationService service) {
        this.authService = service != null ? service : AppContainer.getInstance().getAuthenticationService();

        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 28, 28, 28));

        initUI();
        loadUsersAsync();
    }

    private void initUI() {
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);
        topContainer.add(createHeaderSection());
        topContainer.add(Box.createVerticalStrut(14));
        topContainer.add(createKpiSection());
        topContainer.add(Box.createVerticalStrut(14));

        add(topContainer, BorderLayout.NORTH);
        add(createTableCard(), BorderLayout.CENTER);
    }

    private JPanel createHeaderSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel lblBreadcrumb = new JLabel("Trang chủ / Quản lý tài khoản & Phân quyền");
        lblBreadcrumb.setFont(UITheme.FONT_SMALL);
        lblBreadcrumb.setForeground(UITheme.TEXT_MUTED);

        JLabel lblTitle = new JLabel("Quản Lý Tài Khoản & Phân Quyền Hệ Thống (RBAC)");
        lblTitle.setFont(UITheme.FONT_TITLE_LARGE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);

        JLabel lblSub = new JLabel("Quản trị danh mục tài khoản Quản trị viên, Thủ thư, Độc giả và điều chỉnh quyền truy cập hệ thống");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.TEXT_MUTED);

        titleBox.add(lblBreadcrumb);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(lblSub);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton btnRefresh = new JButton(IconFactory.createRefreshIcon(UITheme.TEXT_MAIN, 16));
        btnRefresh.setPreferredSize(new Dimension(38, 38));
        btnRefresh.setBackground(UITheme.BG_LIGHT);
        btnRefresh.setToolTipText("Làm mới danh sách");
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.setBorder(new UITheme.RoundedBorder(UITheme.BORDER, 8));
        btnRefresh.addActionListener(e -> loadUsersAsync());

        JButton btnAddStaff = new JButton(IconFactory.createAddUserIcon(Color.WHITE, 16));
        btnAddStaff.setPreferredSize(new Dimension(38, 38));
        btnAddStaff.setBackground(UITheme.PRIMARY);
        btnAddStaff.setToolTipText("Cấp tài khoản nhân viên mới");
        btnAddStaff.setFocusPainted(false);
        btnAddStaff.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddStaff.setBorder(new UITheme.RoundedBorder(UITheme.PRIMARY, 8));
        btnAddStaff.addActionListener(e -> showCreateStaffModal());

        actions.add(btnRefresh);
        actions.add(btnAddStaff);

        panel.add(titleBox, BorderLayout.WEST);
        panel.add(actions, BorderLayout.EAST);
        return panel;
    }

    private JPanel createKpiSection() {
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 16, 0));
        kpiRow.setOpaque(false);
        kpiRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

        lblTotalUsers = new JLabel("0", SwingConstants.CENTER);
        lblTotalAdmins = new JLabel("0", SwingConstants.CENTER);
        lblTotalLibrarians = new JLabel("0", SwingConstants.CENTER);
        lblTotalReaders = new JLabel("0", SwingConstants.CENTER);

        kpiRow.add(createMiniKpiCard("Tổng tài khoản", lblTotalUsers, UITheme.TEXT_MAIN));
        kpiRow.add(createMiniKpiCard("Quản trị viên (Admin)", lblTotalAdmins, UITheme.PURPLE_TEXT));
        kpiRow.add(createMiniKpiCard("Thủ thư (Librarian)", lblTotalLibrarians, UITheme.PRIMARY));
        kpiRow.add(createMiniKpiCard("Độc giả (Reader)", lblTotalReaders, new Color(37, 99, 235)));

        return kpiRow;
    }

    private JPanel createMiniKpiCard(String label, JLabel valLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(UITheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BADGE);
        lbl.setForeground(UITheme.TEXT_MUTED);

        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valLabel.setForeground(accentColor);

        card.add(lbl, BorderLayout.NORTH);
        card.add(valLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(UITheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 18, 18, 18));

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftFilters.setOpaque(false);

        txtSearch = new SearchField("Tìm kiếm theo tên đăng nhập, họ tên, email, SĐT...");
        txtSearch.setPreferredSize(new Dimension(340, 36));
        txtSearch.addActionListener(e -> loadUsersAsync());

        cmbRoleFilter = new JComboBox<>(new String[]{"Tất cả vai trò", "ADMIN (Quản trị viên)", "LIBRARIAN (Thủ thư)", "READER (Độc giả)"});
        cmbRoleFilter.setFont(UITheme.FONT_REGULAR);
        cmbRoleFilter.setPreferredSize(new Dimension(190, 36));
        cmbRoleFilter.addActionListener(e -> loadUsersAsync());

        leftFilters.add(txtSearch);
        leftFilters.add(cmbRoleFilter);
        toolbar.add(leftFilters, BorderLayout.WEST);

        card.add(toolbar, BorderLayout.NORTH);

        // Table
        userTable = new JTable(tableModel);
        userTable.setRowHeight(46);
        userTable.setShowHorizontalLines(true);
        userTable.setShowVerticalLines(false);
        userTable.setGridColor(UITheme.BORDER);
        userTable.setFont(UITheme.FONT_REGULAR);
        userTable.setBackground(UITheme.CARD_BG);
        userTable.getTableHeader().setFont(UITheme.FONT_BOLD);
        userTable.getTableHeader().setPreferredSize(new Dimension(0, 38));
        userTable.getTableHeader().setBackground(UITheme.TINT);
        userTable.getTableHeader().setForeground(UITheme.TEXT_MUTED);

        userTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        userTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        userTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        userTable.getColumnModel().getColumn(3).setPreferredWidth(180);
        userTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        userTable.getColumnModel().getColumn(5).setPreferredWidth(110);
        userTable.getColumnModel().getColumn(6).setPreferredWidth(150);

        userTable.getColumnModel().getColumn(0).setCellRenderer(new RoleBadgeRenderer());
        userTable.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());

        ActionCellEditor cellEditor = new ActionCellEditor();
        userTable.getColumnModel().getColumn(6).setCellRenderer(cellEditor);
        userTable.getColumnModel().getColumn(6).setCellEditor(cellEditor);

        JScrollPane scroll = new JScrollPane(userTable);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        scroll.getViewport().setBackground(UITheme.CARD_BG);

        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private void loadUsersAsync() {
        String keyword = (txtSearch != null) ? txtSearch.getText().trim() : "";
        String roleFilter = "ALL";
        if (cmbRoleFilter != null) {
            int sel = cmbRoleFilter.getSelectedIndex();
            if (sel == 1) roleFilter = "ADMIN";
            else if (sel == 2) roleFilter = "LIBRARIAN";
            else if (sel == 3) roleFilter = "READER";
        }

        final String kw = keyword;
        final String rf = roleFilter;

        SwingWorker<List<UserAccountDto>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<UserAccountDto> doInBackground() {
                return authService.getAllUsers(kw, rf);
            }

            @Override
            protected void done() {
                try {
                    List<UserAccountDto> list = get();
                    tableModel.setUsers(list);

                    // Update KPI counters
                    int total = list.size();
                    long admins = 0;
                    long librarians = 0;
                    long readers = 0;
                    for (UserAccountDto u : list) {
                        if ("ADMIN".equalsIgnoreCase(u.getRoleCode())) {
                            admins++;
                        } else if ("LIBRARIAN".equalsIgnoreCase(u.getRoleCode())) {
                            librarians++;
                        } else if ("READER".equalsIgnoreCase(u.getRoleCode())) {
                            readers++;
                        }
                    }

                    lblTotalUsers.setText(String.valueOf(total));
                    lblTotalAdmins.setText(String.valueOf(admins));
                    lblTotalLibrarians.setText(String.valueOf(librarians));
                    lblTotalReaders.setText(String.valueOf(readers));
                } catch (Exception ex) {
                    UiModalHelper.showToastError(UserRoleManagementView.this, "Lỗi tải danh sách người dùng: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void showCreateStaffModal() {
        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        form.setPreferredSize(new Dimension(420, 260));

        JTextField txtUser = new JTextField();
        JPasswordField txtPass = new JPasswordField();
        JTextField txtName = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtPhone = new JTextField();
        JComboBox<String> cmbRole = new JComboBox<>(new String[]{"Thủ Thư (LIBRARIAN)", "Quản Trị Viên (ADMIN)"});

        form.add(new JLabel("Tên đăng nhập:"));
        form.add(txtUser);
        form.add(new JLabel("Mật khẩu ban đầu:"));
        form.add(txtPass);
        form.add(new JLabel("Họ và tên:"));
        form.add(txtName);
        form.add(new JLabel("Email liên hệ:"));
        form.add(txtEmail);
        form.add(new JLabel("Số điện thoại:"));
        form.add(txtPhone);
        form.add(new JLabel("Vai trò phân quyền:"));
        form.add(cmbRole);

        UiModalHelper.showCustomModal(
            this,
            "CREATE_STAFF_MODAL",
            "Cấp Tài Khoản Nhân Viên Thư Viện Mới",
            form,
            SimpleModalBorder.OK_CANCEL_OPTION,
            (controller, action) -> {
                if (action == SimpleModalBorder.OK_OPTION) {
                    String username = txtUser.getText().trim();
                    String rawPass = new String(txtPass.getPassword()).trim();
                    String name = txtName.getText().trim();
                    String email = txtEmail.getText().trim();
                    String phone = txtPhone.getText().trim();
                    String role = (cmbRole.getSelectedIndex() == 0) ? "LIBRARIAN" : "ADMIN";

                    if (username.isEmpty() || rawPass.isEmpty() || name.isEmpty()) {
                        UiModalHelper.showToastWarning(this, "Vui lòng nhập đầy đủ Tên đăng nhập, Mật khẩu và Họ tên!");
                        return;
                    }

                    try {
                        authService.createUserAccount(username, rawPass, name, email, phone, role);
                        controller.close();
                        UiModalHelper.showToastSuccess(this, "Cấp tài khoản nhân viên thành công!");
                        loadUsersAsync();
                    } catch (Exception ex) {
                        UiModalHelper.showToastError(this, ex.getMessage());
                    }
                } else if (action == SimpleModalBorder.CANCEL_OPTION || action == SimpleModalBorder.CLOSE_OPTION) {
                    controller.close();
                }
            }
        );
    }

    private void handleRoleChange(UserAccountDto user) {
        String[] options = {"Quản Trị Viên (ADMIN)", "Thủ Thư (LIBRARIAN)", "Độc Giả (READER)"};
        int currentIdx = "ADMIN".equalsIgnoreCase(user.getRoleCode()) ? 0 : ("LIBRARIAN".equalsIgnoreCase(user.getRoleCode()) ? 1 : 2);

        JComboBox<String> cmb = new JComboBox<>(options);
        cmb.setSelectedIndex(currentIdx);

        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setPreferredSize(new Dimension(320, 60));
        p.add(new JLabel("Chọn vai trò mới cho tài khoản [" + user.getUsername() + "]:"), BorderLayout.NORTH);
        p.add(cmb, BorderLayout.CENTER);

        UiModalHelper.showCustomModal(
            this,
            "CHANGE_ROLE_MODAL",
            "Phân Quyền Vai Trò RBAC",
            p,
            SimpleModalBorder.OK_CANCEL_OPTION,
            (controller, action) -> {
                if (action == SimpleModalBorder.OK_OPTION) {
                    int sel = cmb.getSelectedIndex();
                    String newRole = (sel == 0) ? "ADMIN" : (sel == 1 ? "LIBRARIAN" : "READER");
                    try {
                        authService.updateUserRole(user.getUserId(), newRole);
                        controller.close();
                        UiModalHelper.showToastSuccess(this, "Đã cập nhật vai trò thành công!");
                        loadUsersAsync();
                    } catch (Exception ex) {
                        UiModalHelper.showToastError(this, ex.getMessage());
                    }
                } else if (action == SimpleModalBorder.CANCEL_OPTION || action == SimpleModalBorder.CLOSE_OPTION) {
                    controller.close();
                }
            }
        );
    }

    private void handleToggleStatus(UserAccountDto user) {
        boolean nextState = !user.isActive();
        String actionPrompt = nextState ? "Mở khóa" : "Khóa";
        UiModalHelper.showConfirm(
            this,
            "Xác nhận thay đổi trạng thái",
            "Bạn có chắc muốn " + actionPrompt + " tài khoản [" + user.getUsername() + "] không?",
            () -> {
                try {
                    authService.toggleUserStatus(user.getUserId(), nextState);
                    UiModalHelper.showToastSuccess(this, "Đã " + actionPrompt.toLowerCase() + " tài khoản thành công!");
                    loadUsersAsync();
                } catch (Exception ex) {
                    UiModalHelper.showToastError(this, ex.getMessage());
                }
            }
        );
    }

    private void handleResetPassword(UserAccountDto user) {
        JPasswordField txtNewPass = new JPasswordField();
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setPreferredSize(new Dimension(300, 60));
        p.add(new JLabel("Nhập mật khẩu mới cho [" + user.getUsername() + "]:"), BorderLayout.NORTH);
        p.add(txtNewPass, BorderLayout.CENTER);

        UiModalHelper.showCustomModal(
            this,
            "RESET_PASS_MODAL",
            "Đặt Lại Mật Khẩu",
            p,
            SimpleModalBorder.OK_CANCEL_OPTION,
            (controller, action) -> {
                if (action == SimpleModalBorder.OK_OPTION) {
                    String pass = new String(txtNewPass.getPassword()).trim();
                    if (pass.length() < 6) {
                        UiModalHelper.showToastWarning(this, "Mật khẩu mới phải có ít nhất 6 ký tự!");
                        return;
                    }
                    try {
                        authService.resetUserPassword(user.getUserId(), pass);
                        controller.close();
                        UiModalHelper.showToastSuccess(this, "Đặt lại mật khẩu thành công!");
                    } catch (Exception ex) {
                        UiModalHelper.showToastError(this, ex.getMessage());
                    }
                } else if (action == SimpleModalBorder.CANCEL_OPTION || action == SimpleModalBorder.CLOSE_OPTION) {
                    controller.close();
                }
            }
        );
    }

    private class UserTableModel extends AbstractTableModel {
        private final String[] cols = {"Vai trò", "Tên đăng nhập", "Họ và tên", "Email", "Số điện thoại", "Trạng thái", "Thao tác"};
        private final List<UserAccountDto> users = new ArrayList<>();

        public void setUsers(List<UserAccountDto> list) {
            users.clear();
            if (list != null) users.addAll(list);
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return users.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public boolean isCellEditable(int r, int c) { return c == 6; }

        @Override
        public Object getValueAt(int r, int c) {
            UserAccountDto u = users.get(r);
            return switch (c) {
                case 0 -> cleanRoleName(u.getRoleName() != null ? u.getRoleName() : u.getRoleCode());
                case 1 -> u.getUsername();
                case 2 -> u.getFullName();
                case 3 -> (u.getEmail() != null && !u.getEmail().isEmpty()) ? u.getEmail() : "-";
                case 4 -> (u.getPhone() != null && !u.getPhone().isEmpty()) ? u.getPhone() : "-";
                case 5 -> u.isActive() ? "Hoạt động" : "Bị khóa";
                case 6 -> u;
                default -> null;
            };
        }
    }

    private static String cleanRoleName(String raw) {
        if (raw == null) return "Độc giả";
        String upper = raw.toUpperCase();
        if (upper.contains("ADMIN") || upper.contains("QUẢN TRỊ")) return "Quản trị viên";
        if (upper.contains("LIB") || upper.contains("THỦ THƯ")) return "Thủ thư";
        return "Độc giả";
    }

    private static class RoleBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            String text = (value != null) ? value.toString() : "";
            if (text.contains("Quản trị") || text.contains("ADMIN")) {
                lbl.setForeground(UITheme.PURPLE_TEXT);
            } else if (text.contains("Thủ thư") || text.contains("LIBRARIAN")) {
                lbl.setForeground(UITheme.PRIMARY);
            } else {
                lbl.setForeground(new Color(37, 99, 235));
            }
            lbl.setFont(UITheme.FONT_BOLD);
            return lbl;
        }
    }

    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            String text = (value != null) ? value.toString() : "";
            if ("Hoạt động".equalsIgnoreCase(text)) {
                lbl.setForeground(UITheme.SUCCESS_TEXT);
            } else {
                lbl.setForeground(UITheme.DANGER_TEXT);
            }
            lbl.setFont(UITheme.FONT_BOLD);
            return lbl;
        }
    }

    private class ActionCellEditor extends AbstractCellEditor implements javax.swing.table.TableCellRenderer, javax.swing.table.TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
        private final JButton btnRole = new JButton();
        private final JButton btnStatus = new JButton();
        private final JButton btnReset = new JButton();
        private UserAccountDto currentUser;

        ActionCellEditor() {
            panel.setOpaque(false);
            styleIconButton(btnRole, UITheme.TINT, "Phân quyền tài khoản");
            styleIconButton(btnStatus, UITheme.DANGER_BG, "Khóa/Mở tài khoản");
            styleIconButton(btnReset, UITheme.BG_LIGHT, "Đặt lại mật khẩu");

            btnRole.setIcon(IconFactory.createShieldIcon(UITheme.PRIMARY, 14));
            btnReset.setIcon(IconFactory.createKeyIcon(UITheme.TEXT_MAIN, 14));

            btnRole.addActionListener(e -> {
                fireEditingStopped();
                if (currentUser != null) handleRoleChange(currentUser);
            });

            btnStatus.addActionListener(e -> {
                fireEditingStopped();
                if (currentUser != null) handleToggleStatus(currentUser);
            });

            btnReset.addActionListener(e -> {
                fireEditingStopped();
                if (currentUser != null) handleResetPassword(currentUser);
            });

            panel.add(btnRole);
            panel.add(btnStatus);
            panel.add(btnReset);
        }

        private void styleIconButton(JButton btn, Color bg, String tooltip) {
            btn.setPreferredSize(new Dimension(32, 32));
            btn.setBackground(bg);
            btn.setToolTipText(tooltip);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setBorder(new UITheme.RoundedBorder(UITheme.BORDER, 8));
        }

        private void updateButtonStates(UserAccountDto u) {
            boolean active = u.isActive();
            btnStatus.setIcon(active
                ? IconFactory.createLockIcon(UITheme.DANGER_TEXT, 14)
                : IconFactory.createUnlockIcon(UITheme.SUCCESS_TEXT, 14));
            btnStatus.setBackground(active ? UITheme.DANGER_BG : UITheme.SUCCESS_BG);
            btnStatus.setToolTipText(active ? "Khóa tài khoản" : "Mở khóa tài khoản");
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            if (value instanceof UserAccountDto u) {
                currentUser = u;
                updateButtonStates(u);
            }
            return panel;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
            if (value instanceof UserAccountDto u) {
                currentUser = u;
                updateButtonStates(u);
            }
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentUser;
        }
    }
}
