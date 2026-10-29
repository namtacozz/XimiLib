package com.ximilib.presentation.component.table;

import com.ximilib.application.dto.BookSummaryDto;
import com.ximilib.presentation.util.IconFactory;
import com.ximilib.presentation.util.UITheme;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Renderer cột "Thao tác" (Xóa, Sửa).
 */
public class ActionCellRenderer extends JPanel implements TableCellRenderer {

    private final JLabel btnDelete;
    private final JLabel btnEdit;
    private final BookTableModel tableModel;
    private final Set<String> selectedBookIds;

    public ActionCellRenderer(BookTableModel tableModel, Set<String> selectedBookIds) {
        this.tableModel = tableModel;
        this.selectedBookIds = selectedBookIds;

        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 14));
        setOpaque(true);

        btnDelete = new JLabel(IconFactory.createTrashIcon(UITheme.TEXT_MUTED, 16));
        btnDelete.setToolTipText("Xóa đầu sách (Xóa mềm)");

        btnEdit = new JLabel(IconFactory.createEditIcon(UITheme.TEXT_MUTED, 16));
        btnEdit.setToolTipText("Chỉnh sửa thông tin đầu sách");

        add(btnDelete);
        add(btnEdit);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        BookSummaryDto b = tableModel.getBookAt(row);
        boolean isChecked = (b != null && selectedBookIds.contains(b.getBookId()));
        setBackground((isChecked || isSelected) ? UITheme.ROW_SELECTED : UITheme.CARD_BG);
        return this;
    }
}
