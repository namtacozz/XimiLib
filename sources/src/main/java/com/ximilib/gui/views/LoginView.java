package com.ximilib.gui.views;

import com.ximilib.gui.components.BrandPanel;
import com.ximilib.gui.components.LoginFormPanel;
import com.ximilib.presentation.util.UITheme;
import java.awt.*;
import javax.swing.*;

/**
 * LoginView: Thành phần view ghép nối 50/50 giữa BrandPanel (trái) và LoginFormPanel (phải).
 * Nằm trên thẻ trắng lớn bo góc 20px nổi bật trên nền BG_LIGHT (#F8FAFC).
 */
public class LoginView extends JPanel {

    private final BrandPanel brandPanel;
    private final LoginFormPanel loginFormPanel;

    public LoginView(Window parentWindow) {
        setOpaque(false);
        setLayout(new GridBagLayout()); // Căn giữa toàn bộ thẻ lớn trên nền ứng dụng

        // 1. Thẻ trắng lớn chia đôi 50/50
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Nền card trắng
                g2.setColor(UITheme.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Viền xám mảnh
                g2.setColor(UITheme.BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new GridLayout(1, 2, 0, 0)); // Bố cục chia đôi đúng 50/50
        card.setPreferredSize(new Dimension(880, 540));
        card.setMinimumSize(new Dimension(800, 500));

        brandPanel = new BrandPanel();
        loginFormPanel = new LoginFormPanel(parentWindow);

        card.add(brandPanel);
        card.add(loginFormPanel);

        add(card);
    }

    public BrandPanel getBrandPanel() {
        return brandPanel;
    }

    public LoginFormPanel getLoginFormPanel() {
        return loginFormPanel;
    }
}
