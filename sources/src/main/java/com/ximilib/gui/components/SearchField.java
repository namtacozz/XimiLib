package com.ximilib.gui.components;

import com.ximilib.gui.util.IconFactory;
import com.ximilib.gui.util.UITheme;
import java.awt.*;
import javax.swing.*;

/**
 * SearchField: Thanh tìm kiếm thông minh bo tròn với icon kính lúp.
 * Sửa triệt để lỗi chữ bị nhân đôi (bóng ma / double placeholder).
 * Tác giả: Phan Vũ Xuân Kiên (GUI / DB)
 */
public class SearchField extends JTextField {

    private final String placeholder;
    private final boolean isFlatLaf;

    public SearchField(String placeholder) {
        this.placeholder = placeholder;
        this.isFlatLaf = UIManager.getLookAndFeel().getClass().getName().contains("Flat");

        setFont(UITheme.FONT_REGULAR);
        setForeground(UITheme.TEXT_MAIN);
        setBackground(UITheme.BG_LIGHT);
        setCaretColor(UITheme.TEXT_MAIN);

        // Đăng ký các thuộc tính chuẩn của FlatLaf
        putClientProperty("JTextField.placeholderText", placeholder);
        putClientProperty("JTextField.showClearButton", true);
        putClientProperty("JComponent.roundRect", true);

        // Thêm icon kính lúp thông qua FlatLaf leadingIcon
        Icon searchIcon = IconFactory.createSearchIcon(new Color(148, 163, 184), 16);
        putClientProperty("JTextField.leadingIcon", searchIcon);

        if (isFlatLaf) {
            // Khi có FlatLaf, FlatLaf tự quản lý padding và placeholder chuẩn không bị bóng ma
            setBorder(new UITheme.RoundedBorder(UITheme.BORDER, 20));
        } else {
            // Trường hợp chạy Swing cơ bản không có FlatLaf
            setBorder(BorderFactory.createCompoundBorder(
                new UITheme.RoundedBorder(UITheme.BORDER, 20),
                BorderFactory.createEmptyBorder(6, 36, 6, 14)
            ));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Chỉ tự vẽ thủ công khi KHÔNG dùng FlatLaf để tránh lỗi chữ bị in đè 2 lần
        if (!isFlatLaf) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 1. Vẽ icon kính lúp
            g2.setColor(new Color(148, 163, 184));
            g2.setStroke(new BasicStroke(1.8f));
            int cy = getHeight() / 2;
            int r = 5;
            g2.drawOval(14, cy - r - 1, r * 2, r * 2);
            g2.drawLine(14 + r * 2 - 1, cy + r - 2, 14 + r * 2 + 5, cy + r + 4);

            // 2. Vẽ Placeholder khi ô rỗng
            if (getText() == null || getText().isEmpty()) {
                g2.setFont(getFont());
                g2.setColor(new Color(148, 163, 184));
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(placeholder, 36, y);
            }
            g2.dispose();
        }
    }
}
