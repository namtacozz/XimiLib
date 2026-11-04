package com.ximilib.presentation.view;

import com.ximilib.application.port.ReportPort;
import com.ximilib.presentation.util.UITheme;
import com.ximilib.presentation.util.UserSession;
import com.ximilib.infrastructure.config.AppContainer;
import com.ximilib.infrastructure.logging.AuditLogger;
import com.ximilib.presentation.util.UiModalHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Vector;

/**
 * ReportsDashboardView: Màn hình Báo Cáo & Thống Kê Định Kỳ (SRS UC-7, F-12).
 * Cung cấp 4 loại báo cáo:
 * 1. Thống kê lượt mượn/trả
 * 2. Độc giả quá hạn & nợ phạt
 * 3. Sách hỏng/mất cần thanh lý
 * 4. Top 10 đầu sách mượn nhiều nhất
 * Cho phép kết xuất file Excel (.csv UTF-8 BOM) và In báo cáo kèm Audit Log.
 * Tuân thủ R-01, R-06, R-08, R-11.
 */
public class ReportsDashboardView extends JPanel {

    private final ReportPort reportPort;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    private JComboBox<String> cmbReportType;
    private final com.ximilib.application.port.ReportExporter reportExporter;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JButton btnUpdateStatus;
    private com.ximilib.presentation.component.TrendChartPanel trendChartPanel;

    // KPI Labels
    private JLabel lblTotalBooksVal;
    private JLabel lblActiveLoansVal;
    private JLabel lblOverdueVal;
    private JLabel lblTotalFinesVal;

    public ReportsDashboardView() {
        this(AppContainer.getInstance().getReportPort(), AppContainer.getInstance().getReportExporter());
    }

    public ReportsDashboardView(ReportPort reportPort) {
        this(reportPort, AppContainer.getInstance().getReportExporter());
    }

