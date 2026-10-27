package com.ximilib;

import com.ximilib.gui.LoginForm;
import com.ximilib.gui.util.UITheme;
import java.awt.Rectangle;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main: Điểm khởi chạy của ứng dụng Quản Lý Thư Viện XimiLib.
 * Tác giả: Phan Vũ Xuân Kiên (GUI / DB)
 */
public class Main {

    static {
        // Tắt tăng tốc Direct3D để chống lỗi bề mặt tàng hình / không hiển thị trên Windows DWM
        System.setProperty("sun.java2d.d3d", "false");
        System.setProperty("sun.java2d.noddraw", "true");
        // Dùng viền cửa sổ chuẩn của hệ điều hành Windows để hiển thị 100% trên Taskbar và Task View
        System.setProperty("flatlaf.useWindowDecorations", "false");
    }

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  XimiLib - HỆ THỐNG QUẢN LÝ THƯ VIỆN HIỆN ĐẠI");
        System.out.println("  Giao diện đồ họa chuẩn phong cách LibRack");
        System.out.println("=================================================");

        // Thiết lập phong cách phẳng hiện đại FlatLaf
        UITheme.setupLookAndFeel();

        // Khởi động các tác vụ nền (quét quá hạn phiếu mượn, giải phóng đặt chỗ)
        com.ximilib.infrastructure.jobs.BackgroundJobManager.getInstance().start();

        // Khởi động giao diện trên luồng giao diện người dùng Swing (EDT)
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
