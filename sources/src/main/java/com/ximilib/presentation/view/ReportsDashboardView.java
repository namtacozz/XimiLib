package com.ximilib.presentation.view;

import com.formdev.flatlaf.FlatClientProperties;
import com.ximilib.application.dto.ReportFilterCriteria;
import com.ximilib.application.port.ReportExporter;
import com.ximilib.application.port.ReportPort;
import com.ximilib.infrastructure.config.AppContainer;
import com.ximilib.infrastructure.logging.AuditLogger;
import com.ximilib.presentation.component.SearchField;
import com.ximilib.presentation.component.TrendChartPanel;
import com.ximilib.presentation.util.IconFactory;
import com.ximilib.presentation.util.UITheme;
import com.ximilib.presentation.util.UiModalHelper;
import com.ximilib.presentation.util.UserSession;
import raven.datetime.DatePicker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class ReportsDashboardView extends JPanel {

    private final ReportPort reportPort;
    private final ReportExporter reportExporter;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    private JComboBox<String> cmbReportType;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JButton btnUpdateStatus;
    private TrendChartPanel trendChartPanel;

    private JLabel lblTotalBooksVal;
    private JLabel lblActiveLoansVal;
    private JLabel lblOverdueVal;
    private JLabel lblTotalFinesVal;

    // === Bộ lọc nâng cao (Card đa dạng theo tab báo cáo) ===
    private JPanel advancedFilterCard;
    private CardLayout filterCardLayout;

    // 1. Thống kê mượn - trả
    private SearchField txtLoanReaderSearch;
    private JComboBox<String> cmbLoanType;
    private JComboBox<String> cmbLoanStatus;
    private JFormattedTextField txtLoanDateRange;
    private DatePicker loanRangePicker;

    // 2. Độc giả quá hạn & nợ phạt
    private SearchField txtOverdueSearch;
    private JCheckBox chkOnlyFine;

    // 3. Sách hỏng - mất
    private SearchField txtDamagedSearch;
    private JComboBox<String> cmbDamagedStatus;
    private SearchField txtDamagedLocation;

    // 4. Top 10 đầu sách
    private SearchField txtTopBookSearch;
    private JComboBox<String> cmbTopCategory;

    // Debounce timer cho tìm kiếm gõ phím
    private Timer searchDebounceTimer;

    public ReportsDashboardView() {
        this(AppContainer.getInstance().getReportPort(), AppContainer.getInstance().getReportExporter());
    }

    public ReportsDashboardView(ReportPort reportPort) {
        this(reportPort, AppContainer.getInstance().getReportExporter());
    }

    public ReportsDashboardView(ReportPort reportPort, ReportExporter reportExporter) {
        this.reportPort = (reportPort != null) ? reportPort : AppContainer.getInstance().getReportPort();
        this.reportExporter = (reportExporter != null) ? reportExporter : AppContainer.getInstance().getReportExporter();

        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 28, 28, 28));

        initUI();
        loadKpisAsync();
        loadChartAsync();
        loadReportDataAsync();
    }

    @Deprecated
    public ReportsDashboardView(Object legacyConnectionManager) {
        this(AppContainer.getInstance().getReportPort());
    }

    private void initUI() {
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblBreadcrumb = new JLabel("Trang chủ / Thống kê thư viện");
        lblBreadcrumb.setFont(UITheme.FONT_SMALL);
        lblBreadcrumb.setForeground(UITheme.TEXT_MUTED);
        lblBreadcrumb.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel("Báo Cáo & Thống Kê Định Kỳ");
        lblTitle.setFont(UITheme.FONT_TITLE_LARGE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleBox.add(lblBreadcrumb);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(lblTitle);

        topPanel.add(titleBox);
        topPanel.add(Box.createVerticalStrut(12));

        // Khối tổng quan: 1 khung chứa 4 ô số liệu nằm bên trái, Biểu đồ xu hướng thu hẹp nằm bên phải
        JPanel upperSection = new JPanel(new BorderLayout(14, 0));
        upperSection.setOpaque(false);
        upperSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        upperSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 155));
        upperSection.setPreferredSize(new Dimension(900, 155));

        lblTotalBooksVal = new JLabel("...", SwingConstants.LEFT);
        lblActiveLoansVal = new JLabel("...", SwingConstants.LEFT);
        lblOverdueVal = new JLabel("...", SwingConstants.LEFT);
        lblTotalFinesVal = new JLabel("...", SwingConstants.LEFT);

        JPanel kpiContainer = createKpiContainer();
        trendChartPanel = new TrendChartPanel();

        upperSection.add(kpiContainer, BorderLayout.WEST);
        upperSection.add(trendChartPanel, BorderLayout.CENTER);

        topPanel.add(upperSection);
        topPanel.add(Box.createVerticalStrut(12));

        // Thanh chọn loại báo cáo và các nút tác vụ xuất file
        JPanel filterBar = new JPanel(new BorderLayout(10, 0));
        filterBar.setOpaque(false);
        filterBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel leftFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftFilter.setOpaque(false);

        JLabel lblSelect = new JLabel("Loại báo cáo:");
        lblSelect.setFont(UITheme.FONT_BOLD);
        lblSelect.setForeground(UITheme.TEXT_MAIN);

        cmbReportType = new JComboBox<>(new String[]{
            "1. Thống kê lượt mượn - trả",
            "2. Độc giả quá hạn & nợ phạt",
            "3. Sách hỏng - mất cần thanh lý",
            "4. Top 10 đầu sách mượn nhiều nhất"
        });
        cmbReportType.setFont(UITheme.FONT_REGULAR);
        cmbReportType.setPreferredSize(new Dimension(245, 36));
        cmbReportType.putClientProperty(FlatClientProperties.STYLE, "arc: 10; background: " + UITheme.HEX_PAGE_BG);
        cmbReportType.addActionListener(e -> {
            int idx = cmbReportType.getSelectedIndex();
            filterCardLayout.show(advancedFilterCard, "TAB_" + idx);
            loadReportDataAsync();
        });

        JButton btnRefresh = new JButton(IconFactory.createRefreshIcon(UITheme.TEXT_MAIN, 16));
        btnRefresh.setPreferredSize(new Dimension(38, 36));
        btnRefresh.setBackground(UITheme.BG_LIGHT);
        btnRefresh.setBorder(new UITheme.RoundedBorder(UITheme.BORDER, 8));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.setToolTipText("Làm mới dữ liệu thống kê");
        btnRefresh.addActionListener(e -> {
            loadKpisAsync();
            loadChartAsync();
            loadReportDataAsync();
        });

        leftFilter.add(lblSelect);
        leftFilter.add(cmbReportType);
        leftFilter.add(btnRefresh);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        btnUpdateStatus = new JButton(IconFactory.createCheckIcon(UITheme.ON_TINT, 16));
        btnUpdateStatus.setPreferredSize(new Dimension(38, 36));
        btnUpdateStatus.setBackground(UITheme.TINT);
        btnUpdateStatus.setBorder(new UITheme.RoundedBorder(UITheme.BORDER, 8));
        btnUpdateStatus.setFocusPainted(false);
        btnUpdateStatus.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnUpdateStatus.setToolTipText("Cập nhật trạng thái phiếu mượn");
        btnUpdateStatus.addActionListener(e -> showUpdateStatusDialog());

        JButton btnExportXlsx = new JButton(IconFactory.createExcelIcon(Color.WHITE, 16));
        btnExportXlsx.setPreferredSize(new Dimension(38, 36));
        btnExportXlsx.setBackground(UITheme.PRIMARY);
        btnExportXlsx.setBorder(new UITheme.RoundedBorder(UITheme.PRIMARY, 8));
        btnExportXlsx.setFocusPainted(false);
        btnExportXlsx.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExportXlsx.setToolTipText("Xuất báo cáo Excel (.xlsx)");
        btnExportXlsx.addActionListener(e -> exportToXlsx());

        JButton btnPrint = new JButton(IconFactory.createPrintIcon(Color.WHITE, 16));
        btnPrint.setPreferredSize(new Dimension(38, 36));
        btnPrint.setBackground(UITheme.TEXT_MUTED);
        btnPrint.setBorder(new UITheme.RoundedBorder(UITheme.BORDER, 8));
        btnPrint.setFocusPainted(false);
        btnPrint.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPrint.setToolTipText("In báo cáo");
        btnPrint.addActionListener(e -> printReport());

        rightActions.add(btnUpdateStatus);
        rightActions.add(btnExportXlsx);
        rightActions.add(btnPrint);

        filterBar.add(leftFilter, BorderLayout.WEST);
        filterBar.add(rightActions, BorderLayout.EAST);
        topPanel.add(filterBar);
        topPanel.add(Box.createVerticalStrut(8));

        // Khởi tạo Card Bộ lọc nâng cao
        initAdvancedFilterCards();
        topPanel.add(advancedFilterCard);
        topPanel.add(Box.createVerticalStrut(10));

        add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel();
        reportTable = new JTable(tableModel);
        reportTable.setRowHeight(36);
        reportTable.setFont(UITheme.FONT_REGULAR);
        reportTable.getTableHeader().setFont(UITheme.FONT_BOLD);
        reportTable.getTableHeader().setBackground(UITheme.BG_LIGHT);
        reportTable.setSelectionBackground(UITheme.PASTEL);
        reportTable.setSelectionForeground(UITheme.ON_PASTEL);

        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void initAdvancedFilterCards() {
        filterCardLayout = new CardLayout();
        advancedFilterCard = new JPanel(filterCardLayout);
        advancedFilterCard.setOpaque(false);
        advancedFilterCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Card 0: Lọc Thống kê mượn - trả
        advancedFilterCard.add(createLoanFilterPanel(), "TAB_0");

        // Card 1: Lọc Độc giả quá hạn & nợ phạt
        advancedFilterCard.add(createOverdueFilterPanel(), "TAB_1");

        // Card 2: Lọc Sách hỏng - mất
        advancedFilterCard.add(createDamagedFilterPanel(), "TAB_2");

        // Card 3: Lọc Top 10 đầu sách
        advancedFilterCard.add(createTopBooksFilterPanel(), "TAB_3");
    }

    private JPanel createFilterContainer() {
        JPanel container = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(UITheme.CARD_BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(3, 10, 3, 10));
        return container;
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(UITheme.TEXT_MUTED);
        return lbl;
    }

    private JPanel createLoanFilterPanel() {
        JPanel p = createFilterContainer();

        JLabel lblTag = new JLabel("Bộ lọc:");
        lblTag.setFont(UITheme.FONT_BOLD);
        lblTag.setForeground(UITheme.PRIMARY);
        p.add(lblTag);

        // 1. Bạn đọc / Mã phiếu
        txtLoanReaderSearch = new SearchField("Bạn đọc, mã phiếu...");
        txtLoanReaderSearch.setPreferredSize(new Dimension(175, 32));
        bindDebounce(txtLoanReaderSearch);
        p.add(txtLoanReaderSearch);

        // 2. Hình thức
        cmbLoanType = new JComboBox<>(new String[]{"Hình thức: Tất cả", "Đọc tại chỗ", "Mượn về nhà"});
        cmbLoanType.setFont(UITheme.FONT_REGULAR);
        cmbLoanType.setPreferredSize(new Dimension(135, 32));
        cmbLoanType.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        cmbLoanType.addActionListener(e -> loadReportDataAsync());
        p.add(cmbLoanType);

        // 3. Trạng thái
        cmbLoanStatus = new JComboBox<>(new String[]{
            "Trạng thái: Tất cả",
            "Chờ duyệt (0)",
            "Đang mượn (1)",
            "Đã trả (2)",
            "Quá hạn (3)",
            "Chờ bồi thường (4)",
            "Đã mất (5)",
            "Đã hủy (6)"
        });
        cmbLoanStatus.setFont(UITheme.FONT_REGULAR);
        cmbLoanStatus.setPreferredSize(new Dimension(145, 32));
        cmbLoanStatus.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        cmbLoanStatus.addActionListener(e -> loadReportDataAsync());
        p.add(cmbLoanStatus);

        // 4. Ngày mượn (Khoảng ngày)
        txtLoanDateRange = new JFormattedTextField();
        txtLoanDateRange.setFont(UITheme.FONT_REGULAR);
        txtLoanDateRange.setPreferredSize(new Dimension(165, 32));
        txtLoanDateRange.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Khoảng ngày mượn...");
        txtLoanDateRange.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        txtLoanDateRange.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        loanRangePicker = UiModalHelper.createDatePicker(txtLoanDateRange, true);
        loanRangePicker.addDateSelectionListener(dateEvent -> loadReportDataAsync());
        p.add(txtLoanDateRange);

        // Nút đặt lại lọc
        JButton btnReset = UITheme.createPillButton("Đặt lại", UITheme.TINT, UITheme.ON_TINT);
        btnReset.setPreferredSize(new Dimension(75, 32));
        btnReset.setFont(UITheme.FONT_REGULAR);
        btnReset.setToolTipText("Khôi phục bộ lọc mặc định");
        btnReset.addActionListener(e -> {
            txtLoanReaderSearch.setText("");
            cmbLoanType.setSelectedIndex(0);
            cmbLoanStatus.setSelectedIndex(0);
            if (loanRangePicker != null) {
                loanRangePicker.clearSelectedDate();
            }
            txtLoanDateRange.setValue(null);
            txtLoanDateRange.setText("");
            loadReportDataAsync();
        });
        p.add(btnReset);

        return p;
    }

    private JPanel createOverdueFilterPanel() {
        JPanel p = createFilterContainer();

        JLabel lblTag = new JLabel("Lọc quá hạn:");
        lblTag.setFont(UITheme.FONT_BOLD);
        lblTag.setForeground(UITheme.DANGER_TEXT);
        p.add(lblTag);

        txtOverdueSearch = new SearchField("Tên bạn đọc, mã thẻ, số ĐT, mã phiếu...");
        txtOverdueSearch.setPreferredSize(new Dimension(300, 32));
        bindDebounce(txtOverdueSearch);
        p.add(txtOverdueSearch);

        chkOnlyFine = new JCheckBox("Chỉ độc giả đang nợ tiền phạt > 0");
        chkOnlyFine.setFont(UITheme.FONT_REGULAR);
        chkOnlyFine.setForeground(UITheme.TEXT_MAIN);
        chkOnlyFine.setOpaque(false);
        chkOnlyFine.addActionListener(e -> loadReportDataAsync());
        p.add(chkOnlyFine);

        JButton btnReset = UITheme.createPillButton("Đặt lại", UITheme.TINT, UITheme.ON_TINT);
        btnReset.setPreferredSize(new Dimension(75, 32));
        btnReset.setFont(UITheme.FONT_REGULAR);
        btnReset.setToolTipText("Khôi phục bộ lọc mặc định");
        btnReset.addActionListener(e -> {
            txtOverdueSearch.setText("");
            chkOnlyFine.setSelected(false);
            loadReportDataAsync();
        });
        p.add(btnReset);

        return p;
    }

    private JPanel createDamagedFilterPanel() {
        JPanel p = createFilterContainer();

        JLabel lblTag = new JLabel("Lọc sách hỏng - mất:");
        lblTag.setFont(UITheme.FONT_BOLD);
        lblTag.setForeground(new Color(217, 119, 6));
        p.add(lblTag);

        txtDamagedSearch = new SearchField("Mã cuốn hoặc tựa sách...");
        txtDamagedSearch.setPreferredSize(new Dimension(210, 32));
        bindDebounce(txtDamagedSearch);
        p.add(txtDamagedSearch);

        cmbDamagedStatus = new JComboBox<>(new String[]{"Trạng thái: Tất cả", "Hỏng (HONG)", "Đã mất (DA_MAT)"});
        cmbDamagedStatus.setFont(UITheme.FONT_REGULAR);
        cmbDamagedStatus.setPreferredSize(new Dimension(160, 32));
        cmbDamagedStatus.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        cmbDamagedStatus.addActionListener(e -> loadReportDataAsync());
        p.add(cmbDamagedStatus);

        txtDamagedLocation = new SearchField("Khu vực, kệ sách...");
        txtDamagedLocation.setPreferredSize(new Dimension(160, 32));
        bindDebounce(txtDamagedLocation);
        p.add(txtDamagedLocation);

        JButton btnReset = UITheme.createPillButton("Đặt lại", UITheme.TINT, UITheme.ON_TINT);
        btnReset.setPreferredSize(new Dimension(75, 32));
        btnReset.setFont(UITheme.FONT_REGULAR);
        btnReset.setToolTipText("Khôi phục bộ lọc mặc định");
        btnReset.addActionListener(e -> {
            txtDamagedSearch.setText("");
            cmbDamagedStatus.setSelectedIndex(0);
            txtDamagedLocation.setText("");
            loadReportDataAsync();
        });
        p.add(btnReset);

        return p;
    }

    private JPanel createTopBooksFilterPanel() {
        JPanel p = createFilterContainer();

        JLabel lblTag = new JLabel("Lọc xếp hạng:");
        lblTag.setFont(UITheme.FONT_BOLD);
        lblTag.setForeground(new Color(16, 185, 129));
        p.add(lblTag);

        txtTopBookSearch = new SearchField("Tên đầu sách hoặc mã sách...");
        txtTopBookSearch.setPreferredSize(new Dimension(250, 32));
        bindDebounce(txtTopBookSearch);
        p.add(txtTopBookSearch);

        cmbTopCategory = new JComboBox<>();
        cmbTopCategory.setFont(UITheme.FONT_REGULAR);
        cmbTopCategory.setPreferredSize(new Dimension(175, 32));
        cmbTopCategory.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        cmbTopCategory.addItem("Thể loại: Tất cả");

        // Lấy danh mục thể loại từ BookCatalogService
        try {
            java.util.Map<Integer, String> cats = AppContainer.getInstance().getBookCatalogService().getCategories();
            for (String catName : cats.values()) {
                cmbTopCategory.addItem(catName);
            }
        } catch (Exception ignored) {}

        cmbTopCategory.addActionListener(e -> loadReportDataAsync());
        p.add(cmbTopCategory);

        JButton btnReset = UITheme.createPillButton("Đặt lại", UITheme.TINT, UITheme.ON_TINT);
        btnReset.setPreferredSize(new Dimension(75, 32));
        btnReset.setFont(UITheme.FONT_REGULAR);
        btnReset.setToolTipText("Khôi phục bộ lọc mặc định");
        btnReset.addActionListener(e -> {
            txtTopBookSearch.setText("");
            cmbTopCategory.setSelectedIndex(0);
            loadReportDataAsync();
        });
        p.add(btnReset);

        return p;
    }

    private void bindDebounce(JTextField field) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { triggerSearch(); }
            @Override public void removeUpdate(DocumentEvent e) { triggerSearch(); }
            @Override public void changedUpdate(DocumentEvent e) { triggerSearch(); }

            private void triggerSearch() {
                if (searchDebounceTimer != null && searchDebounceTimer.isRunning()) {
                    searchDebounceTimer.restart();
                } else {
                    searchDebounceTimer = new Timer(300, ev -> loadReportDataAsync());
                    searchDebounceTimer.setRepeats(false);
                    searchDebounceTimer.start();
                }
            }
        });
    }

    private ReportFilterCriteria buildCriteriaForCurrentTab(int tabIndex) {
        ReportFilterCriteria criteria = new ReportFilterCriteria();

        switch (tabIndex) {
            case 0 -> {
                if (txtLoanReaderSearch != null) {
                    criteria.setReaderName(txtLoanReaderSearch.getText().trim());
                }
                if (cmbLoanType != null) {
                    int idx = cmbLoanType.getSelectedIndex();
                    if (idx == 1) criteria.setLoanType("DOC_TAI_CHO");
                    else if (idx == 2) criteria.setLoanType("VE_NHA");
                }
                if (cmbLoanStatus != null) {
                    int idx = cmbLoanStatus.getSelectedIndex();
                    if (idx > 0) {
                        criteria.setStatus(idx - 1); // 1 tương ứng code 0, 2 tương ứng code 1...
                    }
                }
                if (loanRangePicker != null && loanRangePicker.isDateSelected()) {
                    LocalDate[] dates = loanRangePicker.getSelectedDateRange();
                    if (dates != null && dates.length > 0) {
                        criteria.setFromDate(dates[0]);
                        if (dates.length > 1) {
                            criteria.setToDate(dates[1]);
                        } else {
                            criteria.setToDate(dates[0]);
                        }
                    }
                }
            }
            case 1 -> {
                if (txtOverdueSearch != null) {
                    criteria.setOverdueKeyword(txtOverdueSearch.getText().trim());
                }
                if (chkOnlyFine != null) {
                    criteria.setOnlyHasFine(chkOnlyFine.isSelected());
                }
            }
            case 2 -> {
                if (txtDamagedSearch != null) {
                    criteria.setDamagedKeyword(txtDamagedSearch.getText().trim());
                }
                if (cmbDamagedStatus != null) {
                    int idx = cmbDamagedStatus.getSelectedIndex();
                    if (idx == 1) criteria.setCopyStatus("HONG");
                    else if (idx == 2) criteria.setCopyStatus("DA_MAT");
                }
                if (txtDamagedLocation != null) {
                    criteria.setLocation(txtDamagedLocation.getText().trim());
                }
            }
            case 3 -> {
                if (txtTopBookSearch != null) {
                    criteria.setBookKeyword(txtTopBookSearch.getText().trim());
                }
                if (cmbTopCategory != null && cmbTopCategory.getSelectedIndex() > 0) {
                    criteria.setCategoryName((String) cmbTopCategory.getSelectedItem());
                }
            }
        }
        return criteria;
    }

    private JPanel createKpiContainer() {
        JPanel container = new JPanel(new GridLayout(2, 2, 1, 1)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                // Nền bo góc đồng bộ với TrendChartPanel
                g2.setColor(UITheme.CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 16, 16));

                // Đường phân cách mờ tinh tế giữa 4 ô số liệu
                g2.setColor(UITheme.CARD_BORDER);
                g2.drawLine(w / 2, 14, w / 2, h - 14);
                g2.drawLine(16, h / 2, w - 16, h / 2);

                // Viền ngoài bo góc
                g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 16, 16));
                g2.dispose();
            }
        };
        container.setOpaque(false);
        container.setPreferredSize(new Dimension(380, 155));
        container.setMinimumSize(new Dimension(300, 140));

        // Hàng 1: [Tổng đầu sách] [Đang cho mượn]
        container.add(createKpiCell("Tổng đầu sách", lblTotalBooksVal, UITheme.PRIMARY));
        container.add(createKpiCell("Đang cho mượn", lblActiveLoansVal, new Color(59, 130, 246)));

        // Hàng 2: [Phiếu quá hạn] [Tiền phạt đã thu]
        container.add(createKpiCell("Phiếu quá hạn", lblOverdueVal, UITheme.DANGER_TEXT));
        container.add(createKpiCell("Tiền phạt đã thu", lblTotalFinesVal, new Color(16, 185, 129)));

        return container;
    }

    private JPanel createKpiCell(String title, JLabel valueLabel, Color accentColor) {
        JPanel cell = new JPanel(new BorderLayout(0, 3));
        cell.setOpaque(false);
        cell.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_SMALL);
        lblTitle.setForeground(UITheme.TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 19));
        valueLabel.setForeground(accentColor);
        valueLabel.setHorizontalAlignment(SwingConstants.LEFT);

        cell.add(lblTitle, BorderLayout.NORTH);
        cell.add(valueLabel, BorderLayout.CENTER);
        return cell;
    }

    private void loadKpisAsync() {
        new SwingWorker<ReportPort.ReportKpiSummary, Void>() {
            @Override
            protected ReportPort.ReportKpiSummary doInBackground() {
                return reportPort.getKpiSummary();
            }

            @Override
            protected void done() {
                try {
                    ReportPort.ReportKpiSummary kpi = get();
                    lblTotalBooksVal.setText(String.format("%,d", kpi.totalBooks()));
                    lblActiveLoansVal.setText(String.format("%,d", kpi.activeLoans()));
                    lblOverdueVal.setText(String.format("%,d", kpi.overdueLoans()));
                    lblTotalFinesVal.setText(currencyFormat.format(kpi.totalFines() != null ? kpi.totalFines() : BigDecimal.ZERO));
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    private void loadChartAsync() {
        new SwingWorker<List<ReportPort.DailyMetric>, Void>() {
            @Override
            protected List<ReportPort.DailyMetric> doInBackground() {
                return reportPort.getBorrowAndFineTrend(7);
            }

            @Override
            protected void done() {
                try {
                    List<ReportPort.DailyMetric> metrics = get();
                    if (trendChartPanel != null) {
                        trendChartPanel.setMetrics(metrics);
                    }
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    private void loadReportDataAsync() {
        int selectedIndex = cmbReportType.getSelectedIndex();
        btnUpdateStatus.setVisible(selectedIndex == 0);

        ReportFilterCriteria criteria = buildCriteriaForCurrentTab(selectedIndex);

        new SwingWorker<DefaultTableModel, Void>() {
            @Override
            protected DefaultTableModel doInBackground() {
                DefaultTableModel model = new DefaultTableModel();
                switch (selectedIndex) {
                    case 0 -> {
                        model.setColumnIdentifiers(new String[]{"Mã Phiếu", "Bạn Đọc", "Hình Thức", "Ngày Mượn", "Hạn Trả", "Ngày Trả Thực", "Trạng Thái"});
                        for (Object[] row : reportPort.getLoansReport(criteria)) {
                            model.addRow(row);
                        }
                    }
                    case 1 -> {
                        model.setColumnIdentifiers(new String[]{"Bạn Đọc", "Mã Thẻ", "Số ĐT", "Mã Phiếu", "Hạn Trả", "Nợ Phạt Tồn"});
                        for (Object[] row : reportPort.getOverdueReport(criteria)) {
                            BigDecimal fine = (row[5] instanceof BigDecimal bd) ? bd : BigDecimal.ZERO;
                            row[5] = currencyFormat.format(fine);
                            model.addRow(row);
                        }
                    }
                    case 2 -> {
                        model.setColumnIdentifiers(new String[]{"Mã Cuốn", "Tựa Sách", "Trạng Thái", "Tình Trạng", "Khu Vực", "Kệ Sách"});
                        for (Object[] row : reportPort.getDamagedBooksReport(criteria)) {
                            model.addRow(row);
                        }
                    }
                    case 3 -> {
                        model.setColumnIdentifiers(new String[]{"Mã Sách", "Tên Đầu Sách", "Thể Loại", "Số Lượt Mượn"});
                        for (Object[] row : reportPort.getTopBooksReport(criteria)) {
                            model.addRow(row);
                        }
                    }
                }
                return model;
            }

            @Override
            protected void done() {
                try {
                    DefaultTableModel model = get();
                    reportTable.setModel(model);
                } catch (Exception e) {
                    UiModalHelper.showToastError(ReportsDashboardView.this, "Lỗi tải dữ liệu báo cáo!");
                }
            }
        }.execute();
    }

    private void exportToXlsx() {
        if (reportTable.getRowCount() == 0) {
            UiModalHelper.showToastWarning(this, "Không có dữ liệu trong báo cáo để xuất!");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu file báo cáo Excel (.xlsx)");
        fileChooser.setSelectedFile(new File("BaoCao_" + System.currentTimeMillis() + ".xlsx"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = fileChooser.getSelectedFile();
            if (!selected.getName().toLowerCase().endsWith(".xlsx")) {
                selected = new File(selected.getAbsolutePath() + ".xlsx");
            }
            final File targetFile = selected;

            int colCount = reportTable.getColumnCount();
            int rowCount = reportTable.getRowCount();
            String[] headers = new String[colCount];
            for (int c = 0; c < colCount; c++) {
                headers[c] = reportTable.getColumnName(c);
            }
            List<Object[]> rows = new java.util.ArrayList<>(rowCount);
            for (int r = 0; r < rowCount; r++) {
                Object[] row = new Object[colCount];
                for (int c = 0; c < colCount; c++) {
                    row[c] = reportTable.getValueAt(r, c);
                }
                rows.add(row);
            }

            String title = (String) cmbReportType.getSelectedItem();
            if (title != null) {
                title = title.replace('/', '-').replace('\\', '-').replace('?', '-').replace('*', '-').replace('[', '(').replace(']', ')').replace(':', '-');
                if (title.length() > 30) {
                    title = title.substring(0, 30);
                }
            }
            final String sheetTitle = (title != null && !title.isBlank()) ? title : "BaoCao";

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                        reportExporter.exportToExcel(fos, sheetTitle, headers, rows);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        String currentUserId = UserSession.getCurrentUser() != null ? UserSession.getCurrentUser().getId() : "ADMIN";
                        AuditLogger.log(currentUserId, "EXPORT_REPORT_XLSX", "Xuất file Excel .xlsx: " + targetFile.getName());
                        UiModalHelper.showToastSuccess(ReportsDashboardView.this, "Xuất báo cáo Excel .xlsx thành công: " + targetFile.getName());
                    } catch (Exception ex) {
                        UiModalHelper.showToastError(ReportsDashboardView.this, "Lỗi khi xuất file Excel: " + ex.getMessage());
                    }
                }
            }.execute();
        }
    }

    private static class StatusOption {
        final int code;
        final String label;
        final Color dotColor;
        final String note;

        StatusOption(int code, String label, Color dotColor, String note) {
            this.code = code;
            this.label = label;
            this.dotColor = dotColor;
            this.note = note;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static Icon createDotIcon(Color color, int diameter) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                int cy = y + (getIconHeight() - diameter) / 2;
                g2.fillOval(x + 2, cy, diameter, diameter);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return diameter + 8;
            }

            @Override
            public int getIconHeight() {
                return 18;
            }
        };
    }

    private void showUpdateStatusDialog() {
        if (cmbReportType.getSelectedIndex() != 0) {
            UiModalHelper.showToastWarning(this, "Chức năng cập nhật trạng thái chỉ áp dụng cho Báo cáo lượt mượn/trả!");
            return;
        }

        int row = reportTable.getSelectedRow();
        if (row < 0) {
            UiModalHelper.showToastWarning(this, "Vui lòng bấm chọn một phiếu mượn trên bảng trước khi cập nhật trạng thái!");
            return;
        }

        String ticketId = String.valueOf(reportTable.getValueAt(row, 0));
        String readerName = String.valueOf(reportTable.getValueAt(row, 1));
        String currentStatus = String.valueOf(reportTable.getValueAt(row, 6));

        StatusOption[] options = {
            new StatusOption(0, "Chờ duyệt / Chờ nhận sách", new Color(249, 115, 22), "Phiếu đang chờ bạn đọc đến thư viện để nhận sách."),
            new StatusOption(1, "Đang mượn", new Color(59, 130, 246), "Sách đang được bạn đọc mượn sử dụng hợp lệ."),
            new StatusOption(2, "Đã trả (Hoàn tất)", new Color(16, 185, 129), "Xác nhận độc giả đã hoàn trả đầy đủ sách về kho."),
            new StatusOption(3, "Quá hạn", new Color(239, 68, 68), "Phiếu mượn đã vượt quá thời hạn trả quy định."),
            new StatusOption(4, "Chờ xử lý bồi thường", new Color(217, 119, 6), "Sách bị mất/hỏng. Chuyển sang quy trình đền bù."),
            new StatusOption(5, "Đã mất / Bồi hoàn xong", new Color(107, 114, 128), "Độc giả đã hoàn tất nộp tiền bồi hoàn sách mất."),
            new StatusOption(6, "Đã hủy phiếu", new Color(148, 163, 184), "Phiếu mượn bị hủy do quá hạn nhận hoặc độc giả hủy.")
        };

        int defaultIndex = 1;
        for (int i = 0; i < options.length; i++) {
            if (options[i].label.toLowerCase().contains(currentStatus.toLowerCase()) || currentStatus.toLowerCase().contains(options[i].label.toLowerCase())) {
                defaultIndex = i;
                break;
            }
        }

        Color curBg = UITheme.TINT;
        Color curFg = UITheme.PRIMARY;
        if (currentStatus.contains("Đang mượn")) {
            curBg = new Color(224, 231, 255);
            curFg = new Color(67, 56, 202);
        } else if (currentStatus.contains("Đã trả") || currentStatus.contains("Hoàn tất")) {
            curBg = new Color(220, 252, 231);
            curFg = new Color(21, 128, 61);
        } else if (currentStatus.contains("Quá hạn")) {
            curBg = new Color(254, 243, 199);
            curFg = new Color(180, 83, 9);
        } else if (currentStatus.contains("bồi thường") || currentStatus.contains("mất")) {
            curBg = new Color(254, 226, 226);
            curFg = new Color(185, 28, 28);
        }

        final Color finalCurBg = curBg;
        final Color finalCurFg = curFg;

        JPanel cardInfo = new JPanel(new BorderLayout(8, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(248, 250, 252));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        cardInfo.setOpaque(false);
        cardInfo.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel leftInfo = new JPanel(new GridLayout(2, 1, 0, 4));
        leftInfo.setOpaque(false);
        JLabel lblCode = new JLabel("Phiếu mượn: #" + ticketId);
        lblCode.setFont(UITheme.FONT_BOLD);
        lblCode.setForeground(UITheme.TEXT_MAIN);
        JLabel lblPatron = new JLabel("Bạn đọc: " + readerName);
        lblPatron.setFont(UITheme.FONT_REGULAR);
        lblPatron.setForeground(UITheme.TEXT_MUTED);
        leftInfo.add(lblCode);
        leftInfo.add(lblPatron);

        JLabel lblBadge = new JLabel(currentStatus, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(finalCurBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblBadge.setFont(UITheme.FONT_BADGE);
        lblBadge.setForeground(finalCurFg);
        lblBadge.setOpaque(false);
        lblBadge.setBorder(new EmptyBorder(4, 10, 4, 10));

        JPanel rightBadgeBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        rightBadgeBox.setOpaque(false);
        rightBadgeBox.add(lblBadge);

        cardInfo.add(leftInfo, BorderLayout.WEST);
        cardInfo.add(rightBadgeBox, BorderLayout.EAST);

        JComboBox<StatusOption> cmbStatus = new JComboBox<>(options);
        cmbStatus.setFont(UITheme.FONT_REGULAR);
        cmbStatus.setSelectedIndex(defaultIndex);
        cmbStatus.setPreferredSize(new Dimension(380, 38));
        cmbStatus.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof StatusOption opt) {
                    lbl.setText(opt.label);
                    lbl.setIcon(createDotIcon(opt.dotColor, 10));
                    lbl.setBorder(new EmptyBorder(4, 8, 4, 8));
                }
                return lbl;
            }
        });

        JLabel lblNote = new JLabel(options[defaultIndex].note);
        lblNote.setFont(UITheme.FONT_SMALL);
        lblNote.setForeground(UITheme.TEXT_MUTED);
        lblNote.setBorder(new EmptyBorder(4, 2, 8, 2));

        cmbStatus.addActionListener(e -> {
            StatusOption sel = (StatusOption) cmbStatus.getSelectedItem();
            if (sel != null) {
                lblNote.setText(sel.note);
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(12, 16, 16, 16));
        panel.setPreferredSize(new Dimension(420, 240));

        JLabel lblChoose = new JLabel("Chọn trạng thái mới:");
        lblChoose.setFont(UITheme.FONT_BOLD);
        lblChoose.setForeground(UITheme.TEXT_MAIN);
        lblChoose.setAlignmentX(Component.LEFT_ALIGNMENT);

        cardInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblNote.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(cardInfo);
        panel.add(Box.createVerticalStrut(12));
        panel.add(lblChoose);
        panel.add(Box.createVerticalStrut(6));
        panel.add(cmbStatus);
        panel.add(Box.createVerticalStrut(4));
        panel.add(lblNote);

        raven.modal.component.SimpleModalBorder border = new raven.modal.component.SimpleModalBorder(
            panel,
            "Cập Nhật Trạng Thái Phiếu Mượn",
            raven.modal.component.SimpleModalBorder.OK_CANCEL_OPTION,
            (controller, action) -> {
                if (action == raven.modal.component.SimpleModalBorder.OK_OPTION) {
                    controller.close();
                    StatusOption selected = (StatusOption) cmbStatus.getSelectedItem();
                    int selectedStatusCode = (selected != null) ? selected.code : cmbStatus.getSelectedIndex();
                    updateLoanStatusAsync(ticketId, selectedStatusCode);
                } else if (action == raven.modal.component.SimpleModalBorder.CANCEL_OPTION || action == raven.modal.component.SimpleModalBorder.CLOSE_OPTION) {
                    controller.close();
                }
            }
        );
        raven.modal.option.Option option = raven.modal.ModalDialog.createOption();
        option.setBackgroundClickType(raven.modal.option.Option.BackgroundClickType.BLOCK);
        raven.modal.ModalDialog.showModal(this, border, option, "UPDATE_STATUS_MODAL");
    }

    private void updateLoanStatusAsync(String ticketId, int newStatusCode) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                String currentUserId = UserSession.getCurrentUser() != null ? UserSession.getCurrentUser().getId() : "ADMIN";
                reportPort.updateLoanStatus(ticketId, newStatusCode, currentUserId);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    UiModalHelper.showToastSuccess(ReportsDashboardView.this, "Cập nhật trạng thái phiếu mượn #" + ticketId + " thành công!");
                    loadKpisAsync();
                    loadReportDataAsync();
                } catch (Exception ex) {
                    Throwable cause = (ex.getCause() != null) ? ex.getCause() : ex;
                    UiModalHelper.showToastError(ReportsDashboardView.this, "Lỗi cập nhật: " + cause.getMessage());
                }
            }
        }.execute();
    }

    private void printReport() {
        if (reportTable.getRowCount() == 0) {
            UiModalHelper.showToastWarning(this, "Không có dữ liệu trong báo cáo để in!");
            return;
        }
        try {
            String title = (String) cmbReportType.getSelectedItem();
            boolean complete = reportTable.print(JTable.PrintMode.FIT_WIDTH,
                new java.text.MessageFormat("XimiLib - " + title),
                new java.text.MessageFormat("Trang {0}"),
                true, null, true, null);
            if (complete) {
                String currentUserId = UserSession.getCurrentUser() != null ? UserSession.getCurrentUser().getId() : "ADMIN";
                AuditLogger.log(currentUserId, "PRINT_REPORT", "In báo cáo: " + title);
            }
        } catch (Exception e) {
            UiModalHelper.showToastError(this, "Lỗi khi in báo cáo: " + e.getMessage());
        }
    }
}
