package com.ximilib.gui.components;

import com.ximilib.presentation.util.IconFactory;
import com.ximilib.presentation.util.UITheme;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;

/**
 * PaginationBar: Thanh phân trang độc lập, tái sử dụng chuẩn theo phong cách ảnh 2 và FlatLaf.
 * Bố cục:
 * - Căn giữa: Nút Trước (<), các nút số trang, dấu "…", trang cuối, nút Sau (>)
 * - Góc phải: Dropdown chọn số dòng trên trang ("10 / trang", "20 / trang", "50 / trang")
 */
public class PaginationBar extends JPanel {

    private int currentPage = 1;
    private int totalPages = 1;
    private int pageSize = 10;
    private int totalElements = 0;

    private Consumer<Integer> onPageChange;
    private Consumer<Integer> onPageSizeChange;

    private final JPanel pagesPanel;
    private final JComboBox<String> cmbPageSize;

    public PaginationBar() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // 1. Cụm nút phân trang ở giữa
        pagesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        pagesPanel.setOpaque(false);

        // 2. Dropdown số mục trên trang ở góc phải
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setOpaque(false);

        String[] sizes = {"10 / trang", "20 / trang", "50 / trang"};
        cmbPageSize = new JComboBox<>(sizes);
        cmbPageSize.setFont(UITheme.FONT_SMALL);
        cmbPageSize.setForeground(UITheme.TEXT_MAIN);
        cmbPageSize.setBackground(UITheme.CARD_BG);
        cmbPageSize.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cmbPageSize.putClientProperty("JComponent.roundRect", true);
        cmbPageSize.setPreferredSize(new Dimension(110, 32));

        cmbPageSize.addActionListener(e -> {
            String selected = (String) cmbPageSize.getSelectedItem();
            if (selected != null && onPageSizeChange != null) {
                int size = 10;
                if (selected.startsWith("20")) size = 20;
                else if (selected.startsWith("50")) size = 50;
                this.pageSize = size;
                onPageSizeChange.accept(size);
            }
        });

        rightPanel.add(cmbPageSize);

        add(pagesPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        rebuildPageButtons();
    }

    public void setOnPageChange(Consumer<Integer> listener) {
        this.onPageChange = listener;
    }

    public void setOnPageSizeChange(Consumer<Integer> listener) {
        this.onPageSizeChange = listener;
    }

    public void updatePagination(int currentPage, int totalPages, int pageSize, int totalElements) {
        this.currentPage = Math.max(1, currentPage);
        this.totalPages = Math.max(1, totalPages);
        this.pageSize = pageSize;
        this.totalElements = totalElements;

        // Cập nhật combobox mà không kích hoạt lại event
        String expectedItem = pageSize + " / trang";
        if (!expectedItem.equals(cmbPageSize.getSelectedItem())) {
            for (int i = 0; i < cmbPageSize.getItemCount(); i++) {
                if (cmbPageSize.getItemAt(i).equals(expectedItem)) {
                    cmbPageSize.setSelectedIndex(i);
                    break;
                }
            }
        }

        rebuildPageButtons();
    }

    private void rebuildPageButtons() {
        pagesPanel.removeAll();

        // Nút Trước (<)
        JButton btnPrev = createNavButton(IconFactory.createChevronLeftIcon(
            currentPage > 1 ? UITheme.TEXT_MAIN : UITheme.TEXT_MUTED, 16), currentPage > 1);
        btnPrev.addActionListener(e -> {
            if (currentPage > 1 && onPageChange != null) {
                onPageChange.accept(currentPage - 1);
            }
        });
        pagesPanel.add(btnPrev);

        // Danh sách số trang rút gọn với "..."
        List<Integer> pageNumbers = computeVisiblePages(currentPage, totalPages);
        int lastP = 0;
        for (int p : pageNumbers) {
            if (lastP != 0 && p - lastP > 1) {
                JLabel lblDots = new JLabel("...", SwingConstants.CENTER);
                lblDots.setFont(UITheme.FONT_BOLD);
                lblDots.setForeground(UITheme.TEXT_MUTED);
                lblDots.setPreferredSize(new Dimension(28, 32));
                pagesPanel.add(lblDots);
            }

            final int pageTarget = p;
            boolean isActive = (p == currentPage);
            JButton btnPage = createPageNumberButton(String.valueOf(p), isActive);
            btnPage.addActionListener(e -> {
                if (pageTarget != currentPage && onPageChange != null) {
                    onPageChange.accept(pageTarget);
                }
            });
            pagesPanel.add(btnPage);
            lastP = p;
        }

        // Nút Sau (>)
        JButton btnNext = createNavButton(IconFactory.createChevronRightIcon(
            currentPage < totalPages ? UITheme.TEXT_MAIN : UITheme.TEXT_MUTED, 16), currentPage < totalPages);
        btnNext.addActionListener(e -> {
            if (currentPage < totalPages && onPageChange != null) {
                onPageChange.accept(currentPage + 1);
            }
        });
        pagesPanel.add(btnNext);

        pagesPanel.revalidate();
        pagesPanel.repaint();
    }

    private List<Integer> computeVisiblePages(int current, int total) {
        List<Integer> list = new ArrayList<>();
        if (total <= 7) {
            for (int i = 1; i <= total; i++) list.add(i);
            return list;
        }

        list.add(1);

        int start = Math.max(2, current - 1);
        int end = Math.min(total - 1, current + 1);

        if (current <= 3) {
            start = 2;
            end = 4;
        } else if (current >= total - 2) {
            start = total - 3;
            end = total - 1;
        }

        for (int i = start; i <= end; i++) {
            if (!list.contains(i)) list.add(i);
        }

        if (!list.contains(total)) list.add(total);
        return list;
    }

    private JButton createPageNumberButton(String text, boolean isActive) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isActive) {
                    g2.setColor(UITheme.PRIMARY);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } else if (getModel().isRollover()) {
                    g2.setColor(UITheme.TINT_LIGHT);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(UITheme.FONT_BOLD);
        btn.setForeground(isActive ? Color.WHITE : UITheme.TEXT_MAIN);
        btn.setPreferredSize(new Dimension(34, 34));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(0, 0, 0, 0));
        return btn;
    }

    private JButton createNavButton(Icon icon, boolean enabled) {
        JButton btn = new JButton(icon) {
            @Override
            protected void paintComponent(Graphics g) {
                if (enabled && getModel().isRollover()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(UITheme.TINT_LIGHT);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        btn.setEnabled(enabled);
        btn.setPreferredSize(new Dimension(34, 34));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(enabled ? new Cursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
        return btn;
    }
}
