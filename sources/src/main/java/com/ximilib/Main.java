package com.ximilib;

import com.ximilib.gui.LoginForm;
import com.ximilib.gui.util.UITheme;
import javax.swing.SwingUtilities;

/**
 * Main: Điểm khởi chạy của ứng dụng Quản Lý Thư Viện XimiLib.
 * Tác giả: Phan Vũ Xuân Kiên (GUI / DB)
 */
public class Main {
    public static void main(String[] args) {
        // Thiết lập phong cách phẳng hiện đại FlatLaf
        UITheme.setupLookAndFeel();

        // Khởi động giao diện trên luồng giao diện người dùng Swing (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginForm loginForm = new LoginForm();
            loginForm.setVisible(true);
        });
    }
}
