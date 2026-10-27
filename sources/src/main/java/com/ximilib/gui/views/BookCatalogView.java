package com.ximilib.gui.views;

import com.ximilib.application.dto.BookSearchCriteria;
import com.ximilib.application.dto.BookSummaryDto;
import com.ximilib.application.dto.PagedResult;
import com.ximilib.application.service.BookCatalogService;
import com.ximilib.domain.event.BookStockChangedEvent;
import com.ximilib.domain.event.DomainEventListener;
import com.ximilib.domain.event.DomainEventPublisher;
import com.ximilib.domain.exception.BookInUseException;
import com.ximilib.gui.components.PaginationBar;
import com.ximilib.gui.components.SearchField;
import com.ximilib.gui.util.UITheme;
import com.ximilib.gui.util.UserSession;
import com.ximilib.infrastructure.config.AppContainer;
import com.ximilib.models.User;
import com.ximilib.presentation.component.BookFormModalPanel;
import com.ximilib.presentation.component.table.*;
import com.ximilib.presentation.component.table.FilterItems.AvailabilityItem;
import com.ximilib.presentation.component.table.FilterItems.CategoryItem;
import com.ximilib.presentation.util.UiModalHelper;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

/**
 * BookCatalogView: Màn hình "Kho sách" (Use Case UC-3, UC-4).
 * Bố cục phân tầng trực quan, phân trang, lọc theo thể loại và tình trạng tồn kho.
 * Quy tắc: R-01, R-03, R-06, R-08, BR-09.
 */
public class BookCatalogView extends JPanel {

    private final BookCatalogService bookCatalogService;
    private final User currentUser;
    private final boolean canManage;

    // Components UI
    private SearchField txtSearch;
    private JComboBox<CategoryItem> cmbCategory;
    private JComboBox<AvailabilityItem> cmbAvailability;
    private JTable bookTable;
    private BookTableModel tableModel;
    private PaginationBar paginationBar;
    private JLabel lblTotalBooksCount;
    private JButton btnBulkDelete;
    private JPanel loadingOverlay;
    private JPanel emptyStatePanel;
    private JPanel tableContainer;

    // Search state
    private javax.swing.Timer searchDebounceTimer;
    private int currentPage = 1;
    private int pageSize = 10;
    private final Set<String> selectedBookIds = new HashSet<>();
    private DomainEventListener<BookStockChangedEvent> stockEventListener;

    public BookCatalogView() {
        this(AppContainer.getInstance().getBookCatalogService(), UserSession.getCurrentUser());
    }

