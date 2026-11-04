package com.ximilib.presentation.view;

import com.ximilib.application.dto.BookSearchCriteria;
import com.ximilib.application.dto.BookSummaryDto;
import com.ximilib.application.dto.PagedResult;
import com.ximilib.application.service.BookCatalogService;
import com.ximilib.domain.event.BookStockChangedEvent;
import com.ximilib.domain.event.DomainEventListener;
import com.ximilib.domain.event.DomainEventPublisher;
import com.ximilib.domain.exception.BookInUseException;
import com.ximilib.presentation.component.PaginationBar;
import com.ximilib.presentation.component.SearchField;
import com.ximilib.presentation.component.BookCoverTile;
import com.ximilib.presentation.component.BookDetailModalPanel;
import com.ximilib.presentation.util.IconFactory;
import com.ximilib.presentation.util.UITheme;
import com.ximilib.presentation.util.UserSession;
import com.ximilib.infrastructure.config.AppContainer;
import com.ximilib.domain.model.User;
import com.ximilib.presentation.component.BookFormModalPanel;
import com.ximilib.presentation.component.table.*;
import com.ximilib.presentation.component.table.FilterItems.AvailabilityItem;
import com.ximilib.presentation.component.table.FilterItems.CategoryItem;
import com.ximilib.presentation.component.EmptyStatePanel;
import com.ximilib.presentation.util.Illustrations;
import com.ximilib.presentation.util.UiModalHelper;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class BookCatalogView extends JPanel {

    private final BookCatalogService bookCatalogService;
    private final User currentUser;
    private final boolean canManage;

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
    private JPanel gridPanel;
    private JButton btnViewToggle;
    private List<BookSummaryDto> lastLoadedBooks = new ArrayList<>();

    private javax.swing.Timer searchDebounceTimer;
    private int currentPage = 1;
    private int pageSize = 50;
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
        if (currentUser != null && currentUser.isGuest()) {
            JPanel topWrap = new JPanel(new BorderLayout(0, 12));
            topWrap.setOpaque(false);
            topWrap.add(createGuestInvitationBanner(), BorderLayout.NORTH);
            topWrap.add(createHeaderSection(), BorderLayout.SOUTH);
            add(topWrap, BorderLayout.NORTH);
        } else {
            add(createHeaderSection(), BorderLayout.NORTH);
        }

        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setOpaque(false);
        centerWrap.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        centerWrap.add(createMainCardPanel(), BorderLayout.CENTER);

        add(centerWrap, BorderLayout.CENTER);

        registerKeyboardAction(
            e -> {
                if (txtSearch != null) {
                    txtSearch.requestFocusInWindow();
                    txtSearch.selectAll();
                }
            },
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.KeyEvent.CTRL_DOWN_MASK),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private JPanel createGuestInvitationBanner() {
        JPanel banner = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.TINT_LIGHT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(UITheme.TINT);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        banner.setOpaque(false);
        banner.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 12));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftBox.setOpaque(false);

        JLabel lblIcon = new JLabel(com.ximilib.presentation.util.IconFactory.createBookIcon(UITheme.PRIMARY, 16));
        JLabel lblMsg = new JLabel("Chào mừng bạn! Đăng nhập hoặc đăng ký tài khoản để mượn sách mang về, xem vị trí kệ và nhận thẻ thư viện.");
        lblMsg.setFont(UITheme.FONT_SMALL);
        lblMsg.setForeground(UITheme.TEXT_MAIN);

        leftBox.add(lblIcon);
        leftBox.add(lblMsg);

        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBox.setOpaque(false);

        JButton btnClose = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(0, 0, 0, 15));
                    g2.fillOval(0, 0, getWidth(), getHeight());
                }
                Icon closeIcon = IconFactory.createCloseIcon(UITheme.TEXT_MUTED, 12);
                int ix = (getWidth() - closeIcon.getIconWidth()) / 2;
                int iy = (getHeight() - closeIcon.getIconHeight()) / 2;
                closeIcon.paintIcon(this, g2, ix, iy);
                g2.dispose();
            }
        };
        btnClose.setPreferredSize(new Dimension(24, 24));
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.setToolTipText("Đóng");
        btnClose.addActionListener(e -> {
            banner.setVisible(false);
            revalidate();
            repaint();
        });

        rightBox.add(btnClose);

        banner.add(leftBox, BorderLayout.WEST);
        banner.add(rightBox, BorderLayout.EAST);
        return banner;
    }

    private JPanel createHeaderSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Kho Sách");
        lblTitle.setFont(UITheme.FONT_TITLE_LARGE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        boolean isReaderMode = (currentUser == null || currentUser.isReader() || currentUser.isGuest());
        JLabel lblSub = new JLabel(isReaderMode
            ? "Tra cứu danh mục sách phong phú, tác giả và tình trạng sẵn sàng tại thư viện"
            : "Danh mục các đầu sách trong thư viện, quản lý số lượng tồn và tình trạng khả dụng");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(lblSub);

        panel.add(titleBox, BorderLayout.WEST);

        if (canManage) {
            JButton btnAddBook = UITheme.createPillButton("+ Thêm Sách Mới", UITheme.PRIMARY, Color.WHITE);
            btnAddBook.setFont(UITheme.FONT_BOLD);
            btnAddBook.setPreferredSize(new Dimension(150, 38));
            btnAddBook.addActionListener(e -> BookFormModalPanel.show(this, null, this::loadDataAsync));
            panel.add(btnAddBook, BorderLayout.EAST);
        }

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

        gridPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16)) {
            @Override
            public Dimension getPreferredSize() {
                int targetWidth = (getParent() != null && getParent().getWidth() > 0) ? getParent().getWidth() : 800;
                int tileWidth = 126;
                int tileHeight = 224;
                int hgap = 16;
                int vgap = 16;
                int cols = Math.max(1, (targetWidth - hgap) / (tileWidth + hgap));
                int count = getComponentCount();
                if (count == 0) return new Dimension(targetWidth, 100);
                int rows = (count + cols - 1) / cols;
                int totalHeight = rows * (tileHeight + vgap) + vgap + 20;
                return new Dimension(targetWidth, totalHeight);
            }
        };
        gridPanel.setOpaque(false);
        JScrollPane gridScroll = new JScrollPane(gridPanel);
        gridScroll.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER));
        gridScroll.getViewport().setBackground(UITheme.CARD_BG);
        gridScroll.setOpaque(false);
        gridScroll.getVerticalScrollBar().setUnitIncrement(16);
        gridScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        gridScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        loadingOverlay = createLoadingPanel();
        emptyStatePanel = createEmptyStatePanel();

        JPanel centerPanel = new JPanel(new CardLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(tableScroll, "TABLE");
        centerPanel.add(gridScroll, "GRID");
        centerPanel.add(loadingOverlay, "LOADING");
        centerPanel.add(emptyStatePanel, "EMPTY");

        tableContainer.add(centerPanel, BorderLayout.CENTER);
        card.add(tableContainer, BorderLayout.CENTER);

        paginationBar = new PaginationBar();
        paginationBar.setPageSizeSelectorVisible(false);
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

        btnViewToggle = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(UITheme.TINT_LIGHT);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } else {
                    g2.setColor(UITheme.CARD_BG);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.setColor(UITheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnViewToggle.setIcon("GRID".equalsIgnoreCase(UserSession.getCatalogViewMode())
            ? IconFactory.createListIcon(UITheme.ON_TINT, 16)
            : IconFactory.createGridIcon(UITheme.ON_TINT, 16));
        btnViewToggle.setPreferredSize(new Dimension(36, 36));
        btnViewToggle.setContentAreaFilled(false);
        btnViewToggle.setBorderPainted(false);
        btnViewToggle.setFocusPainted(false);
        btnViewToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnViewToggle.setToolTipText("Chuyển đổi giữa chế độ xem danh sách và lưới ảnh bìa");
        btnViewToggle.addActionListener(e -> toggleViewMode());
        rightBox.add(btnViewToggle);

        toolbar.add(leftBox, BorderLayout.WEST);
        toolbar.add(rightBox, BorderLayout.EAST);
        return toolbar;
    }

    public void setSearchKeyword(String keyword) {
        if (txtSearch != null) {
            this.currentPage = 1;
            txtSearch.setText(keyword != null ? keyword : "");
            loadDataAsync();
        }
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
                int viewCol = bookTable.columnAtPoint(e.getPoint());
                int viewRow = bookTable.rowAtPoint(e.getPoint());
                if (viewRow >= 0 && viewCol >= 0) {
                    int modelCol = bookTable.convertColumnIndexToModel(viewCol);
                    int modelRow = bookTable.convertRowIndexToModel(viewRow);
                    if (modelCol == 1 || (e.getClickCount() == 2 && modelCol != 0 && modelCol != 7)) {
                        BookSummaryDto book = tableModel.getBookAt(modelRow);
                        if (book != null) {
                            com.ximilib.presentation.component.BookDetailModalPanel.show(BookCatalogView.this, book);
                        }
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
            bookTable.getColumnModel().removeColumn(bookTable.getColumnModel().getColumn(0));
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
            this.lastLoadedBooks = Collections.emptyList();
            tableModel.setBooks(Collections.emptyList());
            if (gridPanel != null) gridPanel.removeAll();
            lblTotalBooksCount.setText("(0 đầu sách)");
            paginationBar.updatePagination(1, 1, pageSize, 0);
            showCardView("EMPTY");
            return;
        }

        this.lastLoadedBooks = result.getItems();
        tableModel.setBooks(result.getItems());
        renderGridView(result.getItems());
        lblTotalBooksCount.setText("(" + result.getTotalElements() + " đầu sách)");
        paginationBar.updatePagination(result.getPage(), result.getTotalPages(), result.getPageSize(), result.getTotalElements());
        updateBulkDeleteButton();

        showCardView(UserSession.getCatalogViewMode());
    }

    private void toggleViewMode() {
        if ("GRID".equalsIgnoreCase(UserSession.getCatalogViewMode())) {
            UserSession.setCatalogViewMode("TABLE");
            btnViewToggle.setIcon(IconFactory.createGridIcon(UITheme.ON_TINT, 16));
            showCardView("TABLE");
        } else {
            UserSession.setCatalogViewMode("GRID");
            btnViewToggle.setIcon(IconFactory.createListIcon(UITheme.ON_TINT, 16));
            renderGridView(lastLoadedBooks);
            showCardView("GRID");
        }
    }

    private void renderGridView(List<BookSummaryDto> books) {
        if (gridPanel == null) return;
        gridPanel.removeAll();
        if (books != null) {
            for (BookSummaryDto b : books) {
                BookCoverTile tile = new BookCoverTile(
                    b.getCoverImage(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getAvailableCopies(),
                    () -> BookDetailModalPanel.show(BookCatalogView.this, b)
                );
                gridPanel.add(tile);
            }
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void showCardView(String viewName) {
        CardLayout cl = (CardLayout) ((JPanel) tableContainer.getComponent(0)).getLayout();
        cl.show((JPanel) tableContainer.getComponent(0), viewName);
    }

    private JPanel createLoadingPanel() {
        JPanel skeletonPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int rowHeight = 54;
                int startY = 14;
                int rows = 3;

                Color shimmerBg = new Color(248, 250, 252);
                Color shimmerBar = new Color(226, 232, 240);

                for (int i = 0; i < rows; i++) {
                    int y = startY + i * (rowHeight + 10);
                    int w = getWidth() - 28;
                    if (w <= 0) w = 600;

                    g2.setColor(shimmerBg);
                    g2.fillRoundRect(14, y, w, rowHeight, 10, 10);

                    g2.setColor(shimmerBar);
                    g2.fillRoundRect(26, y + 7, 32, 40, 6, 6);

                    g2.fillRoundRect(72, y + 14, Math.min(260, w / 3), 12, 6, 6);

                    g2.setColor(new Color(203, 213, 225));
                    g2.fillRoundRect(72, y + 32, Math.min(150, w / 5), 8, 4, 4);

                    g2.setColor(shimmerBar);
                    g2.fillRoundRect(Math.max(300, w - 210), y + 18, 80, 16, 8, 8);

                    g2.setColor(new Color(254, 205, 211, 160));
                    g2.fillRoundRect(Math.max(400, w - 100), y + 16, 70, 20, 10, 10);
                }
                g2.dispose();
            }
        };
        skeletonPanel.setOpaque(false);
        skeletonPanel.setPreferredSize(new Dimension(600, 220));
        return skeletonPanel;
    }

    private JPanel createEmptyStatePanel() {
        return new EmptyStatePanel(
            Illustrations.EMPTY_SEARCH,
            140,
            "Không tìm thấy cuốn sách nào khớp với từ khóa của bạn",
            "Hãy thử tìm kiếm với từ khóa khác hoặc xóa bộ lọc đang chọn để khám phá các đầu sách cùng thể loại.",
            "Đặt lại bộ lọc",
            this::resetFilters
        );
    }

    private void resetFilters() {
        if (txtSearch != null) txtSearch.setText("");
        if (cmbCategory != null && cmbCategory.getItemCount() > 0) cmbCategory.setSelectedIndex(0);
        if (cmbAvailability != null && cmbAvailability.getItemCount() > 0) cmbAvailability.setSelectedIndex(0);
        currentPage = 1;
        loadDataAsync();
    }

    private void handleDeleteBook(BookSummaryDto book) {
        if (book == null) return;

        UiModalHelper.showConfirm(
            this,
            "Xác Nhận Xóa Sách",
            "Bạn có chắc chắn muốn chuyển sách \"" + book.getTitle() + "\" vào Thùng rác (Xóa mềm)?",
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
                                        inUseEx.getMessage() + " (Vui lòng thu hồi tất cả các cuốn sách trước khi xóa)"
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
                                    msg += " (Bỏ qua " + (toDelete.size() - deleted) + " cuốn do đang có người mượn)";
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
