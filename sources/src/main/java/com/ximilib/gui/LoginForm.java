package com.ximilib.gui;

import com.ximilib.gui.util.UITheme;
import com.ximilib.gui.views.LoginView;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import javax.swing.*;

/**
 * LoginForm: Cửa sổ Đăng Nhập XimiLib được nâng cấp toàn diện theo cấu trúc chia đôi 50/50.
 * - Trái: BrandPanel với đồ họa vector xanh ngọc bích và Carousel 3 slide giới thiệu tính năng thư viện.
 * - Phải: LoginFormPanel căn thẳng lề trái, ô mật khẩu có nút hiện/ẩn, lỗi hiển thị inline.
 * - Kích thước chuẩn 960×600 (tối thiểu 840×540), hiển thị mượt mà trên mọi độ phân giải.
 */
public class LoginForm extends JFrame {

    private LoginView loginView;

    public LoginForm() {
        setTitle("XimiLib - Đăng Nhập Hệ Thống Quản Lý Thư Viện");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 600);
        setMinimumSize(new Dimension(840, 540));
        setResizable(true);
        getContentPane().setBackground(UITheme.BG_LIGHT);

        com.ximilib.presentation.util.AppLogo.applyWindowIcons(this);

        com.ximilib.presentation.util.UiModalHelper.initMainFrame(this);

        initUI();
        setLocationRelativeTo(null);

        // Dừng timer khi đóng cửa sổ
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (loginView != null && loginView.getBrandPanel() != null) {
                    loginView.getBrandPanel().stopTimer();
                }
            }
        });
    }

    private void initUI() {
        setLayout(new BorderLayout());
        loginView = new LoginView(this);
        add(loginView, BorderLayout.CENTER);
    }
}
