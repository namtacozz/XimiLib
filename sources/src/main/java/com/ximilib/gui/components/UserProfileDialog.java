package com.ximilib.gui.components;

import com.ximilib.gui.util.UserSession;
import com.ximilib.domain.model.User;
import com.ximilib.presentation.component.UserProfileModalPanel;
import javax.swing.JFrame;

/**
 * UserProfileDialog: Adapter hien thi Thong tin tai khoan & Doi mat khau
 * bang ModalDialog (SimpleModalBorder In-App Overlay).
 * Triet tieu hoan toan OS JDialog truyen thong theo dung quy chuan XimiLib.
 * Tac gia: Phan Vu Xuan Kien
 */
public class UserProfileDialog {

    private final JFrame parentFrame;
    private final User currentUser;

    public UserProfileDialog(JFrame parent) {
        this(parent, UserSession.getCurrentUser());
    }

    public UserProfileDialog(JFrame parent, User user) {
        this.parentFrame = parent;
        this.currentUser = (user != null) ? user : UserSession.getCurrentUser();
    }

    public static void show(JFrame parent, User user) {
        UserProfileModalPanel.show(parent, user);
    }

    public void setVisible(boolean visible) {
        if (visible && parentFrame != null) {
            show(parentFrame, currentUser);
        }
    }
}
