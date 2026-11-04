package com.ximilib.presentation.view;

import com.ximilib.presentation.util.UITheme;
import com.ximilib.presentation.view.LoginView;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import javax.swing.*;

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
