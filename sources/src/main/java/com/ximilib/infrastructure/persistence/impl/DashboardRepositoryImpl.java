package com.ximilib.infrastructure.persistence.impl;

import com.ximilib.application.dto.ReaderDashboardDto;
import com.ximilib.application.dto.StaffDashboardDto;
import com.ximilib.application.port.DashboardQueryPort;
import com.ximilib.domain.model.LoanDueClassification;
import com.ximilib.domain.repository.DashboardRepository;
import com.ximilib.infrastructure.persistence.connection.DatabaseConnectionManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * DashboardRepositoryImpl: Hiện thực truy vấn CSDL MySQL cho dữ liệu Trang chủ.
 * Tuân thủ R-01, R-07 (100% PreparedStatement, tham số ?).
 */
public class DashboardRepositoryImpl implements DashboardRepository, DashboardQueryPort {

    private final DatabaseConnectionManager connectionManager;

    public DashboardRepositoryImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public StaffDashboardDto.StaffKpiSummary getStaffKpiSummary() {
        int totalTitles = 0;
        int availableCopies = 0;
        int activeBorrowCopies = 0;
        int activeReaders = 0;
        int overdueLoans = 0;

        String sqlTitles = "SELECT COUNT(*) FROM sach WHERE IsDeleted = FALSE";
        String sqlAvailable = "SELECT COUNT(*) FROM cuonsach WHERE Trangthaisach = 'KHA_DUNG'";
        String sqlActiveCopies = "SELECT COUNT(c.Macuonsach) FROM chitietphieumuon c JOIN phieumuon p ON c.Maphieumuon = p.Maphieumuon WHERE p.Trangthai IN (1, 3)";
        String sqlReaders = "SELECT COUNT(*) FROM nguoidung WHERE Mavaitro = 'READER' AND Trangthai = 1";
        String sqlOverdue = "SELECT COUNT(DISTINCT p.Maphieumuon) FROM phieumuon p WHERE p.Trangthai = 3 OR (p.Trangthai = 1 AND p.Ngaytra < CURRENT_DATE())";

        try (Connection conn = connectionManager.getConnection()) {
            totalTitles = querySingleInt(conn, sqlTitles);
            availableCopies = querySingleInt(conn, sqlAvailable);
            activeBorrowCopies = querySingleInt(conn, sqlActiveCopies);
            activeReaders = querySingleInt(conn, sqlReaders);
            overdueLoans = querySingleInt(conn, sqlOverdue);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi truy vấn số liệu KPI Trang chủ: " + e.getMessage(), e);
        }

        return new StaffDashboardDto.StaffKpiSummary(
            totalTitles, availableCopies, activeBorrowCopies, activeReaders, overdueLoans
        );
    }

