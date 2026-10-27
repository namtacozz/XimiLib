package com.ximilib.presentation.component.table;

import com.ximilib.application.dto.BookSummaryDto;
import com.ximilib.gui.util.IconFactory;
import com.ximilib.gui.util.UITheme;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.table.TableCellEditor;

/**
 * CellEditor cho cột Thao tác (kích hoạt click nút Xóa / Sửa).
 */
public class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {

    private final JPanel panel;
    private final BookTableModel tableModel;
    private BookSummaryDto currentBook;

    public ActionCellEditor(BookTableModel tableModel,
                            Consumer<BookSummaryDto> onDelete,
                            Consumer<BookSummaryDto> onEdit) {
        this.tableModel = tableModel;

        panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 12));
        panel.setOpaque(true);

        JButton btnDelete = new JButton(IconFactory.createTrashIcon(UITheme.DANGER_TEXT, 16));
        btnDelete.setContentAreaFilled(false);
        btnDelete.setBorderPainted(false);
        btnDelete.setFocusPainted(false);
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDelete.setToolTipText("Xóa đầu sách (Xóa mềm)");
        btnDelete.addActionListener(e -> {
            fireEditingStopped();
            if (onDelete != null && currentBook != null) {
                onDelete.accept(currentBook);
            }
        });

        JButton btnEdit = new JButton(IconFactory.createEditIcon(UITheme.PRIMARY, 16));
        btnEdit.setContentAreaFilled(false);
        btnEdit.setBorderPainted(false);
        btnEdit.setFocusPainted(false);
        btnEdit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEdit.setToolTipText("Chỉnh sửa thông tin");
        btnEdit.addActionListener(e -> {
            fireEditingStopped();
            if (onEdit != null && currentBook != null) {
                onEdit.accept(currentBook);
            }
        });

        panel.add(btnDelete);
        panel.add(btnEdit);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        this.currentBook = (value instanceof BookSummaryDto b) ? b : tableModel.getBookAt(row);
        panel.setBackground(UITheme.ROW_SELECTED);
        return panel;
    }

    @Override
    public Object getCellEditorValue() {
        return currentBook;
    }
}
