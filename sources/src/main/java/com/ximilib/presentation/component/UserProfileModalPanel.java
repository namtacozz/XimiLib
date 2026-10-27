package com.ximilib.presentation.component;

import com.ximilib.infrastructure.config.AppContainer;
import com.ximilib.gui.LoginForm;
import com.ximilib.gui.util.UITheme;
import com.ximilib.gui.util.UserSession;
import com.ximilib.infrastructure.security.SecurityUtil;
import com.ximilib.models.User;
import com.ximilib.presentation.util.UiModalHelper;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import raven.popup.component.GlassPaneChild;

/**
 * UserProfileModalPanel: Popup thong tin tai khoan & doi mat khau
 * ke thua GlassPaneChild (In-App Overlay chuan GlassPanePopup).
 * Khac phuc triet de hien tuong tat ngay khi click tren thanh Header.
 */
public class UserProfileModalPanel extends GlassPaneChild {

    private final JFrame parentFrame;
    private final User currentUser;
    private final String fullName;
    private final String email;
    private final String phone;
    private final String role;

    private JPasswordField txtCurrentPass;
    private JPasswordField txtNewPass;
    private JPasswordField txtConfirmPass;

    public UserProfileModalPanel(JFrame parent, User user) {
        this.parentFrame = parent;
        this.currentUser = (user != null) ? user : UserSession.getCurrentUser();
        this.fullName = (currentUser != null && currentUser.getFullName() != null) ? currentUser.getFullName() : "Người dùng";
        this.email = (currentUser != null && currentUser.getEmail() != null) ? currentUser.getEmail() : "Chưa cập nhật";
        this.phone = (currentUser != null && currentUser.getPhone() != null) ? currentUser.getPhone() : "Chưa cập nhật";
        this.role = (currentUser != null && currentUser.getRole() != null) ? currentUser.getRole() : "Độc Giả";

        setLayout(new BorderLayout());
        setOpaque(false);
        initUI();
    }

    public static void show(JFrame parent, User user) {
        UiModalHelper.showQuickPopup(new UserProfileModalPanel(parent, user));
    }

    private void initUI() {
        JPanel container = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(UITheme.BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));
        container.setPreferredSize(new Dimension(380, 560));

