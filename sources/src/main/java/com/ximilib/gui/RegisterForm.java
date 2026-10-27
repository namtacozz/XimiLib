package com.ximilib.gui;

import com.ximilib.application.dto.RegisterRequestDto;
import com.ximilib.application.service.AuthenticationService;
import com.ximilib.gui.util.UITheme;
import com.ximilib.infrastructure.config.AppContainer;
import com.ximilib.presentation.util.UiModalHelper;
import raven.datetime.DatePicker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;

/**
 * RegisterForm: Giao diện Đăng Ký Tài Khoản mới cho Độc Giả (SRS UC-2).
 * Tự động cấp thẻ thư viện hiệu lực 365 ngày (BR-02).
 * Tuân thủ R-01, R-06, R-08, R-11.
 */
public class RegisterForm extends JFrame {

    private final AuthenticationService authService;

    private JTextField txtFullName;
    private JFormattedTextField txtBirthDate;
    private DatePicker birthDatePicker;
    private JTextField txtCccd;
    private JTextField txtAddress;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JLabel lblError;
    private JButton btnRegister;

    public RegisterForm() {
        this(AppContainer.getInstance().getAuthenticationService());
    }

    public RegisterForm(AuthenticationService authService) {
        this.authService = authService;

        setTitle("XimiLib - Đăng Ký Tài Khoản Bạn Đọc Thư Viện");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(520, 780);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UITheme.BG_LIGHT);

        UiModalHelper.initMainFrame(this);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UITheme.BG_LIGHT);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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
        card.setBorder(BorderFactory.createEmptyBorder(20, 26, 18, 26));
        card.setPreferredSize(new Dimension(460, 720));

        JLabel lblHeading = new JLabel("Đăng Ký Tài Khoản");
        lblHeading.setFont(UITheme.FONT_TITLE_LARGE);
        lblHeading.setForeground(UITheme.TEXT_MAIN);
        lblHeading.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Tạo tài khoản bạn đọc để mượn sách tại thư viện");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtFullName = createTextField();
        txtBirthDate = new JFormattedTextField();
        txtBirthDate.setFont(UITheme.FONT_REGULAR);
        txtBirthDate.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txtBirthDate.setBorder(new UITheme.RoundedBorder(UITheme.BORDER, 8));
        birthDatePicker = UiModalHelper.createDatePicker(txtBirthDate, false);
        birthDatePicker.setSelectedDate(LocalDate.of(2004, 1, 1));

        txtCccd = createTextField();
        txtAddress = createTextField();
        txtEmail = createTextField();
        txtPhone = createTextField();
        txtUsername = createTextField();

        txtPassword = new JPasswordField();
        txtPassword.setBorder(new UITheme.RoundedBorder(UITheme.BORDER, 8));
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        txtConfirmPassword = new JPasswordField();
        txtConfirmPassword.setBorder(new UITheme.RoundedBorder(UITheme.BORDER, 8));
        txtConfirmPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        lblError = new JLabel(" ");
        lblError.setFont(UITheme.FONT_SMALL);
        lblError.setForeground(UITheme.DANGER_TEXT);
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnRegister = UITheme.createPillButton("TẠO TÀI KHOẢN", UITheme.PRIMARY, Color.WHITE);
        btnRegister.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegister.addActionListener(e -> handleRegister());

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

        card.add(lblHeading);
        card.add(Box.createVerticalStrut(2));
        card.add(lblSub);
        card.add(Box.createVerticalStrut(10));

        addFormField(card, "Họ và tên:", txtFullName);
        addFormField(card, "Ngày sinh (dd/MM/yyyy):", txtBirthDate);
        addFormField(card, "Số CCCD/CMND (9-12 số):", txtCccd);
        addFormField(card, "Địa chỉ liên lạc:", txtAddress);
        addFormField(card, "Email:", txtEmail);
        addFormField(card, "Số điện thoại:", txtPhone);
        addFormField(card, "Tên đăng nhập:", txtUsername);
        addFormField(card, "Mật khẩu (≥8 ký tự, có số, chữ hoa & ký tự đặc biệt):", txtPassword);
        addFormField(card, "Nhập lại mật khẩu:", txtConfirmPassword);

        card.add(lblError);
        card.add(Box.createVerticalStrut(6));
        card.add(btnRegister);
        card.add(Box.createVerticalStrut(8));
        card.add(loginLinkRow);

        root.add(card);
        add(root);
    }

    private void addFormField(JPanel card, String label, JComponent field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(UITheme.TEXT_MAIN);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lbl);
        card.add(field);
        card.add(Box.createVerticalStrut(4));
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(UITheme.FONT_REGULAR);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        tf.setBorder(new UITheme.RoundedBorder(UITheme.BORDER, 8));
        return tf;
    }

    private void handleRegister() {
        lblError.setText(" ");
        String fullName = txtFullName.getText().trim();
        String cccd = txtCccd.getText().trim();
        String address = txtAddress.getText().trim();
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

        LocalDate birthDate = birthDatePicker.getSelectedDate();
        RegisterRequestDto req = new RegisterRequestDto(username, pass, fullName, email, phone, cccd, address, birthDate);

        btnRegister.setEnabled(false);
        btnRegister.setText("Đang đăng ký...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                authService.register(req);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    SwingUtilities.invokeLater(() -> {
                        UiModalHelper.showToastSuccess(RegisterForm.this, "Đăng ký thành công! Thẻ thư viện đã được cấp.");
                        Timer timer = new Timer(1200, e -> {
                            new LoginForm().setVisible(true);
                            dispose();
                        });
                        timer.setRepeats(false);
                        timer.start();
                    });
                } catch (Exception ex) {
                    Throwable cause = (ex.getCause() != null) ? ex.getCause() : ex;
                    SwingUtilities.invokeLater(() -> {
                        btnRegister.setEnabled(true);
                        btnRegister.setText("TẠO TÀI KHOẢN");
                        lblError.setText(cause.getMessage());
                    });
                }
            }
        };
        worker.execute();
    }
}
