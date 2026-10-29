package com.ximilib.gui.components;

import com.formdev.flatlaf.FlatClientProperties;
import com.ximilib.application.dto.LoginResult;
import com.ximilib.application.service.AuthenticationService;
import com.ximilib.infrastructure.config.AppContainer;
import com.ximilib.domain.model.User;
import com.ximilib.gui.MainDashboardFrame;
import com.ximilib.gui.RegisterForm;
import com.ximilib.presentation.util.IconFactory;
import com.ximilib.presentation.util.UITheme;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.prefs.Preferences;
import javax.swing.*;
import com.ximilib.presentation.util.UiModalHelper;

/**
 * LoginFormPanel: Khối Form Đăng Nhập bên phải màn hình.
 * - Căn giữa dọc, chiều rộng form chuẩn 360px - 380px.
 * - Mọi nhãn, tiêu đề, ô nhập và nút bấm được căn THẲNG LỀ TRÁI chuẩn xác 100%.
 * - Hỗ trợ nút hiện/ẩn mật khẩu FlatLaf, phím Enter/Tab và hiển thị lỗi inline.
 * - Gọi xác thực bất đồng bộ ngoài EDT qua AuthenticationService.
 */
public class LoginFormPanel extends JPanel {

    private static final String PREF_REMEMBERED_USER = "remembered_username";
    private final Preferences prefs = Preferences.userNodeForPackage(LoginFormPanel.class);

    private final AuthenticationService authService;
    private final Window parentWindow;

    private JTextField txtIdentifier;
    private JPasswordField txtPassword;
    private JCheckBox chkRemember;
    private JButton btnLogin;
    private JPanel errorPanel;
    private JLabel lblErrorMessage;
    private JLabel lblForgot;
    private JLabel lblRegisterNow;
    private JLabel lblGuest;

    public LoginFormPanel(Window parentWindow) {
        this(parentWindow, AppContainer.getInstance().getAuthenticationService());
    }

    public LoginFormPanel(Window parentWindow, AuthenticationService authService) {
        this.parentWindow = parentWindow;
        this.authService = (authService != null) ? authService : AppContainer.getInstance().getAuthenticationService();

        setOpaque(false);
        setLayout(new GridBagLayout()); // Căn giữa toàn bộ khối form theo chiều dọc và ngang

        initForm();
        loadRememberedUser();
    }

