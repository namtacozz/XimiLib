package com.ximilib.presentation.component;

import com.ximilib.gui.util.UITheme;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * TabbedViewPanel: Khung chứa gộp các màn hình con liên quan sử dụng TabBar phẳng.
 * Cho phép chuyển nhanh giữa các tiểu màn hình (VD: Đầu sách / Thể loại / Đã xóa).
 */
public class TabbedViewPanel extends JPanel {

    private final TabBar tabBar;
    private final CardLayout cardLayout;
    private final JPanel contentArea;

    public TabbedViewPanel(List<String> tabTitles, List<JComponent> tabPanels) {
        setLayout(new BorderLayout());
        setBackground(UITheme.PAGE_BG);

        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(UITheme.PAGE_BG);

        for (int i = 0; i < tabTitles.size() && i < tabPanels.size(); i++) {
            contentArea.add(tabPanels.get(i), String.valueOf(i));
        }

        tabBar = new TabBar(tabTitles, index -> {
            cardLayout.show(contentArea, String.valueOf(index));
        });

        add(tabBar, BorderLayout.NORTH);
        add(contentArea, BorderLayout.CENTER);
    }

    public void selectTab(int index) {
        tabBar.setSelectedIndex(index);
        cardLayout.show(contentArea, String.valueOf(index));
    }
}
