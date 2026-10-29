package com.ximilib.presentation.view;

import com.ximilib.presentation.util.UITheme;
import com.ximilib.presentation.util.UserSession;
import com.ximilib.infrastructure.config.AppContainer;
import com.ximilib.infrastructure.logging.AuditLogger;
import com.ximilib.infrastructure.persistence.connection.DatabaseConnectionManager;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    private final DatabaseConnectionManager connectionManager;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    private JComboBox<String> cmbReportType;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JButton btnUpdateStatus;

    // KPI Labels
    private JLabel lblTotalBooksVal;
    private JLabel lblActiveLoansVal;
    private JLabel lblOverdueVal;
    private JLabel lblTotalFinesVal;

    public ReportsDashboardView() {
        this(AppContainer.getInstance().getConnectionManager());
    }

    public ReportsDashboardView(DatabaseConnectionManager connectionManager) {
        this.connectionManager = (connectionManager != null) ? connectionManager : AppContainer.getInstance().getConnectionManager();

        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 28, 28, 28));

        initUI();
        loadKpisAsync();
        loadReportDataAsync();
    }

    private void initUI() {
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        // 1. Breadcrumb + Title
        JLabel lblBreadcrumb = new JLabel("Trang chủ / Thống kê thư viện");
        lblBreadcrumb.setFont(UITheme.FONT_SMALL);
        lblBreadcrumb.setForeground(UITheme.TEXT_MUTED);

        JLabel lblTitle = new JLabel("Báo Cáo & Thống Kê Định Kỳ (SRS UC-7)");
        lblTitle.setFont(UITheme.FONT_TITLE_LARGE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);

        topPanel.add(lblBreadcrumb);
        topPanel.add(Box.createVerticalStrut(4));
        topPanel.add(lblTitle);
        topPanel.add(Box.createVerticalStrut(16));

        // 2. Hàng 4 thẻ KPI
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        kpiPanel.setOpaque(false);
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
        topPanel.add(Box.createVerticalStrut(18));

        // 3. Thanh điều khiển Bộ lọc & Hành động
        JPanel filterBar = new JPanel(new BorderLayout(12, 0));
        filterBar.setOpaque(false);

        JPanel leftFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
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
        cmbReportType.setPreferredSize(new Dimension(280, 36));
        cmbReportType.addActionListener(e -> loadReportDataAsync());

        JButton btnRefresh = UITheme.createPillButton("Làm mới", UITheme.BG_LIGHT, UITheme.TEXT_MAIN);
        btnRefresh.setPreferredSize(new Dimension(90, 36));
        btnRefresh.addActionListener(e -> {
            loadKpisAsync();
            loadReportDataAsync();
        });

        leftFilter.add(lblSelect);
        leftFilter.add(cmbReportType);
        leftFilter.add(btnRefresh);

        // Nút xuất báo cáo Excel & In & Cập nhật trạng thái
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        btnUpdateStatus = UITheme.createPillButton("Cập nhật trạng thái", UITheme.PRIMARY, Color.WHITE);
        btnUpdateStatus.setPreferredSize(new Dimension(150, 36));
        btnUpdateStatus.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnUpdateStatus.setEnabled(false);
        btnUpdateStatus.addActionListener(e -> showUpdateStatusDialog());

        JButton btnExport = UITheme.createPillButton("Xuất Excel (.csv)", UITheme.TINT, UITheme.ON_TINT);
        btnExport.setPreferredSize(new Dimension(140, 36));
        btnExport.addActionListener(e -> exportToCsv());

        JButton btnPrint = UITheme.createPillButton("In Báo Cáo", new Color(107, 114, 128), Color.WHITE);
        btnPrint.setPreferredSize(new Dimension(110, 36));
        btnPrint.addActionListener(e -> printReport());

        rightActions.add(btnUpdateStatus);
        rightActions.add(btnExport);
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

        reportTable.getSelectionModel().addListSelectionListener(e -> {
            boolean isLoanReport = (cmbReportType.getSelectedIndex() == 0);
            boolean hasSelection = (reportTable.getSelectedRow() >= 0);
            btnUpdateStatus.setEnabled(isLoanReport && hasSelection);
        });

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
        new SwingWorker<Void, Void>() {
            int totalBooks = 0;
            int activeLoans = 0;
            int overdueLoans = 0;
            BigDecimal totalFines = BigDecimal.ZERO;

            @Override
            protected Void doInBackground() {
                try (Connection conn = connectionManager.getConnection()) {
                    // Total books
                    try (var ps = conn.prepareStatement("SELECT COUNT(*) FROM sach WHERE IsDeleted = FALSE");
                         var rs = ps.executeQuery()) {
                        if (rs.next()) totalBooks = rs.getInt(1);
                    }
                    // Active loans
                    try (var ps = conn.prepareStatement("SELECT COUNT(*) FROM phieumuon WHERE Trangthai = 1");
                         var rs = ps.executeQuery()) {
                        if (rs.next()) activeLoans = rs.getInt(1);
                    }
                    // Overdue
                    try (var ps = conn.prepareStatement("SELECT COUNT(*) FROM phieumuon WHERE Trangthai = 3 OR (Trangthai = 1 AND Hantranhan < NOW())");
                         var rs = ps.executeQuery()) {
                        if (rs.next()) overdueLoans = rs.getInt(1);
                    }
                    // Fines paid
                    try (var ps = conn.prepareStatement("SELECT COALESCE(SUM(Sotien), 0) FROM thanhtoan WHERE Trangthai = 1");
                         var rs = ps.executeQuery()) {
                        if (rs.next()) totalFines = rs.getBigDecimal(1);
                    }
                } catch (SQLException ignored) {
                }
                return null;
            }

            @Override
            protected void done() {
                lblTotalBooksVal.setText(String.format("%,d", totalBooks));
                lblActiveLoansVal.setText(String.format("%,d", activeLoans));
                lblOverdueVal.setText(String.format("%,d", overdueLoans));
                lblTotalFinesVal.setText(currencyFormat.format(totalFines != null ? totalFines : BigDecimal.ZERO));
            }
        }.execute();
    }

    private void loadReportDataAsync() {
        int selectedIndex = cmbReportType.getSelectedIndex();
        btnUpdateStatus.setVisible(selectedIndex == 0);
        btnUpdateStatus.setEnabled(false);

        new SwingWorker<DefaultTableModel, Void>() {
            @Override
            protected DefaultTableModel doInBackground() {
                DefaultTableModel model = new DefaultTableModel();
                try (Connection conn = connectionManager.getConnection()) {
                    switch (selectedIndex) {
                        case 0 -> loadLoansReport(conn, model);
                        case 1 -> loadOverdueReport(conn, model);
                        case 2 -> loadDamagedBooksReport(conn, model);
                        case 3 -> loadTopBooksReport(conn, model);
                    }
                } catch (SQLException e) {
                    System.err.println("Lỗi truy vấn báo cáo: " + e.getMessage());
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

    private void loadLoansReport(Connection conn, DefaultTableModel model) throws SQLException {
        model.setColumnIdentifiers(new String[]{"Mã Phiếu", "Bạn Đọc", "Hình Thức", "Ngày Mượn", "Hạn Trả", "Ngày Trả Thực", "Trạng Thái"});
        String sql = """
            SELECT p.Maphieumuon, u.Hoten, p.Hinhthuc, p.Ngaymuon, p.Hantranhan, p.Ngaytra,
                   CASE p.Trangthai 
                       WHEN 0 THEN 'Chờ duyệt'
                       WHEN 1 THEN 'Đang mượn'
                       WHEN 2 THEN 'Đã trả'
                       WHEN 3 THEN 'Quá hạn'
                       WHEN 4 THEN 'Chờ bồi thường'
                       WHEN 5 THEN 'Đã mất'
                       ELSE 'Hủy' END AS StatusTxt
            FROM phieumuon p
            JOIN nguoidung u ON p.ID = u.ID
            ORDER BY p.Ngaymuon DESC LIMIT 100
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString(1), rs.getString(2), rs.getString(3),
                    rs.getDate(4), rs.getDate(5), rs.getDate(6) != null ? rs.getDate(6) : "Chưa trả",
                    rs.getString(7)
                });
            }
        }
    }

    private void loadOverdueReport(Connection conn, DefaultTableModel model) throws SQLException {
        model.setColumnIdentifiers(new String[]{"Bạn Đọc", "Mã Thẻ", "Số ĐT", "Mã Phiếu", "Hạn Trả", "Nợ Phạt Tồn"});
        String sql = """
            SELECT u.Hoten, COALESCE(t.Mathe, 'N/A'), COALESCE(u.Sdt, '-'), p.Maphieumuon, p.Hantranhan,
                   COALESCE((SELECT SUM(Sotienphat) FROM phieuphat WHERE ID = u.ID AND Trangthai = 0), 0) AS TongNo
            FROM phieumuon p
            JOIN nguoidung u ON p.ID = u.ID
            LEFT JOIN thethuvien t ON u.ID = t.ID
            WHERE p.Trangthai = 3 OR (p.Trangthai = 1 AND p.Hantranhan < NOW())
            ORDER BY p.Hantranhan ASC LIMIT 100
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString(1), rs.getString(2), rs.getString(3),
                    rs.getString(4), rs.getDate(5), currencyFormat.format(rs.getBigDecimal(6))
                });
            }
        }
    }

    private void loadDamagedBooksReport(Connection conn, DefaultTableModel model) throws SQLException {
        model.setColumnIdentifiers(new String[]{"Mã Cuốn", "Tựa Sách", "Trạng Thái", "Tình Trạng", "Khu Vực", "Kệ Sách"});
        String sql = """
            SELECT c.Macuonsach, s.Tensach, c.Trangthaisach, COALESCE(c.Tinhtrangsach, '-'),
                   COALESCE(k.Tenkhuvuc, '-'), COALESCE(ks.Tenkesach, '-')
            FROM cuonsach c
            JOIN sach s ON c.Masach = s.Masach
            LEFT JOIN kesach ks ON c.Makesach = ks.Makesach
            LEFT JOIN khuvuc k ON ks.Makhuvuc = k.Makhuvuc
            WHERE c.Trangthaisach IN ('HONG', 'DA_MAT')
            ORDER BY c.Macuonsach LIMIT 100
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString(1), rs.getString(2), rs.getString(3),
                    rs.getString(4), rs.getString(5), rs.getString(6)
                });
            }
        }
    }

    private void loadTopBooksReport(Connection conn, DefaultTableModel model) throws SQLException {
        model.setColumnIdentifiers(new String[]{"Mã Sách", "Tên Đầu Sách", "Thể Loại", "Số Lượt Mượn"});
        String sql = """
            SELECT s.Masach, s.Tensach, COALESCE(tl.Tentheloai, '-'), COUNT(ct.Machitiet) AS SoLuotMuon
            FROM sach s
            LEFT JOIN chitietphieumuon ct ON s.Masach = ct.Masach
            LEFT JOIN theloai tl ON s.Matheloai = tl.Matheloai
            GROUP BY s.Masach, s.Tensach, tl.Tentheloai
            ORDER BY SoLuotMuon DESC LIMIT 10
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4)
                });
            }
        }
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

    private void showUpdateStatusDialog() {
        int row = reportTable.getSelectedRow();
        if (row < 0) {
            UiModalHelper.showToastWarning(this, "Vui lòng chọn một phiếu mượn từ danh sách!");
            return;
        }

        String ticketId = String.valueOf(reportTable.getValueAt(row, 0));
        String readerName = String.valueOf(reportTable.getValueAt(row, 1));
        String currentStatus = String.valueOf(reportTable.getValueAt(row, 6));

        String[] statuses = {
            "0 - Chờ duyệt / Chờ nhận sách",
            "1 - Đang mượn",
            "2 - Đã trả (Hoàn tất)",
            "3 - Quá hạn",
            "4 - Chờ xử lý bồi thường",
            "5 - Đã mất / Đã đền bù",
            "6 - Đã hủy phiếu"
        };

        JComboBox<String> cmbStatus = new JComboBox<>(statuses);
        cmbStatus.setFont(UITheme.FONT_REGULAR);
        cmbStatus.setPreferredSize(new Dimension(300, 36));

        // Chọn index mặc định dựa theo trạng thái hiện tại
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].contains(currentStatus)) {
                cmbStatus.setSelectedIndex(i);
                break;
            }
        }

        JPanel panel = new JPanel(new GridLayout(3, 1, 0, 8));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(340, 110));

        JLabel lblInfo = new JLabel("<html>Phiếu mượn: <b>" + ticketId + "</b> - Bạn đọc: <b>" + readerName + "</b></html>");
        lblInfo.setFont(UITheme.FONT_SMALL);

        JLabel lblPrompt = new JLabel("Chọn trạng thái mới cần cập nhật:");
        lblPrompt.setFont(UITheme.FONT_BOLD);

        panel.add(lblInfo);
        panel.add(lblPrompt);
        panel.add(cmbStatus);

        UiModalHelper.showCustomModal(
            this,
            "UPDATE_STATUS_MODAL",
            "Cập Nhật Trạng Thái Phiếu Mượn",
            panel,
            raven.modal.component.SimpleModalBorder.OK_CANCEL_OPTION,
            (controller, action) -> {
                if (action == raven.modal.component.SimpleModalBorder.OK_OPTION) {
                    controller.close();
                    int selectedStatusCode = cmbStatus.getSelectedIndex();
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
                try (Connection conn = connectionManager.getConnection()) {
                    conn.setAutoCommit(false);
                    try {
                        String sql = "UPDATE phieumuon SET Trangthai = ? WHERE Maphieumuon = ?";
                        try (PreparedStatement ps = conn.prepareStatement(sql)) {
                            ps.setInt(1, newStatusCode);
                            ps.setString(2, ticketId);
                            ps.executeUpdate();
                        }

                        // Nếu cập nhật sang Đã trả (2) hoặc Đã hủy (6), tự động giải phóng sách về KHA_DUNG
                        if (newStatusCode == 2 || newStatusCode == 6) {
                            String sqlFreeCopies = """
                                UPDATE cuonsach c
                                JOIN chitietphieumuon ct ON c.Macuonsach = ct.Macuonsach
                                SET c.Trangthaisach = 'KHA_DUNG'
                                WHERE ct.Maphieumuon = ?
                            """;
                            try (PreparedStatement psFree = conn.prepareStatement(sqlFreeCopies)) {
                                psFree.setString(1, ticketId);
                                psFree.executeUpdate();
                            }
                        } else if (newStatusCode == 1 || newStatusCode == 3) {
                            // Đang mượn hoặc Quá hạn -> Sách ở trạng thái DANG_MUON
                            String sqlBusyCopies = """
                                UPDATE cuonsach c
                                JOIN chitietphieumuon ct ON c.Macuonsach = ct.Macuonsach
                                SET c.Trangthaisach = 'DANG_MUON'
                                WHERE ct.Maphieumuon = ?
                            """;
                            try (PreparedStatement psBusy = conn.prepareStatement(sqlBusyCopies)) {
                                psBusy.setString(1, ticketId);
                                psBusy.executeUpdate();
                            }
                        } else if (newStatusCode == 4 || newStatusCode == 5) {
                            // Chờ bồi thường hoặc Đã mất -> HONG_MAT
                            String sqlLostCopies = """
                                UPDATE cuonsach c
                                JOIN chitietphieumuon ct ON c.Macuonsach = ct.Macuonsach
                                SET c.Trangthaisach = 'HONG_MAT'
                                WHERE ct.Maphieumuon = ?
                            """;
                            try (PreparedStatement psLost = conn.prepareStatement(sqlLostCopies)) {
                                psLost.setString(1, ticketId);
                                psLost.executeUpdate();
                            }
                        }

                        conn.commit();
                        String currentUserId = UserSession.getCurrentUser() != null ? UserSession.getCurrentUser().getId() : "ADMIN";
                        AuditLogger.log(currentUserId, "UPDATE_LOAN_STATUS", "Cập nhật phiếu " + ticketId + " sang trạng thái: " + newStatusCode);
                    } catch (Exception ex) {
                        conn.rollback();
                        throw ex;
                    }
                }
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