    public ReportsDashboardView(ReportPort reportPort, com.ximilib.application.port.ReportExporter reportExporter) {
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

        // 1. Breadcrumb + Title
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
        topPanel.add(Box.createVerticalStrut(16));

        // 2. Hàng 4 thẻ KPI
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        kpiPanel.setOpaque(false);
        kpiPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        kpiPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        lblTotalBooksVal = new JLabel("...", SwingConstants.CENTER);
        lblActiveLoansVal = new JLabel("...", SwingConstants.CENTER);
        lblOverdueVal = new JLabel("...", SwingConstants.CENTER);
        lblTotalFinesVal = new JLabel("...", SwingConstants.CENTER);

        kpiPanel.add(createMiniKpiCard("Tổng đầu sách", lblTotalBooksVal, UITheme.PRIMARY));
        kpiPanel.add(createMiniKpiCard("Đang cho mượn", lblActiveLoansVal, new Color(59, 130, 246)));
        kpiPanel.add(createMiniKpiCard("Phiếu quá hạn", lblOverdueVal, UITheme.DANGER_TEXT));
        kpiPanel.add(createMiniKpiCard("Tiền phạt đã thu", lblTotalFinesVal, new Color(16, 185, 129)));

        topPanel.add(kpiPanel);
        topPanel.add(Box.createVerticalStrut(14));

        trendChartPanel = new com.ximilib.presentation.component.TrendChartPanel();
        trendChartPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(trendChartPanel);
        topPanel.add(Box.createVerticalStrut(14));

        // 3. Thanh điều khiển Bộ lọc & Hành động
        JPanel filterBar = new JPanel(new BorderLayout(10, 0));
        filterBar.setOpaque(false);
        filterBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel leftFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftFilter.setOpaque(false);

        JLabel lblSelect = new JLabel("Loại báo cáo:");
        lblSelect.setFont(UITheme.FONT_BOLD);
        lblSelect.setForeground(UITheme.TEXT_MAIN);

        cmbReportType = new JComboBox<>(new String[]{
            "1. Thống kê lượt mượn/trả",
            "2. Độc giả quá hạn & nợ phạt",
            "3. Sách hỏng/mất cần thanh lý",
            "4. Top 10 đầu sách mượn nhiều nhất"
        });
        cmbReportType.setFont(UITheme.FONT_REGULAR);
        cmbReportType.setPreferredSize(new Dimension(230, 36));
        cmbReportType.addActionListener(e -> loadReportDataAsync());

        JButton btnRefresh = UITheme.createPillButton("Làm mới", UITheme.BG_LIGHT, UITheme.TEXT_MAIN);
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.setMargin(new Insets(2, 8, 2, 8));
        btnRefresh.setPreferredSize(new Dimension(92, 36));
        btnRefresh.addActionListener(e -> {
            loadKpisAsync();
            loadChartAsync();
            loadReportDataAsync();
        });

        leftFilter.add(lblSelect);
        leftFilter.add(cmbReportType);
        leftFilter.add(btnRefresh);

        // Nút xuất báo cáo Excel & In & Cập nhật trạng thái
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        btnUpdateStatus = UITheme.createPillButton("Cập nhật trạng thái", UITheme.TINT, UITheme.ON_TINT);
        btnUpdateStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnUpdateStatus.setMargin(new Insets(2, 10, 2, 10));
        btnUpdateStatus.setPreferredSize(new Dimension(162, 36));
        btnUpdateStatus.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnUpdateStatus.addActionListener(e -> showUpdateStatusDialog());

        JButton btnExportXlsx = UITheme.createPillButton("Xuất Excel (.xlsx)", UITheme.PRIMARY, Color.WHITE);
        btnExportXlsx.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExportXlsx.setMargin(new Insets(2, 10, 2, 10));
        btnExportXlsx.setPreferredSize(new Dimension(150, 36));
        btnExportXlsx.addActionListener(e -> exportToXlsx());

        JButton btnPrint = UITheme.createPillButton("In Báo Cáo", new Color(107, 114, 128), Color.WHITE);
        btnPrint.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPrint.setMargin(new Insets(2, 8, 2, 8));
        btnPrint.setPreferredSize(new Dimension(110, 36));
        btnPrint.addActionListener(e -> printReport());

        rightActions.add(btnUpdateStatus);
        rightActions.add(btnExportXlsx);
        rightActions.add(btnPrint);

        filterBar.add(leftFilter, BorderLayout.WEST);
        filterBar.add(rightActions, BorderLayout.EAST);
        topPanel.add(filterBar);
        topPanel.add(Box.createVerticalStrut(12));

        add(topPanel, BorderLayout.NORTH);

        // 4. Bảng hiển thị kết quả
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

    private JPanel createMiniKpiCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1, true),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_SMALL);
        lblTitle.setForeground(UITheme.TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(accentColor);
        valueLabel.setHorizontalAlignment(SwingConstants.LEFT);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
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
        new SwingWorker<java.util.List<ReportPort.DailyMetric>, Void>() {
            @Override
            protected java.util.List<ReportPort.DailyMetric> doInBackground() {
                return reportPort.getBorrowAndFineTrend(7);
            }

            @Override
            protected void done() {
                try {
                    java.util.List<ReportPort.DailyMetric> metrics = get();
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

        new SwingWorker<DefaultTableModel, Void>() {
            @Override
            protected DefaultTableModel doInBackground() {
                DefaultTableModel model = new DefaultTableModel();
                switch (selectedIndex) {
                    case 0 -> {
                        model.setColumnIdentifiers(new String[]{"Mã Phiếu", "Bạn Đọc", "Hình Thức", "Ngày Mượn", "Hạn Trả", "Ngày Trả Thực", "Trạng Thái"});
                        for (Object[] row : reportPort.getLoansReport()) {
                            model.addRow(row);
                        }
                    }
                    case 1 -> {
                        model.setColumnIdentifiers(new String[]{"Bạn Đọc", "Mã Thẻ", "Số ĐT", "Mã Phiếu", "Hạn Trả", "Nợ Phạt Tồn"});
                        for (Object[] row : reportPort.getOverdueReport()) {
                            BigDecimal fine = (row[5] instanceof BigDecimal bd) ? bd : BigDecimal.ZERO;
                            row[5] = currencyFormat.format(fine);
                            model.addRow(row);
                        }
                    }
                    case 2 -> {
                        model.setColumnIdentifiers(new String[]{"Mã Cuốn", "Tựa Sách", "Trạng Thái", "Tình Trạng", "Khu Vực", "Kệ Sách"});
                        for (Object[] row : reportPort.getDamagedBooksReport()) {
                            model.addRow(row);
                        }
                    }
                    case 3 -> {
                        model.setColumnIdentifiers(new String[]{"Mã Sách", "Tên Đầu Sách", "Thể Loại", "Số Lượt Mượn"});
                        for (Object[] row : reportPort.getTopBooksReport()) {
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

    private void exportToCsv() {
        if (reportTable.getRowCount() == 0) {
            UiModalHelper.showToastWarning(this, "Không có dữ liệu báo cáo để xuất!");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu file báo cáo Excel CSV");
        fileChooser.setSelectedFile(new File("BaoCao_" + System.currentTimeMillis() + ".csv"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (FileOutputStream fos = new FileOutputStream(file);
                 OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                 PrintWriter pw = new PrintWriter(osw)) {

                // Ghi UTF-8 BOM để Excel tự nhận diện Unicode không bị lỗi font
                fos.write(0xEF);
                fos.write(0xBB);
                fos.write(0xBF);

                // Ghi tiêu đề cột
                for (int i = 0; i < reportTable.getColumnCount(); i++) {
                    pw.print("\"" + reportTable.getColumnName(i) + "\"");
                    if (i < reportTable.getColumnCount() - 1) pw.print(",");
                }
                pw.println();

                // Ghi các hàng dữ liệu
                for (int r = 0; r < reportTable.getRowCount(); r++) {
                    for (int c = 0; c < reportTable.getColumnCount(); c++) {
                        Object val = reportTable.getValueAt(r, c);
                        String strVal = (val != null) ? val.toString().replace("\"", "\"\"") : "";
                        pw.print("\"" + strVal + "\"");
                        if (c < reportTable.getColumnCount() - 1) pw.print(",");
                    }
                    pw.println();
                }

                String currentUserId = UserSession.getCurrentUser() != null ? UserSession.getCurrentUser().getId() : "ADMIN";
                AuditLogger.log(currentUserId, "EXPORT_REPORT", "Xuất file báo cáo: " + file.getName());

                UiModalHelper.showToastSuccess(this, "Xuất báo cáo thành công: " + file.getName());
            } catch (Exception e) {
                UiModalHelper.showToastError(this, "Lỗi khi xuất file: " + e.getMessage());
            }
        }
    }

    private void exportToXlsx() {
        if (reportTable.getRowCount() == 0) {
            UiModalHelper.showToastWarning(this, "Không có dữ liệu báo cáo để xuất!");
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
            java.util.List<Object[]> rows = new java.util.ArrayList<>(rowCount);
            for (int r = 0; r < rowCount; r++) {
                Object[] row = new Object[colCount];
                for (int c = 0; c < colCount; c++) {
                    row[c] = reportTable.getValueAt(r, c);
                }
                rows.add(row);
            }

            String title = (String) cmbReportType.getSelectedItem();
            if (title != null && title.length() > 30) {
                title = title.substring(0, 30);
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

        // Màu sắc cho Huy hiệu trạng thái hiện tại
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

        // 1. Thẻ tóm tắt thông tin phiếu (Summary Info Card)
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
        cardInfo.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JPanel lineTop = new JPanel(new BorderLayout());
        lineTop.setOpaque(false);

        JLabel lblTicket = new JLabel("Mã phiếu: " + ticketId);
        lblTicket.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTicket.setForeground(UITheme.TEXT_MAIN);

        JLabel lblBadge = new JLabel(currentStatus) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(finalCurBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBadge.setForeground(finalCurFg);
        lblBadge.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));

        lineTop.add(lblTicket, BorderLayout.WEST);
        lineTop.add(lblBadge, BorderLayout.EAST);

        JLabel lblReader = new JLabel("Bạn đọc:  " + readerName);
        lblReader.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblReader.setForeground(UITheme.TEXT_MUTED);

        cardInfo.add(lineTop, BorderLayout.NORTH);
        cardInfo.add(lblReader, BorderLayout.SOUTH);

        // 2. ComboBox chọn trạng thái mới với Vector Bullet Dot
        JComboBox<StatusOption> cmbStatus = new JComboBox<>(options);
        cmbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbStatus.setPreferredSize(new Dimension(390, 38));
        cmbStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cmbStatus.setSelectedIndex(defaultIndex);
        cmbStatus.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                if (value instanceof StatusOption opt) {
                    lbl.setText(opt.label);
                    lbl.setIcon(createDotIcon(opt.dotColor, 9));
                }
                return lbl;
            }
        });

        // 3. Hộp thông điệp nghiệp vụ động (Dynamic Note Card)
        JPanel pnlNote = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(0, 0, 0, 20));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pnlNote.setOpaque(false);
        pnlNote.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        pnlNote.setBackground(new Color(241, 245, 249));

        JLabel lblNote = new JLabel("• " + options[defaultIndex].note);
        lblNote.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblNote.setForeground(new Color(51, 65, 85));
        pnlNote.add(lblNote, BorderLayout.CENTER);

        cmbStatus.addActionListener(e -> {
            StatusOption selected = (StatusOption) cmbStatus.getSelectedItem();
            if (selected != null) {
                if (selected.code == 4 || selected.code == 3) {
                    pnlNote.setBackground(new Color(254, 243, 199));
                    lblNote.setForeground(new Color(146, 64, 14));
                    lblNote.setText("• Lưu ý: " + selected.note);
                } else {
                    pnlNote.setBackground(new Color(241, 245, 249));
                    lblNote.setForeground(new Color(51, 65, 85));
                    lblNote.setText("• " + selected.note);
                }
                pnlNote.repaint();
            }
        });

        // Bố cục tổng thể của modal
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(400, 185));

        cardInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(cardInfo);
        panel.add(Box.createVerticalStrut(12));

        JLabel lblPrompt = new JLabel("Chọn trạng thái mới cần cập nhật:");
        lblPrompt.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPrompt.setForeground(UITheme.TEXT_MAIN);
        lblPrompt.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblPrompt);
        panel.add(Box.createVerticalStrut(6));

        cmbStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(cmbStatus);
        panel.add(Box.createVerticalStrut(10));

        pnlNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(pnlNote);

        UiModalHelper.showCustomModal(
            this,
            "UPDATE_STATUS_MODAL",
            "Cập Nhật Trạng Thái Phiếu Mượn",
            panel,
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
