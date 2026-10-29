package com.ximilib.presentation.component.table;

import com.ximilib.application.dto.BookSummaryDto;
import com.ximilib.presentation.util.UITheme;
import java.awt.*;
import java.util.Set;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;

/**
 * Renderer cột "Số cuốn" dạng Pill con nhộng:
 * - Xanh khi còn sách (available > 0)
 * - Đỏ khi hết sách (available == 0)
 */
public class StockPillRenderer extends JPanel implements TableCellRenderer {

    private final JLabel lblPill;
    private final BookTableModel tableModel;
    private final Set<String> selectedBookIds;
    private Color pillBg = UITheme.SUCCESS_BG;
    private Color pillFg = UITheme.SUCCESS_TEXT;

    public StockPillRenderer(BookTableModel tableModel, Set<String> selectedBookIds) {
        this.tableModel = tableModel;
        this.selectedBookIds = selectedBookIds;

        setLayout(new FlowLayout(FlowLayout.LEFT, 12, 12));
        setOpaque(true);

        lblPill = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(pillBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblPill.setOpaque(false);
        lblPill.setFont(UITheme.FONT_BADGE);
        lblPill.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        add(lblPill);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        BookSummaryDto b = tableModel.getBookAt(row);
        if (b != null) {
            if (b.getAvailableCopies() > 0) {
                pillBg = UITheme.SUCCESS_BG;
                pillFg = UITheme.SUCCESS_TEXT;
                lblPill.setText(b.getAvailableCopies() + " / " + b.getTotalCopies() + " cuốn");
            } else {
                pillBg = UITheme.DANGER_BG;
                pillFg = UITheme.DANGER_TEXT;
                lblPill.setText("Hết sách (0/" + b.getTotalCopies() + ")");
            }
            lblPill.setForeground(pillFg);
        }

        boolean isChecked = (b != null && selectedBookIds.contains(b.getBookId()));
        setBackground((isChecked || isSelected) ? UITheme.ROW_SELECTED : UITheme.CARD_BG);
        return this;
    }
}
