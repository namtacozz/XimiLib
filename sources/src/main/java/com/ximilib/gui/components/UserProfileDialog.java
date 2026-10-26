package com.ximilib.gui.components;

import com.ximilib.gui.LoginForm;
import com.ximilib.gui.util.UITheme;
import java.awt.*;
import javax.swing.*;

/**
 * UserProfileDialog: Bảng thông tin chi tiết tài khoản người dùng,
 * hỗ trợ đổi mật khẩu và đăng xuất khỏi hệ thống.
 * Tác giả: Phan Vũ Xuân Kiên (GUI / DB)
 */
public class UserProfileDialog extends JDialog {

    private final JFrame parentFrame;
    private final String fullName;
    private final String email;
    private final String phone;
    private final String role;

    private JPasswordField txtCurrentPass;
    private JPasswordField txtNewPass;
    private JPasswordField txtConfirmPass;

    public UserProfileDialog(JFrame parent, String fullName, String email, String phone, String role) {
        super(parent, "Thông Tin Tài Khoản", true);
        this.parentFrame = parent;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;

        setSize(440, 640);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(UITheme.CARD_BG);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        JPanel container = new JPanel();
        container.setBackground(UITheme.CARD_BG);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));

        // 1. Header Profile: Avatar tròn lớn + Tên + Badge Vai trò
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JPanel avatarLarge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Nền xanh mint
                g2.setColor(UITheme.PRIMARY_MINT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                // Chữ cái đầu
                g2.setColor(UITheme.PRIMARY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
                FontMetrics fm = g2.getFontMetrics();
                String initial = fullName.isEmpty() ? "U" : String.valueOf(fullName.charAt(fullName.lastIndexOf(" ") + 1));
                g2.drawString(initial, (getWidth() - fm.stringWidth(initial)) / 2, 44);
                g2.dispose();
            }
        };
        avatarLarge.setPreferredSize(new Dimension(64, 64));
        avatarLarge.setMaximumSize(new Dimension(64, 64));
        avatarLarge.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblName = new JLabel(fullName);
        lblName.setFont(UITheme.FONT_TITLE);
        lblName.setForeground(UITheme.TEXT_MAIN);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblRoleBadge = new JLabel(role) {
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
        lblRoleBadge.setOpaque(false);
        lblRoleBadge.setFont(UITheme.FONT_BADGE);
        lblRoleBadge.setForeground(UITheme.PRIMARY);
        lblRoleBadge.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        lblRoleBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(avatarLarge);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(lblName);
        headerPanel.add(Box.createVerticalStrut(6));
        headerPanel.add(lblRoleBadge);

        // 2. Nhóm thông tin chi tiết (Họ tên, Gmail, SĐT)
        JPanel infoBox = new JPanel();
        infoBox.setOpaque(false);
        infoBox.setLayout(new BoxLayout(infoBox, BoxLayout.Y_AXIS));
        infoBox.setBorder(BorderFactory.createCompoundBorder(
            new UITheme.RoundedBorder(UITheme.BORDER, 12),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        infoBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        infoBox.add(createInfoRow("Họ và tên:", fullName));
        infoBox.add(Box.createVerticalStrut(8));
        infoBox.add(createInfoRow("Gmail:", email));
        infoBox.add(Box.createVerticalStrut(8));
        infoBox.add(createInfoRow("Số điện thoại:", phone));

        // 3. Khung Đổi Mật Khẩu
        JPanel passBox = new JPanel();
        passBox.setOpaque(false);
        passBox.setLayout(new BoxLayout(passBox, BoxLayout.Y_AXIS));
        passBox.setBorder(BorderFactory.createCompoundBorder(
            new UITheme.RoundedBorder(UITheme.BORDER, 12),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        passBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        JLabel lblPassSection = new JLabel("Đổi Mật Khẩu");
        lblPassSection.setFont(UITheme.FONT_BOLD);
        lblPassSection.setForeground(UITheme.TEXT_MAIN);
        passBox.add(lblPassSection);
        passBox.add(Box.createVerticalStrut(8));

        txtCurrentPass = new JPasswordField();
        txtCurrentPass.setBorder(new UITheme.RoundedBorder(UITheme.BORDER, 8));
        txtCurrentPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        passBox.add(createFieldRow("Mật khẩu hiện tại:", txtCurrentPass));
        passBox.add(Box.createVerticalStrut(6));

        txtNewPass = new JPasswordField();
        txtNewPass.setBorder(new UITheme.RoundedBorder(UITheme.BORDER, 8));
        txtNewPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        passBox.add(createFieldRow("Mật khẩu mới:", txtNewPass));
        passBox.add(Box.createVerticalStrut(6));

        txtConfirmPass = new JPasswordField();
        txtConfirmPass.setBorder(new UITheme.RoundedBorder(UITheme.BORDER, 8));
        txtConfirmPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        passBox.add(createFieldRow("Nhập lại mật khẩu mới:", txtConfirmPass));
        passBox.add(Box.createVerticalStrut(10));

        JButton btnChangePass = UITheme.createPillButton("Cập Nhật Mật Khẩu", UITheme.PRIMARY, Color.WHITE);
        btnChangePass.setFont(UITheme.FONT_SMALL);
        btnChangePass.setAlignmentX(Component.RIGHT_ALIGNMENT);
        btnChangePass.addActionListener(e -> handleChangePassword());
        passBox.add(btnChangePass);

        // 4. Hàng nút Đăng Xuất & Đóng ở dưới cùng
        JPanel actionRow = new JPanel(new BorderLayout(10, 0));
        actionRow.setOpaque(false);
        actionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Nút Đăng Xuất màu đỏ nhạt/nổi
        JButton btnLogout = UITheme.createPillButton("Đăng Xuất", UITheme.DANGER_BG, UITheme.DANGER_TEXT);
        btnLogout.setFont(UITheme.FONT_BOLD);
        btnLogout.addActionListener(e -> handleLogout());

        JButton btnClose = UITheme.createPillButton("Đóng", new Color(241, 245, 249), UITheme.TEXT_MAIN);
        btnClose.setFont(UITheme.FONT_BOLD);
        btnClose.addActionListener(e -> dispose());

        actionRow.add(btnLogout, BorderLayout.WEST);
        actionRow.add(btnClose, BorderLayout.EAST);

        // Ghép toàn bộ vào container
        container.add(headerPanel);
        container.add(Box.createVerticalStrut(16));
        container.add(infoBox);
        container.add(Box.createVerticalStrut(14));
        container.add(passBox);
        container.add(Box.createVerticalGlue());
        container.add(Box.createVerticalStrut(14));
        container.add(actionRow);

        add(container, BorderLayout.CENTER);
    }

    private JPanel createInfoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(UITheme.TEXT_MUTED);

        JLabel val = new JLabel(value);
        val.setFont(UITheme.FONT_BOLD);
        val.setForeground(UITheme.TEXT_MAIN);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    private JPanel createFieldRow(String label, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(UITheme.TEXT_MUTED);
        lbl.setPreferredSize(new Dimension(135, 26));

        panel.add(lbl, BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private void handleChangePassword() {
        String currentPass = new String(txtCurrentPass.getPassword()).trim();
        String newPass = new String(txtNewPass.getPassword()).trim();
        String confirmPass = new String(txtConfirmPass.getPassword()).trim();

        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng điền đầy đủ mật khẩu hiện tại và mật khẩu mới!",
                "Thông Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this,
                "Mật khẩu xác nhận không khớp với mật khẩu mới!",
                "Lỗi Xác Nhận", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (newPass.length() < 6) {
            JOptionPane.showMessageDialog(this,
                "Mật khẩu mới phải có ít nhất 6 ký tự!",
                "Cảnh Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
            "Đổi mật khẩu thành công! Hãy ghi nhớ mật khẩu mới của bạn.",
            "Thành Công", JOptionPane.INFORMATION_MESSAGE);

        txtCurrentPass.setText("");
        txtNewPass.setText("");
        txtConfirmPass.setText("");
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn đăng xuất khỏi phiên làm việc không?",
            "Xác Nhận Đăng Xuất",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            if (parentFrame != null) {
                parentFrame.dispose();
            }
            new LoginForm().setVisible(true);
        }
    }
}
