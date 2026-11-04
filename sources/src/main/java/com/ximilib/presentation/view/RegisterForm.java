package com.ximilib.presentation.view;

import com.formdev.flatlaf.FlatClientProperties;
import com.ximilib.application.dto.RegisterRequestDto;
import com.ximilib.application.service.AuthenticationService;
import com.ximilib.presentation.component.PasswordPolicyChecklist;
import com.ximilib.presentation.util.AppLogo;
import com.ximilib.presentation.util.UITheme;
import com.ximilib.infrastructure.config.AppContainer;
import com.ximilib.presentation.util.UiModalHelper;
import raven.datetime.DatePicker;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;

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
    private PasswordPolicyChecklist policyChecklist;
    private JLabel lblError;
    private JButton btnRegister;

    public RegisterForm() {
        this(AppContainer.getInstance().getAuthenticationService());
    }

    public RegisterForm(AuthenticationService authService) {
        this.authService = authService;

        setTitle("XimiLib - Đăng Ký Tài Khoản Bạn Đọc Thư Viện");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(680, 770);
        setMinimumSize(new Dimension(640, 720));
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UITheme.BG_LIGHT);

        AppLogo.applyWindowIcons(this);
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
        card.setBorder(BorderFactory.createEmptyBorder(16, 26, 16, 26));
        card.setPreferredSize(new Dimension(620, 710));
        card.setMaximumSize(new Dimension(640, 730));

        JLabel lblHeading = new JLabel("Đăng Ký Tài Khoản Bạn Đọc");
        lblHeading.setFont(UITheme.FONT_TITLE_LARGE);
        lblHeading.setForeground(UITheme.TEXT_MAIN);
        lblHeading.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Tạo tài khoản để mượn sách, tra cứu tài liệu và nhận thẻ thư viện điện tử");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lblHeading);
        card.add(Box.createVerticalStrut(2));
        card.add(lblSub);
        card.add(Box.createVerticalStrut(12));

        initInputFields();

        card.add(createSectionHeader("1. Thông tin cá nhân"));
        card.add(Box.createVerticalStrut(6));
        card.add(createFieldGroup("Họ và tên *", txtFullName));
        card.add(Box.createVerticalStrut(6));
        card.add(createPairRow(
            createFieldGroup("Ngày sinh *", txtBirthDate),
            createFieldGroup("Số CCCD/CMND *", txtCccd)
        ));
        card.add(Box.createVerticalStrut(10));

        card.add(createSectionHeader("2. Thông tin liên hệ"));
        card.add(Box.createVerticalStrut(6));
        card.add(createFieldGroup("Địa chỉ cư trú hiện tại", txtAddress));
        card.add(Box.createVerticalStrut(6));
        card.add(createPairRow(
            createFieldGroup("Email *", txtEmail),
            createFieldGroup("Số điện thoại *", txtPhone)
        ));
        card.add(Box.createVerticalStrut(10));

        card.add(createSectionHeader("3. Tài khoản & Bảo mật"));
        card.add(Box.createVerticalStrut(6));
        card.add(createFieldGroup("Tên đăng nhập *", txtUsername));
        card.add(Box.createVerticalStrut(6));
        card.add(createPairRow(
            createFieldGroup("Mật khẩu *", txtPassword),
            createFieldGroup("Xác nhận mật khẩu *", txtConfirmPassword)
        ));
        card.add(Box.createVerticalStrut(6));

        policyChecklist.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(policyChecklist);

        lblError = new JLabel(" ");
        lblError.setFont(UITheme.FONT_SMALL);
        lblError.setForeground(UITheme.DANGER_TEXT);
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(Box.createVerticalStrut(6));
        card.add(lblError);

        btnRegister = UITheme.createPillButton("TẠO TÀI KHOẢN", UITheme.PRIMARY, Color.WHITE);
        btnRegister.setFont(UITheme.FONT_BOLD);
        btnRegister.setPreferredSize(new Dimension(360, 42));
        btnRegister.setMaximumSize(new Dimension(420, 42));
        btnRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegister.addActionListener(e -> handleRegister());
        card.add(Box.createVerticalStrut(6));
        card.add(btnRegister);

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
        card.add(Box.createVerticalStrut(8));
        card.add(loginLinkRow);

        root.add(card);
        add(root);
    }

    private void initInputFields() {
        txtFullName = createTextField("Nhập họ và tên đầy đủ");

        txtBirthDate = new JFormattedTextField();
        txtBirthDate.setFont(UITheme.FONT_REGULAR);
        txtBirthDate.setPreferredSize(new Dimension(280, 34));
        txtBirthDate.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "dd/MM/yyyy");
        txtBirthDate.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");
        birthDatePicker = UiModalHelper.createDatePicker(txtBirthDate, false);

        txtCccd = createTextField("9 hoặc 12 chữ số");
        txtAddress = createTextField("Địa chỉ cư trú hiện tại");
        txtEmail = createTextField("example@domain.com");
        txtPhone = createTextField("Số điện thoại (10 chữ số)");
        txtUsername = createTextField("Tên đăng nhập viết liền");

        txtPassword = createPasswordField("Tối thiểu 8 ký tự");
        txtConfirmPassword = createPasswordField("Nhập lại mật khẩu");

        policyChecklist = new PasswordPolicyChecklist();

        txtPassword.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateChecklist(); }
            @Override public void removeUpdate(DocumentEvent e) { updateChecklist(); }
            @Override public void changedUpdate(DocumentEvent e) { updateChecklist(); }

            private void updateChecklist() {
                String pwd = new String(txtPassword.getPassword());
                policyChecklist.updatePassword(pwd);
                clearOutline(txtPassword);
            }
        });

        registerClearOutlineOnType(txtFullName);
        registerClearOutlineOnType(txtBirthDate);
        registerClearOutlineOnType(txtCccd);
        registerClearOutlineOnType(txtAddress);
        registerClearOutlineOnType(txtEmail);
        registerClearOutlineOnType(txtPhone);
        registerClearOutlineOnType(txtUsername);
        registerClearOutlineOnType(txtConfirmPassword);

        txtEmail.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String em = txtEmail.getText().trim();
                if (!em.isEmpty() && !em.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                    markError(txtEmail, "Email không đúng định dạng!");
                }
            }
        });

        txtPhone.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String ph = txtPhone.getText().trim();
                if (!ph.isEmpty() && !ph.matches("^0\\d{9}$")) {
                    markError(txtPhone, "Số điện thoại phải gồm 10 chữ số (bắt đầu bằng 0)!");
                }
            }
        });

        txtCccd.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String c = txtCccd.getText().trim();
                if (!c.isEmpty() && !c.matches("^(\\d{9}|\\d{12})$")) {
                    markError(txtCccd, "Số CCCD/CMND phải gồm đúng 9 hoặc 12 chữ số!");
                }
            }
        });

        txtConfirmPassword.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String p1 = new String(txtPassword.getPassword());
                String p2 = new String(txtConfirmPassword.getPassword());
                if (!p2.isEmpty() && !p1.equals(p2)) {
                    markError(txtConfirmPassword, "Mật khẩu xác nhận không khớp!");
                }
            }
        });
    }

    private JPanel createSectionHeader(String title) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        JLabel lbl = new JLabel(title.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(UITheme.PRIMARY);
        p.add(lbl);
        return p;
    }

    private JPanel createFieldGroup(String label, JComponent field) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_SMALL.deriveFont(Font.BOLD));
        lbl.setForeground(UITheme.TEXT_MAIN);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(lbl);
        p.add(Box.createVerticalStrut(3));
        p.add(field);
        return p;
    }

    private JPanel createPairRow(JPanel left, JPanel right) {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        row.add(left);
        row.add(right);
        return row;
    }

    private JTextField createTextField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(UITheme.FONT_REGULAR);
        tf.setPreferredSize(new Dimension(280, 34));
        tf.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        tf.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");
        return tf;
    }

    private JPasswordField createPasswordField(String placeholder) {
        JPasswordField pf = new JPasswordField();
        pf.setFont(UITheme.FONT_REGULAR);
        pf.setPreferredSize(new Dimension(280, 34));
        pf.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        pf.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true; arc: 10;");
        return pf;
    }

    private void registerClearOutlineOnType(JTextField tf) {
        tf.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { clearOutline(tf); }
            @Override public void removeUpdate(DocumentEvent e) { clearOutline(tf); }
            @Override public void changedUpdate(DocumentEvent e) { clearOutline(tf); }
        });
    }

    private void clearOutline(JComponent comp) {
        comp.putClientProperty(FlatClientProperties.OUTLINE, null);
        lblError.setText(" ");
    }

    private void markError(JComponent comp, String message) {
        if (comp != null) {
            comp.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
            comp.requestFocusInWindow();
        }
        lblError.setText(message);
    }

    private void setFormInteractive(boolean enabled) {
        txtFullName.setEnabled(enabled);
        txtBirthDate.setEnabled(enabled);
        txtCccd.setEnabled(enabled);
        txtAddress.setEnabled(enabled);
        txtEmail.setEnabled(enabled);
        txtPhone.setEnabled(enabled);
        txtUsername.setEnabled(enabled);
        txtPassword.setEnabled(enabled);
        txtConfirmPassword.setEnabled(enabled);
        btnRegister.setEnabled(enabled);
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

        if (fullName.isEmpty()) {
            markError(txtFullName, "Vui lòng nhập họ và tên đầy đủ!");
            return;
        }

        LocalDate birthDate = birthDatePicker.getSelectedDate();
        if (birthDate == null) {
            markError(txtBirthDate, "Vui lòng chọn ngày sinh qua lịch!");
            return;
        }

        if (cccd.isEmpty()) {
            markError(txtCccd, "Vui lòng nhập số CCCD/CMND!");
            return;
        }

        if (!cccd.matches("^(\\d{9}|\\d{12})$")) {
            markError(txtCccd, "Số CCCD/CMND phải gồm đúng 9 hoặc 12 chữ số!");
            return;
        }

        if (email.isEmpty()) {
            markError(txtEmail, "Vui lòng nhập địa chỉ email!");
            return;
        }

        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            markError(txtEmail, "Địa chỉ email không đúng định dạng!");
            return;
        }

        if (phone.isEmpty()) {
            markError(txtPhone, "Vui lòng nhập số điện thoại!");
            return;
        }

        if (!phone.matches("^0\\d{9}$")) {
            markError(txtPhone, "Số điện thoại phải gồm 10 chữ số (bắt đầu bằng 0)!");
            return;
        }

        if (username.isEmpty() || username.length() < 3 || username.contains(" ")) {
            markError(txtUsername, "Tên đăng nhập tối thiểu 3 ký tự và không chứa khoảng trắng!");
            return;
        }

        if (pass.isEmpty() || !policyChecklist.isAllSatisfied()) {
            markError(txtPassword, "Mật khẩu chưa đáp ứng đầy đủ chính sách bảo mật!");
            return;
        }

        if (!pass.equals(confirm)) {
            markError(txtConfirmPassword, "Mật khẩu xác nhận không khớp!");
            return;
        }

        RegisterRequestDto req = new RegisterRequestDto(username, pass, fullName, email, phone, cccd, address, birthDate);

        setFormInteractive(false);
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
                        setFormInteractive(true);
                        btnRegister.setText("TẠO TÀI KHOẢN");
                        markError(null, cause.getMessage());
                    });
                }
            }
        };
        worker.execute();
    }
}
