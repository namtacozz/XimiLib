package com.ximilib.gui;

import com.ximilib.gui.util.UITheme;
import com.ximilib.gui.util.UserSession;
import com.ximilib.models.User;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

/**
 * RegisterForm: Giao diện Đăng Ký Tài Khoản mới cho Độc Giả / Sinh Viên
 * chuẩn phong cách tối giản LibRack.
 * Tác giả: Phan Vũ Xuân Kiên (GUI / DB)
 */
public class RegisterForm extends JFrame {

    private JTextField txtFullName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JLabel lblError;

    public RegisterForm() {
        setTitle("XimiLib - Đăng Ký Tài Khoản Bạn Đọc Thư Viện");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(480, 680);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UITheme.BG_LIGHT);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UITheme.BG_LIGHT);
        root.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(UITheme.BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));
        card.setPreferredSize(new Dimension(410, 620));

        // Tiêu đề
        JLabel lblHeading = new JLabel("Đăng Ký Tài Khoản");
        lblHeading.setFont(UITheme.FONT_TITLE_LARGE);
        lblHeading.setForeground(UITheme.TEXT_MAIN);
        lblHeading.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Tạo tài khoản bạn đọc để mượn sách tại thư viện");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Các trường nhập liệu
        txtFullName = createTextField("Nhập họ và tên đầy đủ");
        txtEmail = createTextField("Nhập địa chỉ email");
        txtPhone = createTextField("Nhập số điện thoại");
        txtUsername = createTextField("Tên đăng nhập mong muốn");
        txtPassword = new JPasswordField();
        txtPassword.setBorder(new UITheme.RoundedBorder(UITheme.BORDER, 8));
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        txtConfirmPassword = new JPasswordField();
        txtConfirmPassword.setBorder(new UITheme.RoundedBorder(UITheme.BORDER, 8));
        txtConfirmPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        lblError = new JLabel(" ");
        lblError.setFont(UITheme.FONT_SMALL);
        lblError.setForeground(UITheme.DANGER_TEXT);
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnRegister = UITheme.createPillButton("TẠO TÀI KHOẢN", UITheme.PRIMARY, Color.WHITE);
        btnRegister.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegister.addActionListener(e -> handleRegister());

        // Link quay lại Đăng Nhập
        JPanel loginLinkRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        loginLinkRow.setOpaque(false);

        JLabel lblHaveAcc = new JLabel("Đã có tài khoản?");
        lblHaveAcc.setFont(UITheme.FONT_SMALL);
        lblHaveAcc.setForeground(UITheme.TEXT_MUTED);

        JLabel lblLoginNow = new JLabel("Đăng nhập ngay");
        lblLoginNow.setFont(UITheme.FONT_BOLD);
        lblLoginNow.setForeground(UITheme.PRIMARY);
        lblLoginNow.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblLoginNow.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new LoginForm().setVisible(true);
                dispose();
            }
        });

        loginLinkRow.add(lblHaveAcc);
        loginLinkRow.add(lblLoginNow);

        // Ghép các thành phần
        card.add(lblHeading);
        card.add(Box.createVerticalStrut(4));
        card.add(lblSub);
        card.add(Box.createVerticalStrut(14));

        card.add(createLabel("Họ và tên:"));
        card.add(txtFullName);
        card.add(Box.createVerticalStrut(6));

        card.add(createLabel("Email:"));
        card.add(txtEmail);
        card.add(Box.createVerticalStrut(6));

        card.add(createLabel("Số điện thoại:"));
        card.add(txtPhone);
        card.add(Box.createVerticalStrut(6));

        card.add(createLabel("Tên đăng nhập:"));
        card.add(txtUsername);
        card.add(Box.createVerticalStrut(6));

        card.add(createLabel("Mật khẩu:"));
        card.add(txtPassword);
        card.add(Box.createVerticalStrut(6));

        card.add(createLabel("Nhập lại mật khẩu:"));
        card.add(txtConfirmPassword);
        card.add(Box.createVerticalStrut(4));

        card.add(lblError);
        card.add(Box.createVerticalStrut(8));
        card.add(btnRegister);
        card.add(Box.createVerticalStrut(12));
        card.add(loginLinkRow);

        root.add(card);
        add(root);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(UITheme.TEXT_MAIN);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField createTextField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(UITheme.FONT_REGULAR);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        tf.setBorder(new UITheme.RoundedBorder(UITheme.BORDER, 8));
        return tf;
    }

    private void handleRegister() {
        String fullName = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String username = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword()).trim();
        String confirm = new String(txtConfirmPassword.getPassword()).trim();

        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || pass.isEmpty()) {
            lblError.setText("Vui lòng điền đầy đủ các thông tin bắt buộc!");
            return;
        }

        if (!pass.equals(confirm)) {
            lblError.setText("Mật khẩu xác nhận không khớp!");
            return;
        }

        // Tạo tài khoản mới vào UserSession
        String newId = "ND00" + (UserSession.getAllUsers().size() + 1);
        User newUser = new User(newId, username, pass, fullName, email, phone, "Độc Giả");
        boolean success = UserSession.register(newUser);

        if (!success) {
            lblError.setText("Tên đăng nhập hoặc Email này đã tồn tại!");
            return;
        }

        JOptionPane.showMessageDialog(this,
            "Đăng ký tài khoản thành công cho: " + fullName + "!\nBạn có thể đăng nhập vào hệ thống ngay.",
            "Thành Công", JOptionPane.INFORMATION_MESSAGE);

        new LoginForm().setVisible(true);
        dispose();
    }
}
