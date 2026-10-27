package com.ximilib.presentation.component.table;

import com.ximilib.application.dto.BookSummaryDto;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Model bảng danh sách đầu sách trong kho.
 */
public class BookTableModel extends AbstractTableModel {

    private final String[] columns = {
        "", "Tên sách", "Tác giả", "Mã sách", "Thể loại", "Nhà xuất bản", "Số cuốn", "Thao tác"
    };

    private final Set<String> selectedBookIds;
    private final Runnable onSelectionChanged;
    private List<BookSummaryDto> currentBooks = new ArrayList<>();

    public BookTableModel(Set<String> selectedBookIds, Runnable onSelectionChanged) {
        this.selectedBookIds = selectedBookIds;
        this.onSelectionChanged = onSelectionChanged;
    }

    public void setBooks(List<BookSummaryDto> list) {
        this.currentBooks = (list != null) ? list : new ArrayList<>();
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return currentBooks.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int col) {
        return columns[col];
    }

    @Override
    public Class<?> getColumnClass(int col) {
        if (col == 0) return Boolean.class;
        if (col == 1) return BookSummaryDto.class;
        return String.class;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return col == 0 || col == 7;
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (row < 0 || row >= currentBooks.size()) return null;
        BookSummaryDto b = currentBooks.get(row);
        return switch (col) {
            case 0 -> selectedBookIds.contains(b.getBookId());
            case 1 -> b;
            case 2 -> b.getAuthor();
            case 3 -> b.getBookId();
            case 4 -> b.getCategoryName();
            case 5 -> b.getPublisherName();
            case 6 -> b.getAvailableCopies() + " / " + b.getTotalCopies();
            case 7 -> b;
            default -> null;
        };
    }

    @Override
    public void setValueAt(Object val, int row, int col) {
        if (col == 0 && row >= 0 && row < currentBooks.size()) {
            BookSummaryDto b = currentBooks.get(row);
            if (Boolean.TRUE.equals(val)) {
                selectedBookIds.add(b.getBookId());
            } else {
                selectedBookIds.remove(b.getBookId());
            }
            fireTableCellUpdated(row, col);
            if (onSelectionChanged != null) {
                onSelectionChanged.run();
            }
        }
    }

    public BookSummaryDto getBookAt(int row) {
        if (row >= 0 && row < currentBooks.size()) return currentBooks.get(row);
        return null;
    }
}
