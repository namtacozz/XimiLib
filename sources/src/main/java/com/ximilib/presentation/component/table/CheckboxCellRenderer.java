package com.ximilib.presentation.component.table;

import com.ximilib.application.dto.BookSummaryDto;
import com.ximilib.presentation.util.UITheme;
import java.awt.Component;
import java.util.Set;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

/**
 * Checkbox Renderer cột đầu: Nổi bật xanh khi dòng được chọn.
 */
public class CheckboxCellRenderer extends JCheckBox implements TableCellRenderer {

    private final BookTableModel tableModel;
    private final Set<String> selectedBookIds;

    public CheckboxCellRenderer(BookTableModel tableModel, Set<String> selectedBookIds) {
        this.tableModel = tableModel;
        this.selectedBookIds = selectedBookIds;
        setHorizontalAlignment(SwingConstants.CENTER);
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        BookSummaryDto b = tableModel.getBookAt(row);
        boolean isChecked = (b != null && selectedBookIds.contains(b.getBookId()));
        setSelected(isChecked);

        if (isChecked || isSelected) {
            setBackground(UITheme.ROW_SELECTED);
        } else {
            setBackground(UITheme.CARD_BG);
        }
        return this;
    }
}