    private int querySingleInt(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public List<StaffDashboardDto.TodoLoanItem> getTodoLoans(LocalDate today, int limit) {
        List<StaffDashboardDto.TodoLoanItem> items = new ArrayList<>();
        String sql = """
            SELECT p.Maphieumuon, u.Hoten as ReaderName, p.Ngaytra, p.Trangthai,
                   (SELECT s.Tensach FROM chitietphieumuon ct
                    JOIN cuonsach cs ON ct.Macuonsach = cs.Macuonsach
                    JOIN sach s ON cs.Masach = s.Masach
                    WHERE ct.Maphieumuon = p.Maphieumuon LIMIT 1) as TenSachDau,
                   (SELECT COUNT(*) FROM chitietphieumuon WHERE Maphieumuon = p.Maphieumuon) as TongSoCuon
            FROM phieumuon p
            JOIN nguoidung u ON p.ID = u.ID
            WHERE p.Trangthai IN (1, 3) AND p.Ngaytra <= DATE_ADD(?, INTERVAL 1 DAY)
            ORDER BY (CASE WHEN p.Ngaytra < ? THEN 0 ELSE 1 END) ASC, p.Ngaytra ASC
            LIMIT ?
        """;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(today));
            ps.setDate(2, Date.valueOf(today));
            ps.setInt(3, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ticketId = rs.getString("Maphieumuon");
                    String readerName = rs.getString("ReaderName");
                    Date dueDateRaw = rs.getDate("Ngaytra");
                    LocalDate dueDate = (dueDateRaw != null) ? dueDateRaw.toLocalDate() : today;
                    String firstBookTitle = rs.getString("TenSachDau");
                    int totalCopies = rs.getInt("TongSoCuon");
                    int additionalCount = Math.max(0, totalCopies - 1);

                    LoanDueClassification classification = LoanDueClassification.classify(dueDate, today);

                    items.add(new StaffDashboardDto.TodoLoanItem(
                        ticketId, readerName, firstBookTitle, additionalCount, dueDate, classification
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi truy vấn danh sách cần xử lý hôm nay: " + e.getMessage(), e);
        }

        return items;
    }

    @Override
    public List<StaffDashboardDto.PendingPickupItem> getPendingPickups(int limit) {
        List<StaffDashboardDto.PendingPickupItem> items = new ArrayList<>();
        String sql = """
            SELECT p.Maphieumuon, u.Hoten as ReaderName, p.Ngaymuon, p.Hantranhan,
                   (SELECT s.Tensach FROM chitietphieumuon ct
                    JOIN cuonsach cs ON ct.Macuonsach = cs.Macuonsach
                    JOIN sach s ON cs.Masach = s.Masach
                    WHERE ct.Maphieumuon = p.Maphieumuon LIMIT 1) as TenSachDau
            FROM phieumuon p
            JOIN nguoidung u ON p.ID = u.ID
            WHERE p.Trangthai = 0
            ORDER BY p.Ngaymuon ASC
            LIMIT ?
        """;

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ticketId = rs.getString("Maphieumuon");
                    String readerName = rs.getString("ReaderName");
                    Date orderDateRaw = rs.getDate("Ngaymuon");
                    LocalDate orderDate = (orderDateRaw != null) ? orderDateRaw.toLocalDate() : LocalDate.now();
                    Timestamp deadlineTs = rs.getTimestamp("Hantranhan");
                    String deadlineNote = (deadlineTs != null)
                        ? "Hạn: " + deadlineTs.toLocalDateTime().format(dtf)
                        : "Chờ nhận tại quầy";
                    String firstBookTitle = rs.getString("TenSachDau");

                    items.add(new StaffDashboardDto.PendingPickupItem(
                        ticketId, readerName, firstBookTitle, orderDate, deadlineNote
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi truy vấn phiếu chờ nhận sách: " + e.getMessage(), e);
        }

        return items;
    }

    @Override
    public List<StaffDashboardDto.NewBookTileDto> getNewBooks(int limit) {
        List<StaffDashboardDto.NewBookTileDto> items = new ArrayList<>();
        String sql = """
            SELECT s.Masach, s.Tensach, s.Hinhanh,
                   COALESCE(GROUP_CONCAT(DISTINCT tg.Tentacgia SEPARATOR ', '), 'Nhiều tác giả') as Tentacgia,
                   SUM(CASE WHEN c.Trangthaisach = 'KHA_DUNG' THEN 1 ELSE 0 END) as SoCuonKhaDung
            FROM sach s
            LEFT JOIN cuonsach c ON s.Masach = c.Masach
            LEFT JOIN sach_tacgia st ON s.Masach = st.Masach
            LEFT JOIN tacgia tg ON st.Matacgia = tg.Matacgia
            LEFT JOIN chitietnhapsach ctn ON s.Masach = ctn.Masach
            LEFT JOIN nhapsach ns ON ctn.Manhapsach = ns.Manhapsach
            WHERE s.IsDeleted = FALSE
            GROUP BY s.Masach, s.Tensach, s.Hinhanh
            ORDER BY MAX(COALESCE(ns.Ngaynhap, '1970-01-01')) DESC, s.Masach DESC
            LIMIT ?
        """;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String bookId = rs.getString("Masach");
                    String title = rs.getString("Tensach");
                    String coverPath = rs.getString("Hinhanh");
                    String author = rs.getString("Tentacgia");
                    int availableCopies = rs.getInt("SoCuonKhaDung");

                    items.add(new StaffDashboardDto.NewBookTileDto(
                        bookId, title, author, coverPath, availableCopies
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi truy vấn danh sách sách mới nhập: " + e.getMessage(), e);
        }

        return items;
    }

    @Override
    public int countUnreadNotifications(String userId) {
        String sql = "SELECT COUNT(*) FROM thongbao WHERE ID = ? AND Dadoc = 0";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            return 0;
        }
        return 0;
    }

    @Override
    public ReaderDashboardDto.ReaderSummary getReaderSummary(String userId) {
        String fullName = "";
        String cardNumber = "Chưa cấp thẻ";
        boolean cardActive = false;
        boolean cardExpired = false;
        int activeLoans = 0;
        int overdueLoans = 0;
        BigDecimal unpaidFine = BigDecimal.ZERO;

        String sqlUser = """
            SELECT u.Hoten, tv.Mathe, tv.Trangthai as CardStatus, tv.Ngayhethan
            FROM nguoidung u
            LEFT JOIN thethuvien tv ON u.ID = tv.ID
            WHERE u.ID = ?
        """;

        String sqlLoans = """
            SELECT COUNT(DISTINCT ct.Macuonsach) as TotalLoans,
                   SUM(CASE WHEN p.Ngaytra < CURRENT_DATE() OR p.Trangthai = 3 THEN 1 ELSE 0 END) as OverdueLoans
            FROM phieumuon p
            JOIN chitietphieumuon ct ON p.Maphieumuon = ct.Maphieumuon
            WHERE p.ID = ? AND p.Trangthai IN (1, 3) AND ct.Ngaytra IS NULL
        """;

        String sqlFines = "SELECT COALESCE(SUM(Sotienphat), 0) FROM phieuphat WHERE ID = ? AND Trangthai = 0";

        try (Connection conn = connectionManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sqlUser)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        fullName = rs.getString("Hoten");
                        String cNum = rs.getString("Mathe");
                        if (cNum != null && !cNum.isBlank()) {
                            cardNumber = cNum;
                            cardActive = (rs.getInt("CardStatus") == 1);
                            Date expiry = rs.getDate("Ngayhethan");
                            if (expiry != null && expiry.toLocalDate().isBefore(LocalDate.now())) {
                                cardExpired = true;
                            }
                        }
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlLoans)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        activeLoans = rs.getInt("TotalLoans");
                        overdueLoans = rs.getInt("OverdueLoans");
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlFines)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        unpaidFine = rs.getBigDecimal(1);
                        if (unpaidFine == null) unpaidFine = BigDecimal.ZERO;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi truy vấn thông tin độc giả: " + e.getMessage(), e);
        }

        return new ReaderDashboardDto.ReaderSummary(
            userId, fullName, cardNumber, cardActive, cardExpired, activeLoans, overdueLoans, unpaidFine
        );
    }

