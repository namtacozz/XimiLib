package com.ximilib.gui.views;

import com.ximilib.application.service.BookCatalogService;
import com.ximilib.presentation.util.IconFactory;
import com.ximilib.presentation.util.UITheme;
import com.ximilib.infrastructure.config.AppContainer;
import com.ximilib.presentation.util.UiModalHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CategoryManagementView: Màn hình Quản Lý Thể Loại Sách (UC-3).
 * Cho phép xem, thêm mới, sửa và xóa thể loại.
 * Tuân thủ R-01, R-06, R-08, R-11.
 */
public class CategoryManagementView extends JPanel {

    private final BookCatalogService bookCatalogService;
    private final CategoryTableModel tableModel = new CategoryTableModel();
    private JTable categoryTable;

    public CategoryManagementView() {
        this(AppContainer.getInstance().getBookCatalogService());
    }

    public CategoryManagementView(BookCatalogService service) {
        this.bookCatalogService = service != null ? service : AppContainer.getInstance().getBookCatalogService();

        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 28, 28, 28));

        initUI();
        loadCategoriesAsync();
    }

    private void initUI() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Quản lý thể loại sách");
        lblTitle.setFont(UITheme.FONT_TITLE_LARGE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);

        JLabel lblSub = new JLabel("Danh mục các phân loại sách phục vụ việc sắp xếp và tra cứu trong kho");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.TEXT_MUTED);

        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(lblSub);

        JButton btnAdd = UITheme.createPillButton("+ Thêm thể loại", UITheme.PRIMARY, Color.WHITE);
        btnAdd.setFont(UITheme.FONT_BOLD);
        btnAdd.setPreferredSize(new Dimension(150, 38));
        btnAdd.addActionListener(e -> showAddCategoryDialog());

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(btnAdd, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Card bảng
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(UITheme.BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        categoryTable = new JTable(tableModel);
        categoryTable.setRowHeight(44);
        categoryTable.setShowHorizontalLines(true);
        categoryTable.setShowVerticalLines(false);
        categoryTable.setGridColor(UITheme.BORDER);
        categoryTable.setBackground(UITheme.CARD_BG);
        categoryTable.setFont(UITheme.FONT_REGULAR);
        categoryTable.getTableHeader().setPreferredSize(new Dimension(0, 38));
        categoryTable.getTableHeader().setFont(UITheme.FONT_BOLD);
        categoryTable.getTableHeader().setBackground(UITheme.PRIMARY_TINT);
        categoryTable.getTableHeader().setForeground(UITheme.TEXT_MUTED);

        categoryTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        categoryTable.getColumnModel().getColumn(0).setMaxWidth(100);
        categoryTable.getColumnModel().getColumn(1).setPreferredWidth(350);
        categoryTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        categoryTable.getColumnModel().getColumn(2).setMaxWidth(160);

        ActionCell actionCell = new ActionCell();
        categoryTable.getColumnModel().getColumn(2).setCellRenderer(actionCell);
        categoryTable.getColumnModel().getColumn(2).setCellEditor(actionCell);

        JScrollPane scroll = new JScrollPane(categoryTable);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UITheme.CARD_BG);

        card.add(scroll, BorderLayout.CENTER);

        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        main.add(card, BorderLayout.CENTER);

        add(main, BorderLayout.CENTER);
    }

    private void loadCategoriesAsync() {
        SwingWorker<Map<Integer, String>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<Integer, String> doInBackground() {
                return bookCatalogService.getCategories();
            }

            @Override
            protected void done() {
                try {
                    Map<Integer, String> map = get();
                    SwingUtilities.invokeLater(() -> tableModel.setCategories(map));
                } catch (Exception ex) {
                    UiModalHelper.showToastError(CategoryManagementView.this, "Lỗi tải thể loại: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void showAddCategoryDialog() {
        JTextField txtName = new JTextField();
        txtName.setFont(UITheme.FONT_REGULAR);
        txtName.setPreferredSize(new Dimension(280, 36));

        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setPreferredSize(new Dimension(300, 60));
        p.add(new JLabel("Tên thể loại mới:"), BorderLayout.NORTH);
        p.add(txtName, BorderLayout.CENTER);

        UiModalHelper.showCustomModal(
            this,
            "ADD_CAT",
            "Thêm thể loại sách",
            p,
            raven.modal.component.SimpleModalBorder.OK_CANCEL_OPTION,
            (controller, action) -> {
                if (action == raven.modal.component.SimpleModalBorder.OK_OPTION) {
                    String name = txtName.getText().trim();
                    if (!name.isEmpty()) {
                        controller.close();
                        bookCatalogService.addCategory(name, "");
                        loadCategoriesAsync();
                        UiModalHelper.showToastSuccess(this, "Đã thêm thể loại: " + name);
                    }
                } else if (action == raven.modal.component.SimpleModalBorder.CANCEL_OPTION || action == raven.modal.component.SimpleModalBorder.CLOSE_OPTION) {
                    controller.close();
                }
            }
        );
    }

    private void showEditCategoryDialog(int id, String currentName) {
        JTextField txtName = new JTextField(currentName);
        txtName.setFont(UITheme.FONT_REGULAR);
        txtName.setPreferredSize(new Dimension(280, 36));

        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setPreferredSize(new Dimension(300, 60));
        p.add(new JLabel("Tên thể loại:"), BorderLayout.NORTH);
        p.add(txtName, BorderLayout.CENTER);

        UiModalHelper.showCustomModal(
            this,
            "EDIT_CAT",
            "Sửa thể loại #" + id,
            p,
            raven.modal.component.SimpleModalBorder.OK_CANCEL_OPTION,
            (controller, action) -> {
                if (action == raven.modal.component.SimpleModalBorder.OK_OPTION) {
                    String name = txtName.getText().trim();
                    if (!name.isEmpty()) {
                        controller.close();
                        bookCatalogService.updateCategory(id, name, "");
                        loadCategoriesAsync();
                        UiModalHelper.showToastSuccess(this, "Đã cập nhật thể loại.");
                    }
                } else if (action == raven.modal.component.SimpleModalBorder.CANCEL_OPTION || action == raven.modal.component.SimpleModalBorder.CLOSE_OPTION) {
                    controller.close();
                }
            }
        );
    }

    private void handleDeleteCategory(int id, String name) {
        UiModalHelper.showConfirm(this, "Xác nhận xóa", "Bạn có chắc muốn xóa thể loại \"" + name + "\"?", () -> {
            try {
                bookCatalogService.deleteCategory(id);
                loadCategoriesAsync();
                UiModalHelper.showToastSuccess(this, "Đã xóa thể loại.");
            } catch (Exception e) {
                UiModalHelper.showToastError(this, "Không thể xóa thể loại đang có sách.");
            }
        });
    }

    private static class CategoryEntry {
        final int id;
        final String name;
        CategoryEntry(int id, String name) { this.id = id; this.name = name; }
    }

    private class CategoryTableModel extends AbstractTableModel {
        private final String[] cols = {"Mã", "Tên thể loại", "Thao tác"};
        private final List<CategoryEntry> entries = new ArrayList<>();

        public void setCategories(Map<Integer, String> map) {
            entries.clear();
            if (map != null) {
                map.forEach((k, v) -> entries.add(new CategoryEntry(k, v)));
            }
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return entries.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public boolean isCellEditable(int r, int c) { return c == 2; }

        @Override
        public Object getValueAt(int r, int c) {
            CategoryEntry e = entries.get(r);
            return switch (c) {
                case 0 -> e.id;
                case 1 -> e.name;
                case 2 -> e;
                default -> null;
            };
        }
    }

    private class ActionCell extends AbstractCellEditor implements javax.swing.table.TableCellRenderer, javax.swing.table.TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        private final JButton btnEdit = new JButton(IconFactory.createEditIcon(UITheme.PRIMARY, 16));
        private final JButton btnDel = new JButton(IconFactory.createTrashIcon(UITheme.DANGER_TEXT, 16));
        private CategoryEntry currentEntry;

        ActionCell() {
            panel.setOpaque(false);
            styleButton(btnEdit);
            styleButton(btnDel);

            btnEdit.addActionListener(e -> {
                fireEditingStopped();
                if (currentEntry != null) showEditCategoryDialog(currentEntry.id, currentEntry.name);
            });
            btnDel.addActionListener(e -> {
                fireEditingStopped();
                if (currentEntry != null) handleDeleteCategory(currentEntry.id, currentEntry.name);
            });

            panel.add(btnEdit);
            panel.add(btnDel);
        }

        private void styleButton(JButton b) {
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
            b.setFocusPainted(false);
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) { return panel; }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            if (v instanceof CategoryEntry ce) currentEntry = ce;
            return panel;
        }
        @Override public Object getCellEditorValue() { return currentEntry; }
    }
}