    private void initForm() {
        JPanel formContainer = new JPanel();
        formContainer.setOpaque(false);
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setPreferredSize(new Dimension(380, 520));
        formContainer.setMaximumSize(new Dimension(400, 560));
        formContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. Brand Header: Logo XimiLib + Badge "Library OS" (Căn trái)
        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        logoRow.setOpaque(false);
        logoRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel iconSquare = new JLabel(com.ximilib.presentation.util.AppLogo.getIcon(28));
        iconSquare.setPreferredSize(new Dimension(28, 28));

        JLabel lblBrand = new JLabel("XimiLib");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblBrand.setForeground(UITheme.TEXT_MAIN);

        JLabel lblBadge = new JLabel("Library OS") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.TINT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblBadge.setOpaque(false);
        lblBadge.setFont(UITheme.FONT_BADGE);
        lblBadge.setForeground(UITheme.ON_TINT);
        lblBadge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));

        logoRow.add(iconSquare);
        logoRow.add(lblBrand);
        logoRow.add(lblBadge);

        // 2. Tiêu đề và mô tả (Căn thẳng lề trái)
        JLabel lblTitle = new JLabel("Đăng nhập vào Thư viện");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(UITheme.TEXT_MAIN);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc = new JLabel("Chào mừng đến với hệ thống quản lý XimiLib");
        lblDesc.setFont(UITheme.FONT_REGULAR);
        // Màu xám đạt chuẩn tương phản WCAG >= 4.5:1 trên nền trắng (#475569)
        lblDesc.setForeground(UITheme.TEXT_MUTED);
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 3. Nhãn & Ô nhập: Tên đăng nhập hoặc Email
        JLabel lblUserTag = new JLabel("Tên đăng nhập hoặc Email");
        lblUserTag.setFont(UITheme.FONT_BOLD);
        lblUserTag.setForeground(UITheme.TEXT_MAIN);
        lblUserTag.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtIdentifier = new JTextField();
        txtIdentifier.setFont(UITheme.FONT_REGULAR);
        txtIdentifier.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtIdentifier.setPreferredSize(new Dimension(360, 44));
        txtIdentifier.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtIdentifier.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập tên đăng nhập hoặc email");
        txtIdentifier.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        txtIdentifier.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");
        txtIdentifier.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON,
            IconFactory.createUsersIcon(UITheme.TEXT_MUTED, 16));
        txtIdentifier.putClientProperty(FlatClientProperties.SELECT_ALL_ON_FOCUS_POLICY, "never");

        // 4. Nhãn & Ô nhập: Mật khẩu (Có nút Hiện/Ẩn mắt)
        JLabel lblPassTag = new JLabel("Mật khẩu");
        lblPassTag.setFont(UITheme.FONT_BOLD);
        lblPassTag.setForeground(UITheme.TEXT_MAIN);
        lblPassTag.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword = new JPasswordField();
        txtPassword.setFont(UITheme.FONT_REGULAR);
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtPassword.setPreferredSize(new Dimension(360, 44));
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập mật khẩu");
        txtPassword.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true; arc: 12;");
        txtPassword.putClientProperty(FlatClientProperties.SELECT_ALL_ON_FOCUS_POLICY, "never");

        // 5. Hàng tùy chọn: Ghi nhớ đăng nhập (trái) & Quên mật khẩu (phải)
        JPanel optionRow = new JPanel(new BorderLayout());
        optionRow.setOpaque(false);
        optionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        optionRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        chkRemember = new JCheckBox("Ghi nhớ đăng nhập", true);
        chkRemember.setFont(UITheme.FONT_SMALL);
        chkRemember.setForeground(UITheme.TEXT_MUTED);
        chkRemember.setOpaque(false);
        chkRemember.setFocusPainted(false);
        chkRemember.setCursor(new Cursor(Cursor.HAND_CURSOR));

        lblForgot = new JLabel("Quên mật khẩu?");
        lblForgot.setFont(UITheme.FONT_SMALL);
        lblForgot.setForeground(UITheme.PRIMARY);
        lblForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblForgot.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!lblForgot.isEnabled()) return;
                UiModalHelper.showToastInfo(parentWindow, "Vui lòng liên hệ Thủ thư hoặc Quản trị viên thư viện để được cấp lại mật khẩu truy cập.");
            }
        });

        optionRow.add(chkRemember, BorderLayout.WEST);
        optionRow.add(lblForgot, BorderLayout.EAST);

        // 6. Vùng hiển thị lỗi tại chỗ (Inline Error Panel)
        errorPanel = createInlineErrorPanel();
        errorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorPanel.setVisible(false);

        // 7. Nút ĐĂNG NHẬP chính (Rộng bằng ô nhập, bo góc)
        btnLogin = new JButton("ĐĂNG NHẬP") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!isEnabled()) {
                    g2.setColor(UITheme.CARD_BORDER);
                } else if (getModel().isPressed()) {
                    g2.setColor(UITheme.PRIMARY_PRESSED);
                } else if (getModel().isRollover()) {
                    g2.setColor(UITheme.PRIMARY_HOVER);
                } else {
                    g2.setColor(UITheme.PRIMARY);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnLogin.setFont(UITheme.FONT_BOLD);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setContentAreaFilled(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnLogin.setPreferredSize(new Dimension(360, 44));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.addActionListener(e -> executeLogin());

        // Phím Enter ở ô tên đăng nhập hoặc mật khẩu đều đăng nhập
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && btnLogin.isEnabled()) {
                    executeLogin();
                }
            }
        };
        txtIdentifier.addKeyListener(enterListener);
        txtPassword.addKeyListener(enterListener);

        // 8. Hàng Đăng ký tài khoản (UC-2)
        JPanel registerRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        registerRow.setOpaque(false);
        registerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel lblNoAcc = new JLabel("Chưa có tài khoản?");
        lblNoAcc.setFont(UITheme.FONT_SMALL);
        lblNoAcc.setForeground(UITheme.TEXT_MUTED);

        lblRegisterNow = new JLabel("Đăng ký ngay");
        lblRegisterNow.setFont(UITheme.FONT_BOLD);
        lblRegisterNow.setForeground(UITheme.PRIMARY);
        lblRegisterNow.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblRegisterNow.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!lblRegisterNow.isEnabled()) return;
                new RegisterForm().setVisible(true);
                if (parentWindow != null) {
                    parentWindow.dispose();
                }
            }
        });

        registerRow.add(lblNoAcc);
        registerRow.add(lblRegisterNow);

        // 9. Chân trang bản quyền
        JLabel lblFooter = new JLabel("© 2026 XimiLib • Trường ĐHBK Đà Nẵng", SwingConstants.CENTER);
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblFooter.setForeground(UITheme.TEXT_MUTED);
        lblFooter.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblFooter.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        // Ghép các thành phần theo thứ tự chuẩn từ trên xuống
        formContainer.add(logoRow);
        formContainer.add(Box.createVerticalStrut(14));
        formContainer.add(lblTitle);
        formContainer.add(Box.createVerticalStrut(4));
        formContainer.add(lblDesc);
        formContainer.add(Box.createVerticalStrut(22));

        formContainer.add(lblUserTag);
        formContainer.add(Box.createVerticalStrut(6));
        formContainer.add(txtIdentifier);
        formContainer.add(Box.createVerticalStrut(14));

        formContainer.add(lblPassTag);
        formContainer.add(Box.createVerticalStrut(6));
        formContainer.add(txtPassword);
        formContainer.add(Box.createVerticalStrut(10));

        formContainer.add(optionRow);
        formContainer.add(Box.createVerticalStrut(10));
        formContainer.add(errorPanel);
        formContainer.add(Box.createVerticalStrut(12));

        formContainer.add(btnLogin);
        formContainer.add(Box.createVerticalStrut(14));
        formContainer.add(registerRow);
        
        // Hàng Tra cứu không cần đăng nhập (Khách viếng thăm - SRS UC-4)
        JPanel guestRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        guestRow.setOpaque(false);
        guestRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        guestRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        lblGuest = new JLabel("Hoặc tra cứu sách không cần đăng nhập");
        lblGuest.setFont(UITheme.FONT_SMALL);
        lblGuest.setForeground(UITheme.PRIMARY);
        lblGuest.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblGuest.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!lblGuest.isEnabled()) return;
                User guest = new User("GUEST", "guest", "", "Khách Viếng Thăm", "", "", "Khách");
                MainDashboardFrame dashboard = new MainDashboardFrame(guest);
                dashboard.setVisible(true);
                if (parentWindow != null) {
                    parentWindow.dispose();
                }
            }
        });
        guestRow.add(lblGuest);
        formContainer.add(Box.createVerticalStrut(6));
        formContainer.add(guestRow);
        formContainer.add(Box.createVerticalGlue());
        formContainer.add(lblFooter);

        add(formContainer);
    }

    private JPanel createInlineErrorPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.DANGER_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(252, 165, 165)); // Viền đỏ nhạt
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        p.setPreferredSize(new Dimension(360, 40));
        p.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        // Icon chấm than đỏ vector
        JLabel iconError = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.DANGER_TEXT);
                g2.fillOval(0, (getHeight() - 16) / 2, 16, 16);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("!", (16 - fm.stringWidth("!")) / 2, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        iconError.setPreferredSize(new Dimension(16, 16));

        lblErrorMessage = new JLabel();
        lblErrorMessage.setFont(UITheme.FONT_SMALL);
        lblErrorMessage.setForeground(UITheme.DANGER_TEXT);

        p.add(iconError, BorderLayout.WEST);
        p.add(lblErrorMessage, BorderLayout.CENTER);
        return p;
    }

    private void showInlineError(String message) {
        if (message == null || message.trim().isEmpty()) {
            errorPanel.setVisible(false);
        } else {
            lblErrorMessage.setText("<html>" + message + "</html>");
            errorPanel.setVisible(true);
        }
        revalidate();
        repaint();
    }

    private void hideInlineError() {
        errorPanel.setVisible(false);
        revalidate();
        repaint();
    }

    private void loadRememberedUser() {
        String savedUser = prefs.get(PREF_REMEMBERED_USER, "");
        if (!savedUser.isEmpty()) {
            txtIdentifier.setText(savedUser);
            chkRemember.setSelected(true);
            SwingUtilities.invokeLater(() -> {
                txtIdentifier.select(0, 0);
                txtPassword.requestFocusInWindow();
            });
        } else {
            // Mặc định nạp tài khoản kien.phan để pair programming thuận tiện
            txtIdentifier.setText("kien.phan");
            txtPassword.setText("123456");
            SwingUtilities.invokeLater(() -> {
                txtIdentifier.select(0, 0);
                txtIdentifier.setCaretPosition(txtIdentifier.getText().length());
            });
        }
    }

    private void setFormInteractive(boolean enabled) {
        txtIdentifier.setEnabled(enabled);
        txtPassword.setEnabled(enabled);
        chkRemember.setEnabled(enabled);
        btnLogin.setEnabled(enabled);
        if (lblForgot != null) lblForgot.setEnabled(enabled);
        if (lblRegisterNow != null) lblRegisterNow.setEnabled(enabled);
        if (lblGuest != null) lblGuest.setEnabled(enabled);
    }

    private void executeLogin() {
        hideInlineError();
        txtIdentifier.select(0, 0);
        txtPassword.select(0, 0);

        String identifier = txtIdentifier.getText().trim();
        char[] passChars = txtPassword.getPassword();
        String password = new String(passChars).trim();

        if (identifier.isEmpty() || password.isEmpty()) {
            showInlineError("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            return;
        }

        // Khóa toàn bộ form: không thể click hay gõ vào ô tài khoản, mật khẩu, hay các nút
        setFormInteractive(false);
        btnLogin.setText("Đang đăng nhập...");

        // Chuyển focus khỏi các ô nhập văn bản để không hiện con trỏ nhấp nháy
        requestFocusInWindow();

        // Xử lý xác thực ngoài EDT (R-06)
        SwingWorker<LoginResult, Void> worker = new SwingWorker<>() {
            @Override
            protected LoginResult doInBackground() {
                try {
                    // Giả lập trễ mạng nhẹ ngoài EDT để hiệu ứng chuyển mượt
                    Thread.sleep(150);
                } catch (InterruptedException ignored) {}
                return authService.login(identifier, password);
            }

            @Override
            protected void done() {
                try {
                    LoginResult result = get();
                    SwingUtilities.invokeLater(() -> handleLoginResult(result, identifier));
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        setFormInteractive(true);
                        btnLogin.setText("ĐĂNG NHẬP");
                        showInlineError("Lỗi kết nối máy chủ xác thực: " + ex.getMessage());
                    });
                }
            }
        };
        worker.execute();
    }

    private void handleLoginResult(LoginResult result, String identifier) {
        setFormInteractive(true);
        btnLogin.setText("ĐĂNG NHẬP");

        if (result.isSuccess()) {
            // Lưu username nếu người dùng chọn ghi nhớ (chỉ lưu identifier, KHÔNG lưu mật khẩu)
            if (chkRemember.isSelected()) {
                prefs.put(PREF_REMEMBERED_USER, identifier);
            } else {
                prefs.remove(PREF_REMEMBERED_USER);
            }

            // Chuyển sang màn hình Dashboard chính
            MainDashboardFrame dashboard = new MainDashboardFrame(result.getUser());
            dashboard.setVisible(true);

            if (parentWindow != null) {
                parentWindow.dispose();
            }
        } else {
            showInlineError(result.getErrorMessage());
            txtPassword.setText("");
            txtPassword.requestFocusInWindow();
        }
    }
}