        // 0. Top Bar của Popup: Tiêu đề + Nút Đóng '✕'
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);
        topHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel lblHeaderTitle = new JLabel("Thông Tin Tài Khoản");
        lblHeaderTitle.setFont(UITheme.FONT_BOLD);
        lblHeaderTitle.setForeground(UITheme.TEXT_MAIN);

        JButton btnCloseX = new JButton("✕");
        btnCloseX.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCloseX.setForeground(UITheme.TEXT_MUTED);
        btnCloseX.setContentAreaFilled(false);
        btnCloseX.setBorderPainted(false);
        btnCloseX.setFocusPainted(false);
        btnCloseX.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCloseX.addActionListener(e -> UiModalHelper.closePopup());

        topHeader.add(lblHeaderTitle, BorderLayout.WEST);
        topHeader.add(btnCloseX, BorderLayout.EAST);

        // 1. Thẻ Avatar & Thông tin cơ bản
        JPanel profileHeader = new JPanel();
        profileHeader.setOpaque(false);
        profileHeader.setLayout(new BoxLayout(profileHeader, BoxLayout.Y_AXIS));

        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
                String initial = (!fullName.isBlank()) ? String.valueOf(fullName.charAt(0)).toUpperCase() : "U";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initial, (getWidth() - fm.stringWidth(initial)) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(56, 56));
        avatar.setMaximumSize(new Dimension(56, 56));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblName = new JLabel(fullName);
        lblName.setFont(UITheme.FONT_TITLE);
        lblName.setForeground(UITheme.TEXT_MAIN);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblRoleBadge = new JLabel(" " + role + " ");
        lblRoleBadge.setFont(UITheme.FONT_BADGE);
        lblRoleBadge.setOpaque(true);
        lblRoleBadge.setBackground(UITheme.TINT);
        lblRoleBadge.setForeground(UITheme.ON_TINT);
        lblRoleBadge.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        lblRoleBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblContact = new JLabel(email + " • " + phone);
        lblContact.setFont(UITheme.FONT_SMALL);
        lblContact.setForeground(UITheme.TEXT_MUTED);
        lblContact.setAlignmentX(Component.CENTER_ALIGNMENT);

        profileHeader.add(avatar);
        profileHeader.add(Box.createVerticalStrut(6));
        profileHeader.add(lblName);
        profileHeader.add(Box.createVerticalStrut(3));
        profileHeader.add(lblRoleBadge);
        profileHeader.add(Box.createVerticalStrut(4));
        profileHeader.add(lblContact);

        // 2. Nút xem Thẻ Thư Viện Điện Tử (Nếu là Độc Giả)
        boolean isReader = (currentUser != null && currentUser.isReader());
        JButton btnViewCard = null;
        if (isReader) {
            btnViewCard = UITheme.createPillButton("💳 Xem Thẻ Điện Tử & Mã QR", UITheme.PRIMARY, Color.WHITE);
            btnViewCard.setFont(UITheme.FONT_SMALL);
            btnViewCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            btnViewCard.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnViewCard.addActionListener(e -> {
                String cardId = (currentUser != null && currentUser.getCardNumber() != null && !currentUser.getCardNumber().isBlank())
                        ? currentUser.getCardNumber() : ((currentUser != null && currentUser.getId() != null) ? currentUser.getId() : "ND001");
                UiModalHelper.showQuickPopup(new DigitalCardPopupPanel(cardId, fullName));
            });
        }

        // 3. Form Đổi Mật Khẩu
        JPanel passCard = new JPanel();
        passCard.setOpaque(false);
        passCard.setLayout(new BoxLayout(passCard, BoxLayout.Y_AXIS));
        passCard.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1),
            "Đổi Mật Khẩu Tài Khoản",
            0, 0, UITheme.FONT_BOLD, UITheme.TEXT_MAIN
        ));

        txtCurrentPass = createPasswordField();
        txtNewPass = createPasswordField();
        txtConfirmPass = createPasswordField();

        passCard.add(createFieldLabel("Mật khẩu hiện tại:"));
        passCard.add(txtCurrentPass);
        passCard.add(Box.createVerticalStrut(4));
        passCard.add(createFieldLabel("Mật khẩu mới (tối thiểu 6 ký tự):"));
        passCard.add(txtNewPass);
        passCard.add(Box.createVerticalStrut(4));
        passCard.add(createFieldLabel("Xác nhận mật khẩu mới:"));
        passCard.add(txtConfirmPass);
        passCard.add(Box.createVerticalStrut(8));

        JButton btnChangePass = UITheme.createPillButton("Cập Nhật Mật Khẩu", UITheme.TINT, UITheme.ON_TINT);
        btnChangePass.setFont(UITheme.FONT_SMALL);
        btnChangePass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        btnChangePass.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnChangePass.addActionListener(e -> handleChangePassword());
        passCard.add(btnChangePass);

        // 4. Hàng nút Thao tác dưới cùng: Đăng xuất & Đóng
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 10, 0));
        bottomRow.setOpaque(false);
        bottomRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JButton btnLogout = UITheme.createPillButton("Đăng Xuất", UITheme.DANGER_BG, UITheme.DANGER_TEXT);
        btnLogout.setFont(UITheme.FONT_SMALL);
        btnLogout.addActionListener(e -> handleLogout());

        JButton btnClose = UITheme.createPillButton("Đóng", UITheme.TINT_LIGHT, UITheme.TEXT_MAIN);
        btnClose.setFont(UITheme.FONT_SMALL);
        btnClose.addActionListener(e -> UiModalHelper.closePopup());

        bottomRow.add(btnLogout);
        bottomRow.add(btnClose);

        // Ghép các thành phần vào Container
        container.add(topHeader);
        container.add(Box.createVerticalStrut(10));
        container.add(profileHeader);
        if (btnViewCard != null) {
            container.add(Box.createVerticalStrut(8));
            container.add(btnViewCard);
        }
        container.add(Box.createVerticalStrut(10));
        container.add(passCard);
        container.add(Box.createVerticalStrut(12));
        container.add(bottomRow);

        add(container, BorderLayout.CENTER);
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(UITheme.TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JPasswordField createPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(UITheme.FONT_REGULAR);
        pf.setBorder(new UITheme.RoundedBorder(UITheme.BORDER, 8));
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return pf;
    }

    private void handleChangePassword() {
        String currentPass = new String(txtCurrentPass.getPassword()).trim();
        String newPass = new String(txtNewPass.getPassword()).trim();
        String confirmPass = new String(txtConfirmPass.getPassword()).trim();

        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            UiModalHelper.showToastWarning(this, "Vui lòng điền đầy đủ mật khẩu hiện tại và mật khẩu mới!");
            return;
        }

        if (currentUser == null) {
            UiModalHelper.showToastError(this, "Không tìm thấy phiên làm việc của người dùng hiện tại!");
            return;
        }

        boolean isCurrentMatch = SecurityUtil.checkPassword(currentPass, currentUser.getPassword())
                || currentPass.equals(currentUser.getPassword());
        if (!isCurrentMatch) {
            UiModalHelper.showToastError(this, "Mật khẩu hiện tại không chính xác! Vui lòng thử lại.");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            UiModalHelper.showToastError(this, "Mật khẩu xác nhận không khớp với mật khẩu mới!");
            return;
        }

        if (newPass.length() < 6) {
            UiModalHelper.showToastWarning(this, "Mật khẩu mới phải có ít nhất 6 ký tự!");
            return;
        }

        try {
            AppContainer.getInstance().getAuthenticationService().resetUserPassword(currentUser.getId(), newPass);
            currentUser.setPassword(SecurityUtil.hashPassword(newPass));
            UiModalHelper.showToastSuccess(this, "Đổi mật khẩu thành công! Hãy ghi nhớ mật khẩu mới.");
            txtCurrentPass.setText("");
            txtNewPass.setText("");
            txtConfirmPass.setText("");
        } catch (Exception ex) {
            UiModalHelper.showToastError(this, "Lỗi khi cập nhật mật khẩu: " + ex.getMessage());
        }
    }

    private void handleLogout() {
        UiModalHelper.closePopup();
        UiModalHelper.showConfirm(
            parentFrame,
            "Xác Nhận Đăng Xuất",
            "Bạn có chắc chắn muốn đăng xuất khỏi phiên làm việc hiện tại?",
            () -> {
                UserSession.setCurrentUser(null);
                if (parentFrame != null) {
                    parentFrame.dispose();
                }
                new LoginForm().setVisible(true);
            }
        );
    }
}