    @Override
    public List<ReaderDashboardDto.ReaderActiveLoan> getReaderActiveLoans(String userId, LocalDate today) {
        List<ReaderDashboardDto.ReaderActiveLoan> items = new ArrayList<>();
        String sql = """
            SELECT p.Maphieumuon, ct.Machitiet, ct.Macuonsach, s.Masach, s.Tensach, s.Hinhanh,
                   COALESCE((SELECT GROUP_CONCAT(tg.Tentacgia SEPARATOR ', ')
                             FROM sach_tacgia st JOIN tacgia tg ON st.Matacgia = tg.Matacgia
                             WHERE st.Masach = s.Masach), 'Nhiều tác giả') as Tentacgia,
                   p.Ngaymuon, p.Ngaytra,
                   (SELECT COUNT(*) FROM giahanmuon gh WHERE gh.Maphieumuon = p.Maphieumuon AND gh.Trangthai = 1) as SoLanGiaHan,
                   (SELECT COUNT(*) FROM datcho dc WHERE dc.Masach = s.Masach AND dc.Trangthai = 1) as SoLuotDatTruoc,
                   (SELECT COALESCE(SUM(Sotienphat), 0) FROM phieuphat pp WHERE pp.ID = p.ID AND pp.Trangthai = 0) as NoPhat
            FROM phieumuon p
            JOIN chitietphieumuon ct ON p.Maphieumuon = ct.Maphieumuon
            JOIN cuonsach cs ON ct.Macuonsach = cs.Macuonsach
            JOIN sach s ON cs.Masach = s.Masach
            WHERE p.ID = ? AND p.Trangthai IN (1, 3) AND ct.Ngaytra IS NULL
            ORDER BY p.Ngaytra ASC
        """;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ticketId = rs.getString("Maphieumuon");
                    String detailId = rs.getString("Machitiet");
                    String copyId = rs.getString("Macuonsach");
                    String bookId = rs.getString("Masach");
                    String bookTitle = rs.getString("Tensach");
                    String authorName = rs.getString("Tentacgia");
                    String coverImage = rs.getString("Hinhanh");
                    Date borrowDateRaw = rs.getDate("Ngaymuon");
                    LocalDate borrowDate = (borrowDateRaw != null) ? borrowDateRaw.toLocalDate() : today;
                    Date dueDateRaw = rs.getDate("Ngaytra");
                    LocalDate dueDate = (dueDateRaw != null) ? dueDateRaw.toLocalDate() : today;

                    int soLanGiaHan = rs.getInt("SoLanGiaHan");
                    int soLuotDatTruoc = rs.getInt("SoLuotDatTruoc");
                    BigDecimal noPhat = rs.getBigDecimal("NoPhat");

                    LoanDueClassification classification = LoanDueClassification.classify(dueDate, today);

                    boolean isRenewed = (soLanGiaHan >= 1);
                    boolean canRenew = true;
                    String renewDisabledReason = "";

                    if (isRenewed) {
                        canRenew = false;
                        renewDisabledReason = "Đã gia hạn 1 lần";
                    } else if (classification.isOverdue()) {
                        canRenew = false;
                        renewDisabledReason = "Sách đã quá hạn";
                    } else if (noPhat != null && noPhat.compareTo(BigDecimal.ZERO) > 0) {
                        canRenew = false;
                        renewDisabledReason = "Đang nợ tiền phạt";
                    } else if (soLuotDatTruoc > 0) {
                        canRenew = false;
                        renewDisabledReason = "Có người đặt trước";
                    }

                    items.add(new ReaderDashboardDto.ReaderActiveLoan(
                        ticketId, detailId, copyId, bookId, bookTitle, authorName, coverImage,
                        borrowDate, dueDate, isRenewed, canRenew, renewDisabledReason, classification
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi truy vấn sách đang mượn của độc giả: " + e.getMessage(), e);
        }

        return items;
    }
}
