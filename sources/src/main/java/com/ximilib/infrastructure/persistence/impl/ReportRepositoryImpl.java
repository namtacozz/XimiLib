package com.ximilib.infrastructure.persistence.impl;

import com.ximilib.application.port.ReportPort;
import com.ximilib.infrastructure.logging.AuditLogger;
import com.ximilib.infrastructure.persistence.connection.DatabaseConnectionManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * ReportRepositoryImpl: Triển khai ReportPort bằng JDBC PreparedStatement kết nối MySQL.
 * Giữ nguyên 100% các câu lệnh SQL và logic transaction phân định trạng thái cuốn sách theo quy định R-05, R-07.
 */
public class ReportRepositoryImpl implements ReportPort {

    private final DatabaseConnectionManager connectionManager;

    public ReportRepositoryImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public ReportKpiSummary getKpiSummary() {
        int totalBooks = 0;
        int activeLoans = 0;
        int overdueLoans = 0;
        BigDecimal totalFines = BigDecimal.ZERO;

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
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn KPI báo cáo: " + e.getMessage());
        }

        return new ReportKpiSummary(totalBooks, activeLoans, overdueLoans, totalFines);
    }

    @Override
    public List<Object[]> getLoansReport() {
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
        List<Object[]> rows = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new Object[]{
                    rs.getString(1), rs.getString(2), rs.getString(3),
                    rs.getDate(4), rs.getDate(5), rs.getDate(6) != null ? rs.getDate(6) : "Chưa trả",
                    rs.getString(7)
                });
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn báo cáo mượn trả: " + e.getMessage());
        }
        return rows;
    }

    @Override
    public List<Object[]> getOverdueReport() {
        String sql = """
            SELECT u.Hoten, COALESCE(t.Mathe, 'N/A'), COALESCE(u.Sdt, '-'), p.Maphieumuon, p.Hantranhan,
                   COALESCE((SELECT SUM(Sotienphat) FROM phieuphat WHERE ID = u.ID AND Trangthai = 0), 0) AS TongNo
            FROM phieumuon p
            JOIN nguoidung u ON p.ID = u.ID
            LEFT JOIN thethuvien t ON u.ID = t.ID
            WHERE p.Trangthai = 3 OR (p.Trangthai = 1 AND p.Hantranhan < NOW())
            ORDER BY p.Hantranhan ASC LIMIT 100
        """;
        List<Object[]> rows = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new Object[]{
                    rs.getString(1), rs.getString(2), rs.getString(3),
                    rs.getString(4), rs.getDate(5), rs.getBigDecimal(6)
                });
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn báo cáo quá hạn: " + e.getMessage());
        }
        return rows;
    }

    @Override
    public List<Object[]> getDamagedBooksReport() {
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
        List<Object[]> rows = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new Object[]{
                    rs.getString(1), rs.getString(2), rs.getString(3),
                    rs.getString(4), rs.getString(5), rs.getString(6)
                });
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn sách hỏng/mất: " + e.getMessage());
        }
        return rows;
    }

    @Override
    public List<Object[]> getTopBooksReport() {
        String sql = """
            SELECT s.Masach, s.Tensach, COALESCE(tl.Tentheloai, '-'), COUNT(ct.Machitiet) AS SoLuotMuon
            FROM sach s
            LEFT JOIN chitietphieumuon ct ON s.Masach = ct.Masach
            LEFT JOIN theloai tl ON s.Matheloai = tl.Matheloai
            GROUP BY s.Masach, s.Tensach, tl.Tentheloai
            ORDER BY SoLuotMuon DESC LIMIT 10
        """;
        List<Object[]> rows = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new Object[]{
                    rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4)
                });
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn top sách: " + e.getMessage());
        }
        return rows;
    }

    @Override
    public void updateLoanStatus(String ticketId, int newStatusCode, String currentUserId) throws Exception {
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
                String userId = (currentUserId != null && !currentUserId.isBlank()) ? currentUserId : "ADMIN";
                AuditLogger.log(userId, "UPDATE_LOAN_STATUS", "Cập nhật phiếu " + ticketId + " sang trạng thái: " + newStatusCode);
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
