package com.ximilib.gui.views;

import com.ximilib.application.dto.BookSummaryDto;
import com.ximilib.application.service.BookCatalogService;
import com.ximilib.domain.event.BookStockChangedEvent;
import com.ximilib.domain.event.DomainEventListener;
import com.ximilib.domain.event.DomainEventPublisher;
import com.ximilib.gui.util.IconFactory;
import com.ximilib.gui.util.UITheme;
import com.ximilib.infrastructure.config.AppContainer;
import com.ximilib.presentation.component.EmptyStatePanel;
import com.ximilib.presentation.util.Illustrations;
import com.ximilib.presentation.util.UiModalHelper;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TrashAndRestoreView: Màn hình Thùng Rác & Khôi Phục Đầu Sách (R-03 Soft-delete).
 * Hiển thị các đầu sách đã bị xóa mềm và cho phép Thủ thư / Quản trị viên khôi phục lại kho sách.
 */
public class TrashAndRestoreView extends JPanel {

    private final BookCatalogService bookCatalogService;
    private final TrashTableModel tableModel = new TrashTableModel();
    private final CardLayout cardLayout = new CardLayout();
    private JPanel centerCardPanel;
    private JTable trashTable;
    private JLabel lblStatus;

    public TrashAndRestoreView() {
        this(AppContainer.getInstance().getBookCatalogService());
    }

