package com.ximilib.presentation.component.table;

import com.ximilib.application.dto.BookSummaryDto;
import com.ximilib.presentation.util.UITheme;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

public class DefaultTextRenderer extends DefaultTableCellRenderer {

    private final Color fgColor;
    private final Font customFont;
    private final BookTableModel tableModel;
    private final Set<String> selectedBookIds;

    public DefaultTextRenderer(int align, Color fgColor, BookTableModel tableModel, Set<String> selectedBookIds) {
        this(align, fgColor, null, tableModel, selectedBookIds);
    }

    public DefaultTextRenderer(int align, Color fgColor, Font font, BookTableModel tableModel, Set<String> selectedBookIds) {
        this.fgColor = fgColor;
        this.customFont = font;
        this.tableModel = tableModel;
        this.selectedBookIds = selectedBookIds;
        setHorizontalAlignment(align);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        lbl.setFont(customFont != null ? customFont : UITheme.FONT_REGULAR);
        lbl.setForeground(fgColor != null ? fgColor : UITheme.TEXT_MUTED);
        lbl.setBorder(new EmptyBorder(0, 12, 0, 12));

        BookSummaryDto b = tableModel.getBookAt(row);
        boolean isChecked = (b != null && selectedBookIds.contains(b.getBookId()));
        lbl.setBackground((isChecked || isSelected) ? UITheme.ROW_SELECTED : UITheme.CARD_BG);
        return lbl;
    }
}
