package com.ximilib.presentation.component.table;

import com.ximilib.application.dto.BookSummaryDto;
import com.ximilib.presentation.util.UITheme;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Set;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Header Renderer: nền tint nhạt, viền dưới, ô checkbox chọn tất cả ở cột 0.
 */
public class TableHeaderRenderer extends DefaultTableCellRenderer {

    private final JCheckBox chkAll = new JCheckBox();
    private final BookTableModel tableModel;
    private final Set<String> selectedBookIds;
    private final Runnable onSelectionChanged;

    public TableHeaderRenderer(BookTableModel tableModel, Set<String> selectedBookIds, Runnable onSelectionChanged) {
        this.tableModel = tableModel;
        this.selectedBookIds = selectedBookIds;
        this.onSelectionChanged = onSelectionChanged;

        chkAll.setOpaque(false);
        chkAll.setHorizontalAlignment(SwingConstants.CENTER);
        chkAll.addItemListener(e -> {
            boolean checked = (e.getStateChange() == ItemEvent.SELECTED);
            for (int r = 0; r < tableModel.getRowCount(); r++) {
                BookSummaryDto b = tableModel.getBookAt(r);
                if (b != null) {
                    if (checked) selectedBookIds.add(b.getBookId());
                    else selectedBookIds.remove(b.getBookId());
                }
            }
            tableModel.fireTableDataChanged();
            if (this.onSelectionChanged != null) {
                this.onSelectionChanged.run();
            }
        });
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int col) {
        if (col == 0) {
            JPanel p = new JPanel(new GridBagLayout());
            p.setBackground(UITheme.PRIMARY_TINT);
            p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER));
            boolean allSelected = (table.getRowCount() > 0 && selectedBookIds.size() >= table.getRowCount());
            chkAll.setSelected(allSelected);
            p.add(chkAll);
            return p;
        }

        JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(UITheme.TEXT_MUTED);
        lbl.setBackground(UITheme.PRIMARY_TINT);
        lbl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
            BorderFactory.createEmptyBorder(0, 12, 0, 12)
        ));
        lbl.setOpaque(true);
        return lbl;
    }
}
