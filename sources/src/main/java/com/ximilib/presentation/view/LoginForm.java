package com.ximilib.presentation.view;

import com.ximilib.presentation.component.BrandPanel;
import com.ximilib.presentation.component.LoginFormPanel;
import com.ximilib.presentation.component.SectionCard;
import com.ximilib.presentation.util.AppLogo;
import com.ximilib.presentation.util.UITheme;
import com.ximilib.presentation.util.UiModalHelper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Cửa sổ đăng nhập chính của XimiLib (gộp LoginForm + LoginView).
 */
public class LoginForm extends JFrame {

    private BrandPanel brandPanel;
    private LoginFormPanel loginFormPanel;

    public LoginForm() {
        setTitle("XimiLib - Đăng Nhập Hệ Thống Quản Lý Thư Viện");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 600);
        setMinimumSize(new Dimension(840, 540));
        setResizable(true);
        getContentPane().setBackground(UITheme.BG_LIGHT);

        AppLogo.applyWindowIcons(this);
        UiModalHelper.initMainFrame(this);

        initUI();
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (brandPanel != null) {
                    brandPanel.stopTimer();
                }
            }
        });
    }

    private void initUI() {
        setLayout(new BorderLayout());

        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setOpaque(false);

        SectionCard card = new SectionCard(new GridLayout(1, 2, 0, 0), new Insets(0, 0, 0, 0), 20, UITheme.CARD_BG, UITheme.BORDER);
        card.setPreferredSize(new Dimension(880, 540));
        card.setMinimumSize(new Dimension(800, 500));

        brandPanel = new BrandPanel();
        loginFormPanel = new LoginFormPanel(this);

        card.add(brandPanel);
        card.add(loginFormPanel);

        centerContainer.add(card);
        add(centerContainer, BorderLayout.CENTER);
    }

    public BrandPanel getBrandPanel() {
        return brandPanel;
    }

    public LoginFormPanel getLoginFormPanel() {
        return loginFormPanel;
    }
}
