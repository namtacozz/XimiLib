package com.ximilib;

import com.ximilib.infrastructure.config.AppContainer;
import com.ximilib.infrastructure.persistence.DatabaseInitializer;
import com.ximilib.presentation.view.LoginForm;
import com.ximilib.presentation.util.UITheme;
import java.awt.Rectangle;
import java.util.Arrays;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    static {
        System.setProperty("sun.java2d.d3d", "false");
        System.setProperty("sun.java2d.noddraw", "true");
        System.setProperty("flatlaf.useWindowDecorations", "false");
    }

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  XimiLib - HỆ THỐNG QUẢN LÝ THƯ VIỆN HIỆN ĐẠI");
        System.out.println("  Giao diện đồ họa chuẩn phong cách LibRack");
        System.out.println("=================================================");

        if (Arrays.asList(args).contains("--init-db")) {
            System.out.println("[INFO] Chế độ khởi tạo CSDL (--init-db). Đang chạy migration...");
            try {
                AppContainer container = AppContainer.getInstance();
                DatabaseInitializer initializer = new DatabaseInitializer(container.getConnectionManager());
                initializer.initialize();
                System.out.println("[OK] Khởi tạo CSDL hoàn tất.");
            } catch (Exception e) {
                System.err.println("[LỖI] Khởi tạo CSDL thất bại: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
            System.exit(0);
            return;
        }

        UITheme.setupLookAndFeel();

        com.ximilib.infrastructure.jobs.BackgroundJobManager.getInstance().start();

        SwingUtilities.invokeLater(() -> {
            try {
                LoginForm loginForm = new LoginForm();
                loginForm.setVisible(true);
                loginForm.toFront();
                loginForm.requestFocus();

                Rectangle bounds = loginForm.getBounds();
                System.out.println("[OK] Cửa sổ Đăng Nhập đã mở tại tọa độ: " + bounds);
                System.out.println("[OK] Look and Feel đang dùng: " + UIManager.getLookAndFeel().getName());
            } catch (Throwable t) {
                System.err.println("[LỖI KHỞI ĐỘNG GIAO DIỆN] " + t.getMessage());
                t.printStackTrace();
            }
        });
    }
}