    public TrashAndRestoreView(BookCatalogService service) {
        this.bookCatalogService = service != null ? service : AppContainer.getInstance().getBookCatalogService();

        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 28, 28, 28));

        initUI();
        loadTrashDataAsync();

        // Tự động tải lại dữ liệu khi người dùng chuyển tab sang "Thùng rác & Khôi phục"
        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                loadTrashDataAsync();
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {}

            @Override
            public void ancestorMoved(AncestorEvent event) {}
        });

        // Lắng nghe sự kiện domain khi có sách bị xóa hoặc khôi phục
        DomainEventPublisher.subscribe(BookStockChangedEvent.class, event -> {
            SwingUtilities.invokeLater(this::loadTrashDataAsync);
        });
    }

    private void initUI() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Thùng rác & Khôi phục");
        lblTitle.setFont(UITheme.FONT_TITLE_LARGE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);

        lblStatus = new JLabel("Danh sách các đầu sách đã được chuyển vào thùng rác");
        lblStatus.setFont(UITheme.FONT_SMALL);
        lblStatus.setForeground(UITheme.TEXT_MUTED);

        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(lblStatus);

        JButton btnRefresh = UITheme.createPillButton("Làm mới", UITheme.PRIMARY_TINT, UITheme.ON_TINT);
        btnRefresh.setFont(UITheme.FONT_BOLD);
        btnRefresh.setPreferredSize(new Dimension(110, 36));
        btnRefresh.addActionListener(e -> loadTrashDataAsync());

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(btnRefresh, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

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

        trashTable = new JTable(tableModel);
        trashTable.setRowHeight(48);
        trashTable.setShowHorizontalLines(true);
        trashTable.setShowVerticalLines(false);
        trashTable.setGridColor(UITheme.BORDER);
        trashTable.setBackground(UITheme.CARD_BG);
        trashTable.setFont(UITheme.FONT_REGULAR);
        trashTable.getTableHeader().setPreferredSize(new Dimension(0, 38));
        trashTable.getTableHeader().setFont(UITheme.FONT_BOLD);
        trashTable.getTableHeader().setBackground(UITheme.PRIMARY_TINT);
        trashTable.getTableHeader().setForeground(UITheme.TEXT_MUTED);

        trashTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        trashTable.getColumnModel().getColumn(1).setPreferredWidth(320);
        trashTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        trashTable.getColumnModel().getColumn(3).setPreferredWidth(160);
        trashTable.getColumnModel().getColumn(4).setPreferredWidth(120);

        ActionCell cell = new ActionCell();
        trashTable.getColumnModel().getColumn(4).setCellRenderer(cell);
        trashTable.getColumnModel().getColumn(4).setCellEditor(cell);

        JScrollPane scroll = new JScrollPane(trashTable);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UITheme.CARD_BG);

        centerCardPanel = new JPanel(cardLayout);
        centerCardPanel.setOpaque(false);
        centerCardPanel.add(scroll, "TABLE");
        centerCardPanel.add(new EmptyStatePanel(
            Illustrations.EMPTY_TRASH,
            150,
            "Thùng rác đang trống",
            "Không có đầu sách nào bị xóa trong hệ thống."
        ), "EMPTY");

        card.add(centerCardPanel, BorderLayout.CENTER);

        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        main.add(card, BorderLayout.CENTER);

        add(main, BorderLayout.CENTER);
    }

    private void loadTrashDataAsync() {
        SwingWorker<List<BookSummaryDto>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<BookSummaryDto> doInBackground() {
                return bookCatalogService.getDeletedBooks();
            }

            @Override
            protected void done() {
                try {
                    List<BookSummaryDto> list = get();
                    SwingUtilities.invokeLater(() -> {
                        tableModel.setBooks(list);
                        if (list == null || list.isEmpty()) {
                            cardLayout.show(centerCardPanel, "EMPTY");
                            lblStatus.setText("Thùng rác hiện đang trống.");
                        } else {
                            cardLayout.show(centerCardPanel, "TABLE");
                            lblStatus.setText("Tổng cộng có " + list.size() + " đầu sách trong thùng rác.");
                        }
                    });
                } catch (Exception ex) {
                    UiModalHelper.showToastError(TrashAndRestoreView.this, "Lỗi tải thùng rác: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void handleRestore(BookSummaryDto book) {
        UiModalHelper.showConfirm(this, "Khôi phục sách", "Khôi phục đầu sách \"" + book.getTitle() + "\" về Kho sách hoạt động?", () -> {
            bookCatalogService.restoreBook(book.getBookId());
            loadTrashDataAsync();
            UiModalHelper.showToastSuccess(this, "Đã khôi phục thành công đầu sách: " + book.getTitle());
        });
    }

    private class TrashTableModel extends AbstractTableModel {
        private final String[] cols = {"Mã sách", "Tên sách", "Tác giả", "Thể loại", "Thao tác"};
        private final List<BookSummaryDto> books = new ArrayList<>();

        public void setBooks(List<BookSummaryDto> list) {
            books.clear();
            if (list != null) books.addAll(list);
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return books.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public boolean isCellEditable(int r, int c) { return c == 4; }

        @Override
        public Object getValueAt(int r, int c) {
            BookSummaryDto b = books.get(r);
            return switch (c) {
                case 0 -> b.getBookId();
                case 1 -> b.getTitle();
                case 2 -> b.getAuthor();
                case 3 -> b.getCategoryName();
                case 4 -> b;
                default -> null;
            };
        }
    }

    private class ActionCell extends AbstractCellEditor implements javax.swing.table.TableCellRenderer, javax.swing.table.TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 6));
        private final JButton btnRestore = UITheme.createPillButton("Khôi phục", UITheme.SUCCESS_BG, UITheme.SUCCESS_TEXT);
        private BookSummaryDto currentBook;

        ActionCell() {
            panel.setOpaque(false);
            btnRestore.setFont(UITheme.FONT_BADGE);
            btnRestore.setPreferredSize(new Dimension(84, 28));
            btnRestore.addActionListener(e -> {
                fireEditingStopped();
                if (currentBook != null) handleRestore(currentBook);
            });
            panel.add(btnRestore);
        }

        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) { return panel; }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            if (v instanceof BookSummaryDto b) currentBook = b;
            return panel;
        }
        @Override public Object getCellEditorValue() { return currentBook; }
    }
}
