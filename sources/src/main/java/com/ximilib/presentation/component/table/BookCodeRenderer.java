package com.ximilib.presentation.component.table;

import com.ximilib.application.dto.BookSummaryDto;
import com.ximilib.gui.util.UITheme;
import java.awt.Component;
import java.awt.Font;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renderer hiển thị Mã sách.
 */
public class BookCodeRenderer extends DefaultTableCellRenderer {

    private final BookTableModel tableModel;
    private final Set<String> selectedBookIds;

    public BookCodeRenderer(BookTableModel tableModel, Set<String> selectedBookIds) {
        this.tableModel = tableModel;
        this.selectedBookIds = selectedBookIds;
        setHorizontalAlignment(SwingConstants.LEFT);
        setFont(new Font("Segoe UI", Font.BOLD, 12));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        lbl.setForeground(UITheme.TEXT_MUTED);
        lbl.setBorder(new EmptyBorder(0, 12, 0, 12));

        BookSummaryDto b = tableModel.getBookAt(row);
        boolean isChecked = (b != null && selectedBookIds.contains(b.getBookId()));
        lbl.setBackground((isChecked || isSelected) ? UITheme.ROW_SELECTED : UITheme.CARD_BG);
        return lbl;
    }
}
