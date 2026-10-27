package com.ximilib.presentation.component.table;

import com.ximilib.application.dto.BookSummaryDto;
import com.ximilib.gui.util.UITheme;
import java.awt.Color;
import java.awt.Component;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renderer văn bản chuẩn với hiệu ứng đổi màu dòng khi được chọn.
 */
public class DefaultTextRenderer extends DefaultTableCellRenderer {

    private final Color fgColor;
    private final BookTableModel tableModel;
    private final Set<String> selectedBookIds;

    public DefaultTextRenderer(int align, Color fgColor, BookTableModel tableModel, Set<String> selectedBookIds) {
        this.fgColor = fgColor;
        this.tableModel = tableModel;
        this.selectedBookIds = selectedBookIds;
        setHorizontalAlignment(align);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        lbl.setFont(UITheme.FONT_REGULAR);
        lbl.setForeground(fgColor);
        lbl.setBorder(new EmptyBorder(0, 12, 0, 12));

        BookSummaryDto b = tableModel.getBookAt(row);
        boolean isChecked = (b != null && selectedBookIds.contains(b.getBookId()));
        lbl.setBackground((isChecked || isSelected) ? UITheme.ROW_SELECTED : UITheme.CARD_BG);
        return lbl;
    }
}
