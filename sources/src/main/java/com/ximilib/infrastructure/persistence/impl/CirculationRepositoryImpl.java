package com.ximilib.infrastructure.persistence.impl;

import com.ximilib.application.dto.BorrowTicketDto;
import com.ximilib.application.port.CirculationQueryPort;
import com.ximilib.domain.model.LoanType;
import com.ximilib.domain.repository.CirculationRepository;
import com.ximilib.infrastructure.persistence.connection.DatabaseConnectionManager;

import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * CirculationRepositoryImpl: Hiện thực thao tác CSDL cho Lưu hành mượn trả và bồi hoàn sách.
 * Tuân thủ R-01, R-04, R-05 (Transaction, Locking), R-07 (PreparedStatement).
 */
public class CirculationRepositoryImpl implements CirculationRepository, CirculationQueryPort {

    private final DatabaseConnectionManager connectionManager;

    public CirculationRepositoryImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = (connectionManager != null) ? connectionManager : new DatabaseConnectionManager();
    }

    @Override
    public Optional<UserCardInfo> findCardByIdentifier(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) return Optional.empty();
        String sql = """
            SELECT u.ID, u.Hoten, t.Mathe, t.Trangthai, t.Ngayhethan
            FROM nguoidung u
            JOIN thethuvien t ON u.ID = t.ID AND t.Trangthai = 1
            WHERE u.Trangthai = 1 AND (LOWER(u.Tendangnhap) = ? OR LOWER(u.Email) = ? OR u.Sdt = ? OR t.Mathe = ?)
            LIMIT 1;
        """;
        String norm = identifier.trim().toLowerCase();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, norm);
            ps.setString(2, norm);
            ps.setString(3, identifier.trim());
            ps.setString(4, identifier.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String uid = rs.getString("ID");
                    String name = rs.getString("Hoten");
                    String cardNum = rs.getString("Mathe");
                    String status = rs.getString("Trangthai");
                    Date expiry = rs.getDate("Ngayhethan");
                    boolean active = "1".equals(status) || "HOAT_DONG".equalsIgnoreCase(status);
                    boolean expired = (expiry != null && expiry.toLocalDate().isBefore(LocalDate.now()));
                    return Optional.of(new UserCardInfo(uid, name, cardNum, active, expired));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tra cứu thẻ thư viện: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<BookCopyInfo> findCopyByBarcode(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) return Optional.empty();
        String term = barcode.trim();
        String normalizedBookId = term;
        // Chuẩn hóa mã sách nếu người dùng gõ thiếu số 0 (ví dụ: MS1 -> MS0001, MS07 -> MS0007, MS007 -> MS0007)
        if (term.toUpperCase().matches("^MS\\d{1,3}$")) {
            int num = Integer.parseInt(term.substring(2));
            normalizedBookId = String.format("MS%04d", num);
        } else if (term.toUpperCase().matches("^MS\\d{1,3}-C\\d{2}$")) {
            String[] parts = term.split("-");
            int num = Integer.parseInt(parts[0].substring(2));
            normalizedBookId = String.format("MS%04d-%s", num, parts[1].toUpperCase());
        }

        String sql = """
            SELECT c.Macuonsach, s.Masach, s.Tensach, s.Giabia, c.Trangthaisach,
                   CONCAT(COALESCE(kv.Tenkhuvuc, ''), ' - ', COALESCE(ke.Tenkesach, '')) as Vitri
            FROM cuonsach c
            JOIN sach s ON c.Masach = s.Masach
            LEFT JOIN kesach ke ON c.Makesach = ke.Makesach
            LEFT JOIN khuvuc kv ON ke.Makhuvuc = kv.Makhuvuc
            WHERE (c.Macuonsach = ? OR c.Macuonsach = ? OR c.Isbn = ? OR c.Masach = ? OR c.Masach = ? OR s.Isbn = ?) AND s.IsDeleted = FALSE
            ORDER BY CASE WHEN c.Trangthaisach = 'KHA_DUNG' THEN 0 ELSE 1 END, c.Macuonsach ASC
            LIMIT 1;
        """;
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, term);
            ps.setString(2, normalizedBookId);
            ps.setString(3, term);
            ps.setString(4, term);
            ps.setString(5, normalizedBookId);
            ps.setString(6, term);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new BookCopyInfo(
                        rs.getString("Macuonsach"),
                        rs.getString("Masach"),
                        rs.getString("Tensach"),
                        rs.getBigDecimal("Giabia"),
                        rs.getString("Trangthaisach"),
                        rs.getString("Vitri")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tra cứu cuốn sách: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<BookCopyInfo> findAvailableCopies(String bookId) {
        List<BookCopyInfo> list = new ArrayList<>();
        String sql = """
            SELECT c.Macuonsach, s.Masach, s.Tensach, s.Giabia, c.Trangthaisach,
                   CONCAT(COALESCE(kv.Tenkhuvuc, ''), ' - ', COALESCE(ke.Tenkesach, '')) as Vitri
            FROM cuonsach c
            JOIN sach s ON c.Masach = s.Masach
            LEFT JOIN kesach ke ON c.Makesach = ke.Makesach
            LEFT JOIN khuvuc kv ON ke.Makhuvuc = kv.Makhuvuc
            WHERE c.Masach = ? AND c.Trangthaisach = 'KHA_DUNG' AND s.IsDeleted = FALSE
            ORDER BY c.Macuonsach ASC;
        """;
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new BookCopyInfo(
                        rs.getString("Macuonsach"),
                        rs.getString("Masach"),
                        rs.getString("Tensach"),
                        rs.getBigDecimal("Giabia"),
                        rs.getString("Trangthaisach"),
                        rs.getString("Vitri")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy danh sách cuốn sách khả dụng: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int countActiveHomeLoans(String userId) {
        String sql = """
            SELECT COUNT(ct.Machitiet)
            FROM phieumuon p
            JOIN chitietphieumuon ct ON p.Maphieumuon = ct.Maphieumuon
            WHERE p.ID = ? AND p.Hinhthuc = 'VE_NHA' AND p.Trangthai IN (0, 1, 3, 4) AND ct.Ngaytra IS NULL
        """;
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đếm số sách mang về đang mượn: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public boolean hasUnpaidFines(String userId) {
        String sql = "SELECT 1 FROM phieuphat WHERE ID = ? AND Trangthai = 0 LIMIT 1";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public void createBorrowTicket(String ticketId, String userId, String cardId, LoanType loanType,
                                  LocalDateTime borrowDate, LocalDateTime dueDate, List<String> copyIds, String notes) {
        List<String> sortedCopyIds = new ArrayList<>(copyIds);
        Collections.sort(sortedCopyIds); // Khóa theo thứ tự id tăng dần tránh deadlock (R-05)

        String insertTicketSql = """
            INSERT INTO phieumuon (Maphieumuon, ID, Mathe, Hinhthuc, Ngaymuon, Hantranhan, Trangthai, Ghichuphieu)
            VALUES (?, ?, ?, ?, ?, ?, 1, ?)
        """;
        String insertDetailSql = """
            INSERT INTO chitietphieumuon (Machitiet, Maphieumuon, Macuonsach, Tinhtrangkhimuon)
            VALUES (?, ?, ?, 'Mới 100%')
        """;
        String updateCopySql = "UPDATE cuonsach SET Trangthaisach = 'DANG_MUON' WHERE Macuonsach = ?";

        try (Connection conn = connectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Khóa bi quan SELECT ... FOR UPDATE (R-05)
                for (String cid : sortedCopyIds) {
                    try (PreparedStatement lockPs = conn.prepareStatement("SELECT Trangthaisach FROM cuonsach WHERE Macuonsach = ? FOR UPDATE")) {
                        lockPs.setString(1, cid);
                        try (ResultSet rs = lockPs.executeQuery()) {
                            if (!rs.next() || !"KHA_DUNG".equalsIgnoreCase(rs.getString("Trangthaisach"))) {
                                throw new IllegalStateException("Cuốn sách " + cid + " không còn ở trạng thái khả dụng để mượn!");
                            }
                        }
                    }
                }

                try (PreparedStatement psTicket = conn.prepareStatement(insertTicketSql)) {
                    psTicket.setString(1, ticketId);
                    psTicket.setString(2, userId);
                    psTicket.setString(3, cardId);
                    psTicket.setString(4, loanType.name());
                    psTicket.setDate(5, Date.valueOf(borrowDate.toLocalDate()));
                    psTicket.setTimestamp(6, Timestamp.valueOf(dueDate));
                    psTicket.setString(7, notes != null ? notes : "");
                    psTicket.executeUpdate();
                }

                int seq = 1;
                for (String cid : sortedCopyIds) {
                    String detailId = ticketId + "-D" + seq++;
                    try (PreparedStatement psDetail = conn.prepareStatement(insertDetailSql)) {
                        psDetail.setString(1, detailId);
                        psDetail.setString(2, ticketId);
                        psDetail.setString(3, cid);
                        psDetail.executeUpdate();
                    }

                    try (PreparedStatement psCopy = conn.prepareStatement(updateCopySql)) {
                        psCopy.setString(1, cid);
                        psCopy.executeUpdate();
                    }
                }

                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo phiếu mượn: " + e.getMessage(), e);
        }
    }

    @Override
    public List<BorrowTicketDto> findTickets(String keyword, Integer statusFilter) {
        StringBuilder sql = new StringBuilder("""
            SELECT p.Maphieumuon, p.ID, u.Hoten, p.Mathe, p.Hinhthuc, p.Ngaymuon,
                   p.Hantranhan, p.Ngaytra, p.Trangthai, p.Ghichuphieu,
                   GROUP_CONCAT(s.Tensach SEPARATOR '; ') AS BookTitles,
                   GROUP_CONCAT(ct.Macuonsach SEPARATOR '; ') AS CopyBarcodes
            FROM phieumuon p
            JOIN nguoidung u ON p.ID = u.ID
            LEFT JOIN chitietphieumuon ct ON p.Maphieumuon = ct.Maphieumuon
            LEFT JOIN cuonsach c ON ct.Macuonsach = c.Macuonsach
            LEFT JOIN sach s ON c.Masach = s.Masach
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(p.Maphieumuon) LIKE ? OR LOWER(u.Hoten) LIKE ? OR LOWER(p.Mathe) LIKE ?) ");
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            params.add(kw); params.add(kw); params.add(kw);
        }
        if (statusFilter != null) {
            sql.append(" AND p.Trangthai = ? ");
            params.add(statusFilter);
        }
        sql.append(" GROUP BY p.Maphieumuon, p.ID, u.Hoten, p.Mathe, p.Hinhthuc, p.Ngaymuon, p.Hantranhan, p.Ngaytra, p.Trangthai, p.Ghichuphieu ");
        sql.append(" ORDER BY p.Ngaymuon DESC, p.Maphieumuon DESC LIMIT 100");

        return executeTicketQuery(sql.toString(), params);
    }

    @Override
    public List<BorrowTicketDto> findTicketsByReader(String userId) {
        String sql = """
            SELECT p.Maphieumuon, p.ID, u.Hoten, p.Mathe, p.Hinhthuc, p.Ngaymuon,
                   p.Hantranhan, p.Ngaytra, p.Trangthai, p.Ghichuphieu,
                   GROUP_CONCAT(s.Tensach SEPARATOR '; ') AS BookTitles,
                   GROUP_CONCAT(ct.Macuonsach SEPARATOR '; ') AS CopyBarcodes
            FROM phieumuon p
            JOIN nguoidung u ON p.ID = u.ID
            LEFT JOIN chitietphieumuon ct ON p.Maphieumuon = ct.Maphieumuon
            LEFT JOIN cuonsach c ON ct.Macuonsach = c.Macuonsach
            LEFT JOIN sach s ON c.Masach = s.Masach
            WHERE p.ID = ?
            GROUP BY p.Maphieumuon, p.ID, u.Hoten, p.Mathe, p.Hinhthuc, p.Ngaymuon, p.Hantranhan, p.Ngaytra, p.Trangthai, p.Ghichuphieu
            ORDER BY p.Ngaymuon DESC
        """;
        return executeTicketQuery(sql, List.of(userId));
    }

    @Override
    public Optional<BorrowDetailInfo> findActiveLoanByCopy(String copyBarcode) {
        if (copyBarcode == null || copyBarcode.trim().isEmpty()) return Optional.empty();
        String term = copyBarcode.trim();
        String normalizedId = term;
        if (term.toUpperCase().matches("^MS\\d{1,3}$")) {
            int num = Integer.parseInt(term.substring(2));
            normalizedId = String.format("MS%04d", num);
        } else if (term.toUpperCase().matches("^MS\\d{1,3}-C\\d{2}$")) {
            String[] parts = term.split("-");
            int num = Integer.parseInt(parts[0].substring(2));
            normalizedId = String.format("MS%04d-%s", num, parts[1].toUpperCase());
        }

        String sql = """
            SELECT ct.Machitiet, p.Maphieumuon, ct.Macuonsach, s.Tensach, p.ID,
                   COALESCE(p.Hantranhan, p.Ngaytra) as DueDateVal, s.Giabia
            FROM chitietphieumuon ct
            JOIN phieumuon p ON ct.Maphieumuon = p.Maphieumuon
            JOIN cuonsach c ON ct.Macuonsach = c.Macuonsach
            JOIN sach s ON c.Masach = s.Masach
            WHERE (ct.Macuonsach = ? OR ct.Macuonsach = ? OR c.Isbn = ? OR c.Masach = ? OR c.Masach = ? OR s.Isbn = ?)
              AND ct.Ngaytra IS NULL AND p.Trangthai IN (1, 3, 4)
            LIMIT 1
        """;
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, term);
            ps.setString(2, normalizedId);
            ps.setString(3, term);
            ps.setString(4, term);
            ps.setString(5, normalizedId);
            ps.setString(6, term);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp dueTs = rs.getTimestamp("DueDateVal");
                    LocalDateTime dueDate = dueTs != null ? dueTs.toLocalDateTime() : null;
                    return Optional.of(new BorrowDetailInfo(
                        rs.getString("Machitiet"),
                        rs.getString("Maphieumuon"),
                        rs.getString("Macuonsach"),
                        rs.getString("Tensach"),
                        rs.getString("ID"),
                        dueDate,
                        rs.getBigDecimal("Giabia")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tra cứu cuốn sách đang mượn: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public void returnCopy(String detailId, String ticketId, String copyId, LocalDateTime returnDate, String condition, BigDecimal fine) {
        String updateDetailSql = """
            UPDATE chitietphieumuon
            SET Ngaytra = ?, Tinhtrangkhitra = ?, Tienphat = ?
            WHERE Machitiet = ?
        """;
        String updateCopySql = "UPDATE cuonsach SET Trangthaisach = 'KHA_DUNG' WHERE Macuonsach = ?";

        try (Connection conn = connectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(updateDetailSql)) {
                    ps.setTimestamp(1, Timestamp.valueOf(returnDate));
                    ps.setString(2, condition != null ? condition : "Bình thường");
                    ps.setBigDecimal(3, fine != null ? fine : BigDecimal.ZERO);
                    ps.setString(4, detailId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(updateCopySql)) {
                    ps.setString(1, copyId);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật trả sách: " + e.getMessage(), e);
        }
    }

    @Override
    public void closeTicketIfAllReturned(String ticketId) {
        String checkSql = "SELECT COUNT(*) FROM chitietphieumuon WHERE Maphieumuon = ? AND Ngaytra IS NULL";
        String closeSql = "UPDATE phieumuon SET Trangthai = 2, Ngaytra = ? WHERE Maphieumuon = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
            psCheck.setString(1, ticketId);
            try (ResultSet rs = psCheck.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    try (PreparedStatement psClose = conn.prepareStatement(closeSql)) {
                        psClose.setDate(1, Date.valueOf(LocalDate.now()));
                        psClose.setString(2, ticketId);
                        psClose.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đóng phiếu mượn: " + e.getMessage(), e);
        }
    }

    @Override
    public void createFine(String fineId, String userId, String detailId, String reason, BigDecimal amount, String notes) {
        String sql = """
            INSERT INTO phieuphat (Maphieuphat, ID, Machitiet, Lydophat, Sotienphat, Ngayphat, Trangthai, Ghichuphat)
            VALUES (?, ?, ?, ?, ?, NOW(), 0, ?)
        """;
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fineId);
            ps.setString(2, userId);
            ps.setString(3, detailId);
            ps.setString(4, reason);
            ps.setBigDecimal(5, amount);
            ps.setString(6, notes != null ? notes : "");
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tạo phiếu phạt: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateTicketStatus(String ticketId, int status) {
        String sql = "UPDATE phieumuon SET Trangthai = ? WHERE Maphieumuon = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, status);
            ps.setString(2, ticketId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật trạng thái phiếu: " + e.getMessage(), e);
        }
    }

    @Override
    public void markCopyLost(String copyId) {
        String sql = "UPDATE cuonsach SET Trangthaisach = 'HONG_MAT' WHERE Macuonsach = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, copyId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật cuốn sách hỏng mất: " + e.getMessage(), e);
        }
    }

    @Override
    public void payFineAndCloseLoss(String fineId, String paymentMethod) {
        String updateFineSql = "UPDATE phieuphat SET Trangthai = 1 WHERE Maphieuphat = ?";
        String findTicketSql = """
            SELECT p.Maphieumuon FROM phieuphat f
            JOIN chitietphieumuon ct ON f.Machitiet = ct.Machitiet
            JOIN phieumuon p ON ct.Maphieumuon = p.Maphieumuon
            WHERE f.Maphieuphat = ?
        """;
        String closeLossSql = "UPDATE phieumuon SET Trangthai = 5, Ngaytra = ? WHERE Maphieumuon = ?"; // 5 = DA_MAT

        try (Connection conn = connectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement psFine = conn.prepareStatement(updateFineSql)) {
                    psFine.setString(1, fineId);
                    psFine.executeUpdate();
                }

                String ticketId = null;
                try (PreparedStatement psFind = conn.prepareStatement(findTicketSql)) {
                    psFind.setString(1, fineId);
                    try (ResultSet rs = psFind.executeQuery()) {
                        if (rs.next()) ticketId = rs.getString(1);
                    }
                }

                if (ticketId != null) {
                    try (PreparedStatement psLoss = conn.prepareStatement(closeLossSql)) {
                        psLoss.setDate(1, Date.valueOf(LocalDate.now()));
                        psLoss.setString(2, ticketId);
                        psLoss.executeUpdate();
                    }
                }

                // Ghi nhận thanh toán vào bảng thanhtoan (Bảng 18)
                String payId = "PAY-" + System.currentTimeMillis();
                String insertPaymentSql = """
                    INSERT INTO thanhtoan (Mathanhtoan, Maphieuphat, Ngaythanhtoan, Sotien, Phuongthuc, Trangthai)
                    VALUES (?, ?, NOW(), COALESCE((SELECT Sotienphat FROM phieuphat WHERE Maphieuphat = ?), 0), ?, 1)
                """;
                try (PreparedStatement psPay = conn.prepareStatement(insertPaymentSql)) {
                    psPay.setString(1, payId);
                    psPay.setString(2, fineId);
                    psPay.setString(3, fineId);
                    psPay.setString(4, (paymentMethod != null && !paymentMethod.isBlank()) ? paymentMethod : "Tiền mặt");
                    psPay.executeUpdate();
                }

                conn.commit();
                com.ximilib.infrastructure.logging.AuditLogger.log("CIRCULATION", "PAY_FINE", "Hoàn tất thanh toán phạt " + fineId + " bằng " + paymentMethod);
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thanh toán phạt và đóng bồi thường: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isTicketRenewed(String ticketId) {
        String sql = "SELECT 1 FROM giahanmuon WHERE Maphieumuon = ? LIMIT 1";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ticketId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public int countReservations(String bookId) {
        String sql = "SELECT COUNT(*) FROM datcho WHERE Masach = ? AND Trangthai = 1";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            return 0;
        }
        return 0;
    }

    @Override
    public void extendDueDate(String ticketId, LocalDateTime newDueDate, String reason) {
        String getOldDueSql = "SELECT Hantranhan FROM phieumuon WHERE Maphieumuon = ?";
        String insertRenewSql = """
            INSERT INTO giahanmuon (Magiahan, Maphieumuon, Ngaygiahan, Hantracu, Hantramoi, Lydogiahan, Trangthai)
            VALUES (?, ?, NOW(), ?, ?, ?, 1)
        """;
        String updateDueSql = "UPDATE phieumuon SET Hantranhan = ? WHERE Maphieumuon = ?";

        try (Connection conn = connectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Timestamp oldDue = null;
                try (PreparedStatement psGet = conn.prepareStatement(getOldDueSql)) {
                    psGet.setString(1, ticketId);
                    try (ResultSet rs = psGet.executeQuery()) {
                        if (rs.next()) oldDue = rs.getTimestamp(1);
                    }
                }

                String renewId = "GH-" + System.currentTimeMillis() % 1000000;
                try (PreparedStatement psInsert = conn.prepareStatement(insertRenewSql)) {
                    psInsert.setString(1, renewId);
                    psInsert.setString(2, ticketId);
                    psInsert.setTimestamp(3, oldDue != null ? oldDue : Timestamp.valueOf(LocalDateTime.now()));
                    psInsert.setTimestamp(4, Timestamp.valueOf(newDueDate));
                    psInsert.setString(5, reason != null ? reason : "Gia hạn trực tuyến");
                    psInsert.executeUpdate();
                }

                try (PreparedStatement psUpdate = conn.prepareStatement(updateDueSql)) {
                    psUpdate.setTimestamp(1, Timestamp.valueOf(newDueDate));
                    psUpdate.setString(2, ticketId);
                    psUpdate.executeUpdate();
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lưu thông tin gia hạn mượn: " + e.getMessage(), e);
        }
    }

    @Override
    public void createReservation(String resId, String userId, String bookId) {
        String sql = """
            INSERT INTO datcho (Madatcho, ID, Masach, Ngaydat, Vitri, Ngayhethan, Trangthai)
            VALUES (?, ?, ?, NOW(), 1, DATE_ADD(NOW(), INTERVAL 24 HOUR), 1)
        """;
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, resId);
            ps.setString(2, userId);
            ps.setString(3, bookId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi ghi nhận đặt giữ sách: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateNextTicketId() {
        return "PM-" + System.currentTimeMillis() % 10000000;
    }

    @Override
    public String generateNextFineId() {
        return "PP-" + System.currentTimeMillis() % 10000000;
    }

    private List<BorrowTicketDto> executeTicketQuery(String sql, List<Object> params) {
        List<BorrowTicketDto> list = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tid = rs.getString("Maphieumuon");
                    String uid = rs.getString("ID");
                    String name = rs.getString("Hoten");
                    String cardNum = rs.getString("Mathe");
                    String hinhthucStr = rs.getString("Hinhthuc");
                    LoanType lt = "TAI_CHO".equalsIgnoreCase(hinhthucStr) ? LoanType.TAI_CHO : LoanType.VE_NHA;
                    Date bDate = rs.getDate("Ngaymuon");
                    LocalDateTime borrowDate = bDate != null ? bDate.toLocalDate().atStartOfDay() : null;
                    Timestamp dueTs = rs.getTimestamp("Hantranhan");
                    LocalDateTime dueDate = dueTs != null ? dueTs.toLocalDateTime() : null;
                    Date rDate = rs.getDate("Ngaytra");
                    LocalDateTime returnDate = rDate != null ? rDate.toLocalDate().atStartOfDay() : null;
                    int status = rs.getInt("Trangthai");
                    String notes = rs.getString("Ghichuphieu");
                    String titles = rs.getString("BookTitles");
                    String barcodes = rs.getString("CopyBarcodes");

                    list.add(new BorrowTicketDto(
                        tid, uid, name, cardNum, lt, borrowDate, dueDate, returnDate, status,
                        titles != null ? titles : "Nhiều cuốn", barcodes != null ? barcodes : "", notes
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn phiếu mượn: " + e.getMessage(), e);
        }
        return list;
    }
}
