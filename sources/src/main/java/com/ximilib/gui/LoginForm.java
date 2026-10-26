package com.ximilib.gui;

import com.ximilib.gui.util.UITheme;
import com.ximilib.gui.util.UserSession;
import com.ximilib.models.User;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * LoginForm: Giao diện cửa sổ Đăng Nhập được thiết kế theo phong cách tối giản,
 * hiện đại của LibRack (librack.com) với tông màu xanh Emerald Green.
 * Tác giả: Phan Vũ Xuân Kiên (GUI / DB)
 */
public class LoginForm extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JCheckBox chkRemember;
    private JLabel lblError;
    private JButton btnLogin;

    public LoginForm() {
        setTitle("XimiLib - Đăng Nhập Hệ Thống Quản Lý Thư Viện");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UITheme.BG_LIGHT);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UITheme.BG_LIGHT);
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Card trắng nổi bật ở trung tâm
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Nền card trắng
                g2.setColor(UITheme.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Viền xám mảnh
                g2.setColor(UITheme.BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(30, 32, 26, 32));
        card.setPreferredSize(new Dimension(380, 470));

        // 1. Logo XimiLib + Badge
        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        logoRow.setOpaque(false);

        // Icon vuông xanh ngọc
        JPanel iconSquare = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.8f));
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                g2.drawRoundRect(cx - 8, cy - 7, 7, 14, 2, 2);
                g2.drawRoundRect(cx + 1, cy - 7, 7, 14, 2, 2);
                g2.dispose();
            }
        };
        iconSquare.setPreferredSize(new Dimension(32, 32));
        iconSquare.setOpaque(false);

        JLabel lblBrand = new JLabel("XimiLib");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblBrand.setForeground(UITheme.TEXT_MAIN);

        JLabel lblBadge = new JLabel("Library OS") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY_MINT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblBadge.setOpaque(false);
        lblBadge.setFont(UITheme.FONT_BADGE);
        lblBadge.setForeground(UITheme.PRIMARY);
        lblBadge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));

        logoRow.add(iconSquare);
        logoRow.add(lblBrand);
        logoRow.add(lblBadge);

        // 2. Tiêu đề chào mừng
        JLabel lblHeading = new JLabel("Đăng nhập vào Thư viện");
        lblHeading.setFont(UITheme.FONT_TITLE);
        lblHeading.setForeground(UITheme.TEXT_MAIN);
        lblHeading.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Chào mừng đến với hệ thống quản lý XimiLib");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 3. Form nhập liệu
        JLabel lblUserTag = new JLabel("Tên đăng nhập hoặc Email");
        lblUserTag.setFont(UITheme.FONT_BOLD);
        lblUserTag.setForeground(UITheme.TEXT_MAIN);
        lblUserTag.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUsername = new JTextField("kien.phan");
        txtUsername.setFont(UITheme.FONT_REGULAR);
        txtUsername.setToolTipText("Thử các tài khoản: admin, kien.phan, nam.le, sinhvien (mật khẩu: 123456)");
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtUsername.setPreferredSize(new Dimension(320, 40));
        txtUsername.setBorder(new UITheme.RoundedBorder(UITheme.BORDER, 10));

        JLabel lblPassTag = new JLabel("Mật khẩu");
        lblPassTag.setFont(UITheme.FONT_BOLD);
        lblPassTag.setForeground(UITheme.TEXT_MAIN);
        lblPassTag.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword = new JPasswordField("123456");
        txtPassword.setFont(UITheme.FONT_REGULAR);
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtPassword.setPreferredSize(new Dimension(320, 40));
        txtPassword.setBorder(new UITheme.RoundedBorder(UITheme.BORDER, 10));

        // Hàng ghi nhớ đăng nhập & Quên mật khẩu
        JPanel optionRow = new JPanel(new BorderLayout());
        optionRow.setOpaque(false);
        optionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        chkRemember = new JCheckBox("Ghi nhớ đăng nhập", true);
        chkRemember.setFont(UITheme.FONT_SMALL);
        chkRemember.setForeground(UITheme.TEXT_MUTED);
        chkRemember.setOpaque(false);
        chkRemember.setFocusPainted(false);

        JLabel lblForgot = new JLabel("Quên mật khẩu?");
        lblForgot.setFont(UITheme.FONT_SMALL);
        lblForgot.setForeground(UITheme.PRIMARY);
        lblForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblForgot.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(LoginForm.this,
                    "Vui lòng liên hệ Quản trị viên (Nguyễn Việt Nhật) để cấp lại mật khẩu!",
                    "Quên Mật Khẩu", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        optionRow.add(chkRemember, BorderLayout.WEST);
        optionRow.add(lblForgot, BorderLayout.EAST);

        // Thông báo lỗi (mặc định ẩn)
        lblError = new JLabel(" ");
        lblError.setFont(UITheme.FONT_SMALL);
        lblError.setForeground(UITheme.DANGER_TEXT);
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 4. Nút ĐĂNG NHẬP chính
        btnLogin = UITheme.createPillButton("ĐĂNG NHẬP", UITheme.PRIMARY, Color.WHITE);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnLogin.setPreferredSize(new Dimension(320, 42));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.addActionListener(e -> handleLogin());

        // Hỗ trợ bấm Enter để đăng nhập
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        };
        txtUsername.addKeyListener(enterListener);
        txtPassword.addKeyListener(enterListener);

        // 5. Footer bản quyền
        JLabel lblFooter = new JLabel("© 2026 XimiLib • Trường ĐHBK Đà Nẵng");
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblFooter.setForeground(new Color(148, 163, 184));
        lblFooter.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Ghép các thành phần vào Card
        card.add(logoRow);
        card.add(Box.createVerticalStrut(12));
        card.add(lblHeading);
        card.add(Box.createVerticalStrut(4));
        card.add(lblSub);
        card.add(Box.createVerticalStrut(20));

        card.add(lblUserTag);
        card.add(Box.createVerticalStrut(6));
        card.add(txtUsername);
        card.add(Box.createVerticalStrut(12));

        card.add(lblPassTag);
        card.add(Box.createVerticalStrut(6));
        card.add(txtPassword);
        card.add(Box.createVerticalStrut(8));

        card.add(optionRow);
        card.add(Box.createVerticalStrut(6));
        card.add(lblError);
        card.add(Box.createVerticalStrut(6));

        // Hàng liên kết Đăng ký tài khoản
        JPanel regRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        regRow.setOpaque(false);
        JLabel lblNoAcc = new JLabel("Chưa có tài khoản?");
        lblNoAcc.setFont(UITheme.FONT_SMALL);
        lblNoAcc.setForeground(UITheme.TEXT_MUTED);

        JLabel lblRegisterNow = new JLabel("Đăng ký ngay");
        lblRegisterNow.setFont(UITheme.FONT_BOLD);
        lblRegisterNow.setForeground(UITheme.PRIMARY);
        lblRegisterNow.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblRegisterNow.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new RegisterForm().setVisible(true);
                dispose();
            }
        });

        regRow.add(lblNoAcc);
        regRow.add(lblRegisterNow);

        card.add(btnLogin);
        card.add(Box.createVerticalStrut(12));
        card.add(regRow);
        card.add(Box.createVerticalGlue());
        card.add(lblFooter);

        root.add(card);
        add(root);
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            return;
        }

        // Xác thực qua UserSession (chứa danh sách tài khoản theo nhóm đồ án)
        User authenticatedUser = UserSession.authenticate(username, password);
        if (authenticatedUser == null) {
            lblError.setText("Sai tài khoản hoặc mật khẩu!");
            JOptionPane.showMessageDialog(this,
                "Tài khoản hoặc mật khẩu không chính xác!\n\n" +
                "Các tài khoản mẫu có sẵn trong hệ thống:\n" +
                "• admin / 123456 (Nguyễn Việt Nhật - Quản Trị Viên)\n" +
                "• kien.phan / 123456 (Phan Vũ Xuân Kiên - Thủ Thư)\n" +
                "• nam.le / 123456 (Lê Võ Thành Nam - Thủ Thư)\n" +
                "• sinhvien / 123456 (Trần Văn An - Bạn Đọc)\n\n" +
                "Hoặc bạn có thể bấm 'Đăng ký ngay' để tạo thêm tài khoản mới!",
                "Đăng Nhập Thất Bại", JOptionPane.WARNING_MESSAGE);
            return;
        }

        lblError.setText("Đang đăng nhập vào hệ thống...");
        Timer timer = new Timer(200, e -> {
            // Mở Main Dashboard với thông tin của chính tài khoản vừa đăng nhập
            MainDashboardFrame dashboard = new MainDashboardFrame(authenticatedUser);
            dashboard.setVisible(true);
            dispose();
        });
        timer.setRepeats(false);
        timer.start();
    }
}
