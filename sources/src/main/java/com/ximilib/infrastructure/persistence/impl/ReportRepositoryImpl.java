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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportRepositoryImpl extends BaseJdbcRepository implements ReportPort {

    public ReportRepositoryImpl(DatabaseConnectionManager connectionManager) {
        super(connectionManager);
    }

    @Override
    public ReportKpiSummary getKpiSummary() {
        int totalBooks = queryInt("SELECT COUNT(*) FROM sach WHERE IsDeleted = FALSE");
        int activeLoans = queryInt("SELECT COUNT(*) FROM phieumuon WHERE Trangthai = 1");
        int overdueLoans = queryInt("SELECT COUNT(*) FROM phieumuon WHERE Trangthai = 3 OR (Trangthai = 1 AND Hantranhan < NOW())");

        BigDecimal totalFines = BigDecimal.ZERO;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COALESCE(SUM(Sotien), 0) FROM thanhtoan WHERE Trangthai = 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) totalFines = rs.getBigDecimal(1);
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn tiền phạt báo cáo: " + e.getMessage());
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
        return queryList(sql, rs -> new Object[]{
            rs.getString(1), rs.getString(2), rs.getString(3),
            rs.getDate(4), rs.getDate(5), rs.getDate(6) != null ? rs.getDate(6) : "Chưa trả",
            rs.getString(7)
        });
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
        return queryList(sql, rs -> new Object[]{
            rs.getString(1), rs.getString(2), rs.getString(3),
            rs.getString(4), rs.getDate(5), rs.getBigDecimal(6)
        });
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
        return queryList(sql, rs -> new Object[]{
            rs.getString(1), rs.getString(2), rs.getString(3),
            rs.getString(4), rs.getString(5), rs.getString(6)
        });
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
        return queryList(sql, rs -> new Object[]{
            rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4)
        });
    }

    @Override
    public List<ReportPort.DailyMetric> getBorrowAndFineTrend(int days) {
        int nDays = (days > 0) ? days : 7;
        java.time.LocalDate startDate = java.time.LocalDate.now().minusDays(nDays - 1);

        Map<java.time.LocalDate, Integer> borrowMap = new HashMap<>();
        String borrowSql = "SELECT DATE(Ngaymuon) AS bdate, COUNT(*) AS cnt "
                         + "FROM phieumuon WHERE Ngaymuon >= ? GROUP BY DATE(Ngaymuon)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(borrowSql)) {
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date d = rs.getDate("bdate");
                    if (d != null) {
                        borrowMap.put(d.toLocalDate(), rs.getInt("cnt"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn thống kê lượt mượn: " + e.getMessage());
        }

        Map<java.time.LocalDate, BigDecimal> fineMap = new HashMap<>();
        String fineSql = "SELECT DATE(Ngaythanhtoan) AS fdate, SUM(Sotien) AS total "
                       + "FROM thanhtoan WHERE Trangthai = 1 AND Ngaythanhtoan >= ? GROUP BY DATE(Ngaythanhtoan)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(fineSql)) {
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date d = rs.getDate("fdate");
                    if (d != null) {
                        fineMap.put(d.toLocalDate(), rs.getBigDecimal("total"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn thống kê doanh thu phạt: " + e.getMessage());
        }

        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM");
        List<ReportPort.DailyMetric> result = new ArrayList<>();
        for (int i = 0; i < nDays; i++) {
            java.time.LocalDate date = startDate.plusDays(i);
            int borrows = borrowMap.getOrDefault(date, 0);
            BigDecimal fine = fineMap.getOrDefault(date, BigDecimal.ZERO);
            result.add(new ReportPort.DailyMetric(date.format(fmt), borrows, fine));
        }
        return result;
    }

    @Override
    public void updateLoanStatus(String ticketId, int newStatusCode, String currentUserId) throws Exception {
        runInTransaction(null, conn -> {
            String sql = "UPDATE phieumuon SET Trangthai = ? WHERE Maphieumuon = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, newStatusCode);
                ps.setString(2, ticketId);
                ps.executeUpdate();
            }

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

            String userId = (currentUserId != null && !currentUserId.isBlank()) ? currentUserId : "ADMIN";
            AuditLogger.log(userId, "UPDATE_LOAN_STATUS", "Cập nhật phiếu " + ticketId + " sang trạng thái: " + newStatusCode);
        });
    }
}