    public BookCatalogView(BookCatalogService service, User user) {
        this.bookCatalogService = (service != null) ? service : AppContainer.getInstance().getBookCatalogService();
        this.currentUser = (user != null) ? user : new User("ND002", "librarian", "123456", "Thủ Thư", "lib@ximilib.vn", "0905", "Thủ Thư");

        this.canManage = (this.currentUser != null) && (this.currentUser.isAdmin() || this.currentUser.isLibrarian());

        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 28, 28, 28));

        initUI();
        registerStockObserver();
        loadDataAsync();
    }

    private void initUI() {
        JPanel mainContent = new JPanel();
        mainContent.setOpaque(false);
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));

        mainContent.add(createHeaderSection());
        mainContent.add(Box.createVerticalStrut(18));
        mainContent.add(createMainCardPanel());

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderSection() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel("Kho sách");
        lblTitle.setFont(UITheme.FONT_TITLE_LARGE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);

        JPanel breadcrumbPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        breadcrumbPanel.setOpaque(false);

        JLabel lblHome = new JLabel("Trang chủ ");
        lblHome.setFont(UITheme.FONT_SMALL);
        lblHome.setForeground(UITheme.TEXT_MUTED);

        JLabel lblSep = new JLabel(" /  ");
        lblSep.setFont(UITheme.FONT_SMALL);
        lblSep.setForeground(UITheme.CARD_BORDER);

        JLabel lblCurrent = new JLabel("Kho sách");
        lblCurrent.setFont(UITheme.FONT_BOLD);
        lblCurrent.setForeground(UITheme.TEXT_MAIN);

        breadcrumbPanel.add(lblHome);
        breadcrumbPanel.add(lblSep);
        breadcrumbPanel.add(lblCurrent);

        panel.add(lblTitle);
        panel.add(breadcrumbPanel);
        return panel;
    }

    private JPanel createMainCardPanel() {
        JPanel card = new JPanel() {
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
        card.setLayout(new BorderLayout());
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        card.add(createCardToolbar(), BorderLayout.NORTH);

        tableContainer = new JPanel(new BorderLayout());
        tableContainer.setOpaque(false);
        tableContainer.setBorder(BorderFactory.createEmptyBorder(14, 0, 10, 0));

        initTable();
        JScrollPane tableScroll = new JScrollPane(bookTable);
        tableScroll.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER));
        tableScroll.getViewport().setBackground(UITheme.CARD_BG);
        tableScroll.setOpaque(false);

        loadingOverlay = createLoadingPanel();
        emptyStatePanel = createEmptyStatePanel();

        JPanel centerPanel = new JPanel(new CardLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(tableScroll, "TABLE");
        centerPanel.add(loadingOverlay, "LOADING");
        centerPanel.add(emptyStatePanel, "EMPTY");

        tableContainer.add(centerPanel, BorderLayout.CENTER);
        card.add(tableContainer, BorderLayout.CENTER);

        paginationBar = new PaginationBar();
        paginationBar.setOnPageChange(page -> {
            this.currentPage = page;
            loadDataAsync();
        });
        paginationBar.setOnPageSizeChange(size -> {
            this.pageSize = size;
            this.currentPage = 1;
            loadDataAsync();
        });

        card.add(paginationBar, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createCardToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(16, 0));
        toolbar.setOpaque(false);

        JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        leftBox.setOpaque(false);

        JLabel lblTitle = new JLabel("Tất cả sách");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);
        leftBox.add(lblTitle);

        lblTotalBooksCount = new JLabel("(Đang tải...)");
        lblTotalBooksCount.setFont(UITheme.FONT_SMALL);
        lblTotalBooksCount.setForeground(UITheme.TEXT_MUTED);
        leftBox.add(lblTotalBooksCount);

        btnBulkDelete = UITheme.createPillButton("Xóa đã chọn", UITheme.DANGER_BG, UITheme.DANGER_TEXT);
        btnBulkDelete.setFont(UITheme.FONT_BADGE);
        btnBulkDelete.setVisible(false);
        btnBulkDelete.addActionListener(e -> handleBulkDelete());
        if (canManage) {
            leftBox.add(btnBulkDelete);
        }

        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBox.setOpaque(false);

        txtSearch = new SearchField("Tìm theo tên sách, tác giả, mã sách...");
        txtSearch.setPreferredSize(new Dimension(280, 36));
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { triggerDebounceSearch(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { triggerDebounceSearch(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { triggerDebounceSearch(); }
        });

        cmbCategory = new JComboBox<>();
        cmbCategory.setFont(UITheme.FONT_REGULAR);
        cmbCategory.setBackground(UITheme.CARD_BG);
        cmbCategory.setForeground(UITheme.TEXT_MAIN);
        cmbCategory.setPreferredSize(new Dimension(160, 36));
        cmbCategory.putClientProperty("JComponent.roundRect", true);
        cmbCategory.addItem(new CategoryItem(null, "Tất cả thể loại"));

        Map<Integer, String> cats = bookCatalogService.getCategories();
        for (Map.Entry<Integer, String> entry : cats.entrySet()) {
            cmbCategory.addItem(new CategoryItem(entry.getKey(), entry.getValue()));
        }
        cmbCategory.addActionListener(e -> {
            currentPage = 1;
            loadDataAsync();
        });

        cmbAvailability = new JComboBox<>();
        cmbAvailability.setFont(UITheme.FONT_REGULAR);
        cmbAvailability.setBackground(UITheme.CARD_BG);
        cmbAvailability.setForeground(UITheme.TEXT_MAIN);
        cmbAvailability.setPreferredSize(new Dimension(145, 36));
        cmbAvailability.putClientProperty("JComponent.roundRect", true);
        cmbAvailability.addItem(new AvailabilityItem(BookSearchCriteria.AvailabilityFilter.ALL, "Tất cả tình trạng"));
        cmbAvailability.addItem(new AvailabilityItem(BookSearchCriteria.AvailabilityFilter.AVAILABLE_ONLY, "Còn sách"));
        cmbAvailability.addItem(new AvailabilityItem(BookSearchCriteria.AvailabilityFilter.OUT_OF_STOCK, "Hết sách"));
        cmbAvailability.addActionListener(e -> {
            currentPage = 1;
            loadDataAsync();
        });

        rightBox.add(txtSearch);
        rightBox.add(cmbCategory);
        rightBox.add(cmbAvailability);

        toolbar.add(leftBox, BorderLayout.WEST);
        toolbar.add(rightBox, BorderLayout.EAST);
        return toolbar;
    }

    private void triggerDebounceSearch() {
        if (searchDebounceTimer != null && searchDebounceTimer.isRunning()) {
            searchDebounceTimer.restart();
        } else {
            searchDebounceTimer = new javax.swing.Timer(300, evt -> {
                currentPage = 1;
                loadDataAsync();
            });
            searchDebounceTimer.setRepeats(false);
            searchDebounceTimer.start();
        }
    }

    private void initTable() {
        tableModel = new BookTableModel(selectedBookIds, this::updateBulkDeleteButton);
        bookTable = new JTable(tableModel);

        bookTable.setRowHeight(52);
        bookTable.setShowHorizontalLines(false);
        bookTable.setShowVerticalLines(false);
        bookTable.setIntercellSpacing(new Dimension(0, 0));
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.setFillsViewportHeight(true);
        bookTable.setBackground(UITheme.CARD_BG);
        bookTable.setFont(UITheme.FONT_REGULAR);
        bookTable.getTableHeader().setReorderingAllowed(false);

        bookTable.getTableHeader().setDefaultRenderer(new TableHeaderRenderer(tableModel, selectedBookIds, this::updateBulkDeleteButton));
        bookTable.getTableHeader().setPreferredSize(new Dimension(0, 42));

        configureColumns();

        bookTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                bookTable.repaint();
            }
        });

        bookTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = bookTable.columnAtPoint(e.getPoint());
                int row = bookTable.rowAtPoint(e.getPoint());
                if (row >= 0 && (col == 1 || (e.getClickCount() == 2 && col != 0 && col != 7))) {
                    var book = tableModel.getBookAt(row);
                    if (book != null) {
                        com.ximilib.presentation.component.BookDetailModalPanel.show(BookCatalogView.this, book);
                    }
                }
            }
        });
    }

    private void configureColumns() {
        bookTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        bookTable.getColumnModel().getColumn(0).setMaxWidth(55);
        bookTable.getColumnModel().getColumn(0).setCellRenderer(new CheckboxCellRenderer(tableModel, selectedBookIds));
        bookTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JCheckBox()));

        bookTable.getColumnModel().getColumn(1).setPreferredWidth(320);
        bookTable.getColumnModel().getColumn(1).setCellRenderer(new BookTitleCellRenderer(tableModel, selectedBookIds));

        bookTable.getColumnModel().getColumn(2).setPreferredWidth(160);
        bookTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTextRenderer(SwingConstants.LEFT, UITheme.TEXT_MAIN, tableModel, selectedBookIds));

        bookTable.getColumnModel().getColumn(3).setPreferredWidth(95);
        bookTable.getColumnModel().getColumn(3).setCellRenderer(new BookCodeRenderer(tableModel, selectedBookIds));

        bookTable.getColumnModel().getColumn(4).setPreferredWidth(140);
        bookTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTextRenderer(SwingConstants.LEFT, UITheme.TEXT_MUTED, tableModel, selectedBookIds));

        bookTable.getColumnModel().getColumn(5).setPreferredWidth(150);
        bookTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTextRenderer(SwingConstants.LEFT, UITheme.TEXT_MUTED, tableModel, selectedBookIds));

        bookTable.getColumnModel().getColumn(6).setPreferredWidth(140);
        bookTable.getColumnModel().getColumn(6).setCellRenderer(new StockPillRenderer(tableModel, selectedBookIds));

        if (canManage) {
            bookTable.getColumnModel().getColumn(7).setPreferredWidth(90);
            bookTable.getColumnModel().getColumn(7).setMaxWidth(110);
            bookTable.getColumnModel().getColumn(7).setCellRenderer(new ActionCellRenderer(tableModel, selectedBookIds));
            bookTable.getColumnModel().getColumn(7).setCellEditor(new ActionCellEditor(tableModel, this::handleDeleteBook, this::handleEditBook));
        } else {
            bookTable.getColumnModel().removeColumn(bookTable.getColumnModel().getColumn(7));
        }
    }

    private void loadDataAsync() {
        showCardView("LOADING");

        String keyword = txtSearch != null ? txtSearch.getText().trim() : "";
        CategoryItem catItem = (cmbCategory != null) ? (CategoryItem) cmbCategory.getSelectedItem() : null;
        Integer catId = (catItem != null) ? catItem.id : null;
        AvailabilityItem availItem = (cmbAvailability != null) ? (AvailabilityItem) cmbAvailability.getSelectedItem() : null;
        BookSearchCriteria.AvailabilityFilter avail = (availItem != null) ? availItem.filter : BookSearchCriteria.AvailabilityFilter.ALL;

        BookSearchCriteria criteria = new BookSearchCriteria(keyword, catId, avail);
        final int targetPage = currentPage;
        final int targetPageSize = pageSize;

        SwingWorker<PagedResult<BookSummaryDto>, Void> worker = new SwingWorker<>() {
            @Override
            protected PagedResult<BookSummaryDto> doInBackground() {
                try {
                    Thread.sleep(70);
                } catch (InterruptedException ignored) {}
                return bookCatalogService.searchBooks(criteria, targetPage, targetPageSize);
            }

            @Override
            protected void done() {
                try {
                    PagedResult<BookSummaryDto> result = get();
                    SwingUtilities.invokeLater(() -> updateUIWithResult(result));
                } catch (Exception e) {
                    Throwable cause = (e.getCause() != null) ? e.getCause() : e;
                    SwingUtilities.invokeLater(() -> {
                        showCardView("EMPTY");
                        lblTotalBooksCount.setText("(Lỗi kết nối CSDL)");
                        UiModalHelper.showToastError(BookCatalogView.this, "Lỗi kết nối MySQL: " + cause.getMessage());
                    });
                }
            }
        };
        worker.execute();
    }

    private void updateUIWithResult(PagedResult<BookSummaryDto> result) {
        if (result == null || result.getItems().isEmpty()) {
            tableModel.setBooks(Collections.emptyList());
            lblTotalBooksCount.setText("(0 đầu sách)");
            paginationBar.updatePagination(1, 1, pageSize, 0);
            showCardView("EMPTY");
            return;
        }

        tableModel.setBooks(result.getItems());
        lblTotalBooksCount.setText("(" + result.getTotalElements() + " đầu sách)");
        paginationBar.updatePagination(result.getPage(), result.getTotalPages(), result.getPageSize(), result.getTotalElements());
        updateBulkDeleteButton();
        showCardView("TABLE");
    }

    private void showCardView(String viewName) {
        CardLayout cl = (CardLayout) ((JPanel) tableContainer.getComponent(0)).getLayout();
        cl.show((JPanel) tableContainer.getComponent(0), viewName);
    }

    private JPanel createLoadingPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        JLabel lbl = new JLabel("Đang tải danh mục sách thư viện...");
        lbl.setFont(UITheme.FONT_SUBTITLE);
        lbl.setForeground(UITheme.TEXT_MUTED);
        p.add(lbl);
        return p;
    }

    private JPanel createEmptyStatePanel() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(60, 20, 60, 20));

        JLabel lblEmptyTitle = new JLabel("Không tìm thấy sách phù hợp");
        lblEmptyTitle.setFont(UITheme.FONT_TITLE);
        lblEmptyTitle.setForeground(UITheme.TEXT_MAIN);
        lblEmptyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblEmptyDesc = new JLabel("Hãy thử tìm kiếm với từ khóa khác hoặc thay đổi bộ lọc Thể loại / Tình trạng.");
        lblEmptyDesc.setFont(UITheme.FONT_REGULAR);
        lblEmptyDesc.setForeground(UITheme.TEXT_MUTED);
        lblEmptyDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(lblEmptyTitle);
        p.add(Box.createVerticalStrut(8));
        p.add(lblEmptyDesc);
        return p;
    }

    private void handleDeleteBook(BookSummaryDto book) {
        if (book == null) return;

        UiModalHelper.showConfirm(
            this,
            "Xác Nhận Xóa Đầu Sách (BR-09)",
            "Bạn có chắc chắn muốn chuyển đầu sách \"" + book.getTitle() + "\" vào Thùng rác (Xóa mềm)?",
            () -> {
                SwingWorker<Void, Void> deleteWorker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() {
                        bookCatalogService.deleteBook(book.getBookId());
                        return null;
                    }

                    @Override
                    protected void done() {
                        try {
                            get();
                            SwingUtilities.invokeLater(() -> {
                                selectedBookIds.remove(book.getBookId());
                                UiModalHelper.showToastSuccess(
                                    BookCatalogView.this,
                                    "Đã chuyển đầu sách \"" + book.getTitle() + "\" vào Thùng rác thành công!"
                                );
                                loadDataAsync();
                            });
                        } catch (Exception ex) {
                            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                            SwingUtilities.invokeLater(() -> {
                                if (cause instanceof BookInUseException inUseEx) {
                                    UiModalHelper.showToastWarning(
                                        BookCatalogView.this,
                                        inUseEx.getMessage() + " (Vui lòng thu hồi tất cả các cuốn sách trước khi xóa theo BR-09)"
                                    );
                                } else {
                                    UiModalHelper.showToastError(
                                        BookCatalogView.this,
                                        "Lỗi khi xóa sách: " + cause.getMessage()
                                    );
                                }
                            });
                        }
                    }
                };
                deleteWorker.execute();
            }
        );
    }

    private void handleEditBook(BookSummaryDto book) {
        if (book == null) return;
        BookFormModalPanel.show(this, book, this::loadDataAsync);
    }

    private void handleBulkDelete() {
        if (selectedBookIds.isEmpty()) return;

        UiModalHelper.showConfirm(
            this,
            "Xác Nhận Xóa Hàng Loạt",
            "Bạn có chắc chắn muốn xóa mềm " + selectedBookIds.size() + " đầu sách đã chọn không?",
            () -> {
                List<String> toDelete = new ArrayList<>(selectedBookIds);
                SwingWorker<Integer, Void> bulkWorker = new SwingWorker<>() {
                    @Override
                    protected Integer doInBackground() {
                        return bookCatalogService.deleteBooksBatch(toDelete);
                    }

                    @Override
                    protected void done() {
                        try {
                            int deleted = get();
                            SwingUtilities.invokeLater(() -> {
                                selectedBookIds.clear();
                                String msg = "Đã xóa thành công " + deleted + " đầu sách.";
                                if (toDelete.size() - deleted > 0) {
                                    msg += " (Bỏ qua " + (toDelete.size() - deleted) + " đầu sách do có cuốn đang mượn theo BR-09)";
                                }
                                UiModalHelper.showToastSuccess(BookCatalogView.this, msg);
                                loadDataAsync();
                            });
                        } catch (Exception ex) {
                            SwingUtilities.invokeLater(() -> loadDataAsync());
                        }
                    }
                };
                bulkWorker.execute();
            }
        );
    }

    private void updateBulkDeleteButton() {
        if (btnBulkDelete != null) {
            btnBulkDelete.setVisible(!selectedBookIds.isEmpty());
            btnBulkDelete.setText("Xóa đã chọn (" + selectedBookIds.size() + ")");
        }
    }

    private void registerStockObserver() {
        stockEventListener = event -> SwingUtilities.invokeLater(this::loadDataAsync);
        DomainEventPublisher.subscribe(BookStockChangedEvent.class, stockEventListener);
    }
}
