package com.ximilib.presentation.component;

import com.ximilib.presentation.util.UserSession;
import com.ximilib.domain.model.User;
import com.ximilib.presentation.component.UserProfileModalPanel;
import javax.swing.JFrame;

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
