package com.ximilib.presentation.component;

import com.ximilib.presentation.util.UITheme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * TabBar: Thanh chuyển tab phẳng hiện đại theo phong cách Stripe / Linear.
 * - Gạch chân màu hồng pastel (UITheme.PRIMARY) dày 2px tại tab được chọn.
 * - Tab không chọn hiển thị màu textMuted, hover textMain.
 * - Tối giản, không abstraction thừa.
 */
public class TabBar extends JPanel {

    private final List<String> tabNames;
    private final Consumer<Integer> onTabSelected;
    private final List<TabItem> tabItems = new ArrayList<>();
    private int selectedIndex = 0;

    public TabBar(List<String> tabs, Consumer<Integer> onTabSelected) {
        this.tabNames = (tabs != null) ? tabs : List.of();
        this.onTabSelected = onTabSelected;

        setLayout(new FlowLayout(FlowLayout.LEFT, 24, 0));
        setOpaque(false);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
            BorderFactory.createEmptyBorder(0, 8, 0, 8)
        ));

        initTabs();
    }

    private void initTabs() {
        for (int i = 0; i < tabNames.size(); i++) {
            int index = i;
            TabItem item = new TabItem(tabNames.get(i), i == selectedIndex);
            item.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    setSelectedIndex(index);
                    if (onTabSelected != null) {
                        onTabSelected.accept(index);
                    }
                }
            });
            tabItems.add(item);
            add(item);
        }
    }

    public void setSelectedIndex(int index) {
        if (index < 0 || index >= tabItems.size() || index == selectedIndex) return;
        tabItems.get(selectedIndex).setActive(false);
        this.selectedIndex = index;
        tabItems.get(selectedIndex).setActive(true);
        repaint();
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    private static class TabItem extends JPanel {
        private final String title;
        private boolean active;
        private boolean hover = false;

        public TabItem(String title, boolean active) {
            this.title = title;
            this.active = active;
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(getFontMetrics().stringWidth(title) + 16, 40));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        private FontMetrics getFontMetrics() {
            return getFontMetrics(UITheme.FONT_REGULAR.deriveFont(Font.BOLD, 13f));
        }

        public void setActive(boolean active) {
            this.active = active;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Màu chữ
            if (active) {
                g2.setFont(UITheme.FONT_REGULAR.deriveFont(Font.BOLD, 13f));
                g2.setColor(UITheme.PRIMARY);
            } else if (hover) {
                g2.setFont(UITheme.FONT_REGULAR.deriveFont(13f));
                g2.setColor(UITheme.TEXT_MAIN);
            } else {
                g2.setFont(UITheme.FONT_REGULAR.deriveFont(13f));
                g2.setColor(UITheme.TEXT_MUTED);
            }

            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(title)) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2 - 2;
            g2.drawString(title, x, y);

            // Gạch chân màu hồng khi active
            if (active) {
                g2.setColor(UITheme.PRIMARY);
                g2.fillRect(0, getHeight() - 3, getWidth(), 3);
            }

            g2.dispose();
        }
    }
}
