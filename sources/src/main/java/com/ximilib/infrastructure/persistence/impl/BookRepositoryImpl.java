package com.ximilib.infrastructure.persistence.impl;

import com.ximilib.application.dto.BookSearchCriteria;
import com.ximilib.application.dto.BookSummaryDto;
import com.ximilib.application.dto.PagedResult;
import com.ximilib.application.port.BookQueryPort;
import com.ximilib.domain.repository.BookRepository;
import com.ximilib.infrastructure.persistence.connection.DatabaseConnectionManager;
import java.awt.Color;
import java.sql.*;
import java.util.*;

/**
 * BookRepositoryImpl: Triển khai truy vấn dữ liệu thực tế từ MySQL qua JDBC PreparedStatement.
 * - Tuân thủ R-01, R-03, R-04, R-07.
 * - 100% PreparedStatement có tham số `?`, không cộng chuỗi SQL.
 */
public class BookRepositoryImpl implements BookRepository, BookQueryPort {

    private static final Color[] PALETTE = {
        new Color(79, 70, 229),
        new Color(15, 23, 42),
        new Color(220, 38, 38),
        new Color(245, 158, 11),
        new Color(30, 58, 138),
        new Color(16, 185, 129),
        new Color(5, 150, 105),
        new Color(126, 34, 206),
        new Color(234, 88, 12),
        new Color(14, 116, 144),
        new Color(2, 132, 199),
        new Color(217, 119, 6)
    };

    private final DatabaseConnectionManager connectionManager;

    public BookRepositoryImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = (connectionManager != null) ? connectionManager : new DatabaseConnectionManager();
    }

    @Override
    public PagedResult<BookSummaryDto> searchBooks(BookSearchCriteria criteria, int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;

        String keyword = (criteria != null && criteria.getKeyword() != null) ? criteria.getKeyword().trim() : "";
        Integer categoryId = (criteria != null) ? criteria.getCategoryId() : null;
        BookSearchCriteria.AvailabilityFilter avail = (criteria != null && criteria.getAvailability() != null)
                ? criteria.getAvailability() : BookSearchCriteria.AvailabilityFilter.ALL;

        StringBuilder whereClause = new StringBuilder(" WHERE s.IsDeleted = FALSE ");
        List<Object> params = new ArrayList<>();

        if (!keyword.isEmpty()) {
            whereClause.append(" AND (LOWER(s.Tensach) LIKE ? OR LOWER(s.Masach) LIKE ? OR LOWER(tg.Tentacgia) LIKE ? OR s.Isbn LIKE ?) ");
            String kwParam = "%" + keyword.toLowerCase() + "%";
            params.add(kwParam);
            params.add(kwParam);
            params.add(kwParam);
            params.add(kwParam);
        }

        if (categoryId != null && categoryId > 0) {
            whereClause.append(" AND s.Matheloai = ? ");
            params.add(categoryId);
        }

        // Subquery counting active copies
        String copiesSubquery = "COALESCE((SELECT COUNT(*) FROM cuonsach cs WHERE cs.Masach = s.Masach AND cs.Trangthaisach = 'KHA_DUNG'), 0)";
        if (avail == BookSearchCriteria.AvailabilityFilter.AVAILABLE_ONLY) {
            whereClause.append(" AND ").append(copiesSubquery).append(" > 0 ");
        } else if (avail == BookSearchCriteria.AvailabilityFilter.OUT_OF_STOCK) {
            whereClause.append(" AND ").append(copiesSubquery).append(" = 0 ");
        }

        // 1. Đếm tổng số bản ghi khớp
        String countSql = "SELECT COUNT(DISTINCT s.Masach) "
                        + "FROM sach s "
                        + "LEFT JOIN theloai t ON s.Matheloai = t.Matheloai "
                        + "LEFT JOIN nhaxuatban nxb ON s.Manxb = nxb.manxb "
                        + "LEFT JOIN sach_tacgia st ON s.Masach = st.Masach "
                        + "LEFT JOIN tacgia tg ON st.Matacgia = tg.Matacgia "
                        + whereClause;

        int totalItems = 0;
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(countSql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalItems = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn đếm sách từ MySQL: " + e.getMessage(), e);
        }

        // 2. Truy vấn dữ liệu phân trang
        String dataSql = "SELECT s.Masach, s.Tensach, s.Matheloai, MAX(t.Tentheloai) AS Tentheloai, s.Manxb, MAX(nxb.tennxb) AS tennxb, "
                       + "       GROUP_CONCAT(tg.Tentacgia SEPARATOR ', ') AS Tentacgia, s.Isbn, s.Namxb, s.Giabia, s.Hinhanh, s.Motasach, s.IsDeleted, "
                       + "       " + copiesSubquery + " AS available_copies, "
                       + "       COALESCE((SELECT COUNT(*) FROM cuonsach cs WHERE cs.Masach = s.Masach), 0) AS total_copies, "
                       + "       COALESCE((SELECT COUNT(*) FROM cuonsach cs WHERE cs.Masach = s.Masach AND cs.Trangthaisach = 'DANG_MUON'), 0) AS active_loans "
                       + "FROM sach s "
                       + "LEFT JOIN theloai t ON s.Matheloai = t.Matheloai "
                       + "LEFT JOIN nhaxuatban nxb ON s.Manxb = nxb.manxb "
                       + "LEFT JOIN sach_tacgia st ON s.Masach = st.Masach "
                       + "LEFT JOIN tacgia tg ON st.Matacgia = tg.Matacgia "
                       + whereClause
                       + "GROUP BY s.Masach "
                       + "ORDER BY CAST(SUBSTRING(s.Masach, 3) AS UNSIGNED) DESC "
                       + "LIMIT ? OFFSET ?";

        List<BookSummaryDto> items = new ArrayList<>();
        int offset = (page - 1) * pageSize;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(dataSql)) {
            int pIdx = 1;
            for (Object param : params) {
                ps.setObject(pIdx++, param);
            }
            ps.setInt(pIdx++, pageSize);
            ps.setInt(pIdx, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String bookId = rs.getString("Masach");
                    String title = rs.getString("Tensach");
                    String author = rs.getString("Tentacgia");
                    if (author == null) author = "Đang cập nhật";
                    int catId = rs.getInt("Matheloai");
                    String catName = rs.getString("Tentheloai");
                    if (catName == null) catName = "Chưa phân loại";
                    String pubName = rs.getString("tennxb");
                    if (pubName == null) pubName = "NXB Khác";
                    int availableCopies = rs.getInt("available_copies");
                    int totalCopies = rs.getInt("total_copies");
                    int activeLoans = rs.getInt("active_loans");
                    boolean isDeleted = rs.getBoolean("IsDeleted");
                    String hinhanh = rs.getString("Hinhanh");

                    Color color = PALETTE[Math.abs(bookId.hashCode()) % PALETTE.length];

                    BookSummaryDto dto = new BookSummaryDto(
                        bookId, title, author, catId, catName, pubName,
                        availableCopies, totalCopies, color, isDeleted, activeLoans
                    );
                    dto.setCoverImage(hinhanh);
                    dto.setDescription(rs.getString("Motasach"));
                    dto.setIsbn(rs.getString("Isbn"));
                    items.add(dto);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn danh sách sách từ MySQL: " + e.getMessage(), e);
        }

        return new PagedResult<>(items, totalItems, page, pageSize);
    }

    @Override
    public Map<Integer, String> getCategories() {
        Map<Integer, String> result = new LinkedHashMap<>();
        String sql = "SELECT Matheloai, Tentheloai FROM theloai ORDER BY Matheloai ASC";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getInt("Matheloai"), rs.getString("Tentheloai"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn danh mục thể loại từ MySQL: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public int countActiveLoans(String bookId) {
        String sql = "SELECT COUNT(*) FROM cuonsach WHERE Masach = ? AND Trangthaisach = 'DANG_MUON'";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kiểm tra số sách đang mượn từ MySQL: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public void softDelete(String bookId) {
        String sql = "UPDATE sach SET IsDeleted = TRUE WHERE Masach = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa mềm sách từ MySQL: " + e.getMessage(), e);
        }
    }

    @Override
    public int softDeleteBatch(List<String> bookIds) {
        return softDeleteBatch(null, bookIds);
    }

    @Override
    public int softDeleteBatch(com.ximilib.domain.repository.TransactionContext tx, List<String> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) return 0;
        if (tx == null) {
            return new com.ximilib.infrastructure.persistence.connection.JdbcTransactionCoordinator(connectionManager)
                    .execute(ctx -> softDeleteBatch(ctx, bookIds));
        }
        Connection conn = ((com.ximilib.infrastructure.persistence.connection.JdbcTransactionContext) tx).getConnection();
        String sql = "UPDATE sach SET IsDeleted = TRUE WHERE Masach = ?";
        int count = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String id : bookIds) {
                ps.setString(1, id);
                ps.addBatch();
            }
            int[] results = ps.executeBatch();
            for (int r : results) {
                if (r > 0 || r == Statement.SUCCESS_NO_INFO) count++;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi xóa mềm hàng loạt sách từ MySQL: " + ex.getMessage(), ex);
        }
        return count;
    }

    @Override
    public int countTotalBooks() {
        String sql = "SELECT COUNT(*) FROM sach WHERE IsDeleted = FALSE";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đếm tổng số sách từ MySQL: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public List<BookSummaryDto> getDeletedBooks() {
        String sql = """
            SELECT s.Masach, s.Tensach,
                   COALESCE(MAX(t.Tentheloai), 'Chưa phân loại') as Tentheloai,
                   COALESCE(MAX(nxb.tennxb), 'Chưa rõ') as Tennxb,
                   COALESCE(GROUP_CONCAT(tg.Tentacgia SEPARATOR ', '), 'Nhiều tác giả') as Tentacgia,
                   s.Hinhanh, s.Matheloai,
                   COUNT(c.Macuonsach) as TongSoCuon,
                   SUM(CASE WHEN c.Trangthaisach = 'KHA_DUNG' THEN 1 ELSE 0 END) as SoCuonKhaDung
            FROM sach s
            LEFT JOIN theloai t ON s.Matheloai = t.Matheloai
            LEFT JOIN nhaxuatban nxb ON s.Manxb = nxb.manxb
            LEFT JOIN sach_tacgia st ON s.Masach = st.Masach
            LEFT JOIN tacgia tg ON st.Matacgia = tg.Matacgia
            LEFT JOIN cuonsach c ON s.Masach = c.Masach
            WHERE s.IsDeleted = TRUE
            GROUP BY s.Masach, s.Tensach, s.Hinhanh, s.Matheloai
            ORDER BY s.Tensach ASC
        """;
        List<BookSummaryDto> list = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int colorIdx = 0;
            while (rs.next()) {
                String id = rs.getString("Masach");
                String title = rs.getString("Tensach");
                String author = rs.getString("Tentacgia");
                int catId = rs.getInt("Matheloai");
                String catName = rs.getString("Tentheloai");
                String pubName = rs.getString("Tennxb");
                int totalCopies = rs.getInt("TongSoCuon");
                int availCopies = rs.getInt("SoCuonKhaDung");
                String cover = rs.getString("Hinhanh");
                Color coverColor = PALETTE[colorIdx % PALETTE.length];
                colorIdx++;

                BookSummaryDto dto = new BookSummaryDto(
                    id, title, author, catId, catName, pubName,
                    availCopies, totalCopies, coverColor, true, 0
                );
                dto.setCoverImage(cover);
                list.add(dto);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy danh sách thùng rác từ MySQL: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void restoreBook(String bookId) {
        String sql = "UPDATE sach SET IsDeleted = FALSE WHERE Masach = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khôi phục sách từ thùng rác: " + e.getMessage(), e);
        }
    }

    @Override
    public void addCategory(String name, String desc) {
        String sql = "INSERT INTO theloai (Tentheloai, Mota) VALUES (?, ?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.setString(2, desc != null ? desc.trim() : "");
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm thể loại mới: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateCategory(int id, String name, String desc) {
        String sql = "UPDATE theloai SET Tentheloai = ?, Mota = ? WHERE Matheloai = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.setString(2, desc != null ? desc.trim() : "");
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật thể loại: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteCategory(int id) {
        String sql = "DELETE FROM theloai WHERE Matheloai = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa thể loại: " + e.getMessage(), e);
        }
    }

    @Override
    public void saveNewBook(String title, String author, String categoryName, String isbn, double price, int initialCopies) {
        saveNewBook(title, author, categoryName, "NXB Bách Khoa", "Kệ IT-01 (Khu A)", isbn, price, initialCopies);
    }

    @Override
    public void saveNewBook(String title, String author, String categoryName, String publisherName, String shelfLocation, String isbn, double price, int initialCopies) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên sách không được để trống!");
        }
        String cleanTitle = title.trim();
        String cleanAuthor = (author != null && !author.trim().isEmpty()) ? author.trim() : "Nhiều tác giả";
        String cleanPub = (publisherName != null && !publisherName.trim().isEmpty()) ? publisherName.trim() : "NXB Bách Khoa";
        String cleanShelf = (shelfLocation != null && !shelfLocation.trim().isEmpty()) ? shelfLocation.trim() : "Kệ IT-01 (Khu A)";
        String cleanIsbn = (isbn != null && !isbn.trim().isEmpty()) ? isbn.trim() : "978-604-00-0000-0";
        if (initialCopies <= 0) initialCopies = 3;

        try (Connection conn = connectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Tìm hoặc lấy thể loại phù hợp
                int catId = 1;
                try (PreparedStatement psCat = conn.prepareStatement("SELECT Matheloai FROM theloai WHERE LOWER(Tentheloai) = ? LIMIT 1")) {
                    psCat.setString(1, (categoryName != null ? categoryName.trim().toLowerCase() : ""));
                    try (ResultSet rs = psCat.executeQuery()) {
                        if (rs.next()) {
                            catId = rs.getInt(1);
                        } else {
                            try (PreparedStatement psFirst = conn.prepareStatement("SELECT Matheloai FROM theloai ORDER BY Matheloai ASC LIMIT 1")) {
                                try (ResultSet rsFirst = psFirst.executeQuery()) {
                                    if (rsFirst.next()) catId = rsFirst.getInt(1);
                                }
                            }
                        }
                    }
                }

                // 2. Tìm hoặc thêm tác giả mới
                String authorId = null;
                try (PreparedStatement psTg = conn.prepareStatement("SELECT Matacgia FROM tacgia WHERE LOWER(Tentacgia) = ? LIMIT 1")) {
                    psTg.setString(1, cleanAuthor.toLowerCase());
                    try (ResultSet rs = psTg.executeQuery()) {
                        if (rs.next()) {
                            authorId = rs.getString(1);
                        }
                    }
                }
                if (authorId == null) {
                    int nextTgNum = 1;
                    try (PreparedStatement psMaxTg = conn.prepareStatement("SELECT MAX(CAST(SUBSTRING(Matacgia, 3) AS UNSIGNED)) FROM tacgia WHERE Matacgia LIKE 'TG%'")) {
                        try (ResultSet rs = psMaxTg.executeQuery()) {
                            if (rs.next()) nextTgNum = rs.getInt(1) + 1;
                        }
                    }
                    authorId = String.format("TG%03d", nextTgNum);
                    try (PreparedStatement psInsTg = conn.prepareStatement("INSERT INTO tacgia (Matacgia, Tentacgia) VALUES (?, ?)")) {
                        psInsTg.setString(1, authorId);
                        psInsTg.setString(2, cleanAuthor);
                        psInsTg.executeUpdate();
                    }
                }

                // 3. Tìm hoặc thêm NXB
                String pubId = "NXB01";
                try (PreparedStatement psPub = conn.prepareStatement("SELECT manxb FROM nhaxuatban WHERE LOWER(tennxb) = ? OR LOWER(tennxb) LIKE ? LIMIT 1")) {
                    psPub.setString(1, cleanPub.toLowerCase());
                    psPub.setString(2, "%" + cleanPub.toLowerCase() + "%");
                    try (ResultSet rs = psPub.executeQuery()) {
                        if (rs.next()) {
                            pubId = rs.getString(1);
                        } else {
                            int nextNxb = 1;
                            try (PreparedStatement psMaxNxb = conn.prepareStatement("SELECT MAX(CAST(SUBSTRING(manxb, 4) AS UNSIGNED)) FROM nhaxuatban WHERE manxb LIKE 'NXB%'")) {
                                try (ResultSet rsMax = psMaxNxb.executeQuery()) {
                                    if (rsMax.next()) nextNxb = rsMax.getInt(1) + 1;
                                }
                            }
                            pubId = String.format("NXB%02d", nextNxb);
                            try (PreparedStatement psInsPub = conn.prepareStatement("INSERT INTO nhaxuatban (manxb, tennxb) VALUES (?, ?)")) {
                                psInsPub.setString(1, pubId);
                                psInsPub.setString(2, cleanPub);
                                psInsPub.executeUpdate();
                            }
                        }
                    }
                }

                // 4. Tìm hoặc xác định Kệ sách
                String shelfId = "KE-A1";
                try (PreparedStatement psKe = conn.prepareStatement("SELECT Makesach FROM kesach WHERE LOWER(Tenkesach) = ? OR LOWER(Tenkesach) LIKE ? OR LOWER(Makesach) = ? LIMIT 1")) {
                    psKe.setString(1, cleanShelf.toLowerCase());
                    psKe.setString(2, "%" + cleanShelf.toLowerCase() + "%");
                    psKe.setString(3, cleanShelf.toLowerCase());
                    try (ResultSet rs = psKe.executeQuery()) {
                        if (rs.next()) {
                            shelfId = rs.getString(1);
                        } else {
                            try (PreparedStatement psFirstKe = conn.prepareStatement("SELECT Makesach FROM kesach LIMIT 1")) {
                                try (ResultSet rsFirst = psFirstKe.executeQuery()) {
                                    if (rsFirst.next()) shelfId = rsFirst.getString(1);
                                }
                            }
                        }
                    }
                }

                // 5. Sinh mã sách tự động: MSxxxx
                int nextBookNum = 1;
                try (PreparedStatement psMaxBook = conn.prepareStatement("SELECT MAX(CAST(SUBSTRING(Masach, 3) AS UNSIGNED)) FROM sach WHERE Masach LIKE 'MS%'")) {
                    try (ResultSet rs = psMaxBook.executeQuery()) {
                        if (rs.next()) nextBookNum = rs.getInt(1) + 1;
                    }
                }
                String bookId = String.format("MS%04d", nextBookNum);

                // 6. Thêm sách vào bảng sach
                String insBookSql = "INSERT INTO sach (Masach, Matheloai, Manxb, Tensach, Isbn, Namxb, Giabia, Hinhanh, IsDeleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, FALSE)";
                try (PreparedStatement psBook = conn.prepareStatement(insBookSql)) {
                    psBook.setString(1, bookId);
                    psBook.setInt(2, catId);
                    psBook.setString(3, pubId);
                    psBook.setString(4, cleanTitle);
                    psBook.setString(5, cleanIsbn);
                    psBook.setInt(6, java.time.LocalDate.now().getYear());
                    psBook.setBigDecimal(7, java.math.BigDecimal.valueOf(price > 0 ? price : 150000.0));
                    psBook.setString(8, null);
                    psBook.executeUpdate();
                }

                // 7. Thêm liên kết sach_tacgia (dùng đúng cột Vaitrotacgia hoặc default)
                String insStSql = "INSERT INTO sach_tacgia (Masach, Matacgia, Vaitrotacgia) VALUES (?, ?, 'Tác giả chính')";
                try (PreparedStatement psSt = conn.prepareStatement(insStSql)) {
                    psSt.setString(1, bookId);
                    psSt.setString(2, authorId);
                    psSt.executeUpdate();
                }

                // 8. Tạo cuốn sách vật lý (cuonsach) ở trạng thái KHA_DUNG
                String insCopySql = "INSERT INTO cuonsach (Macuonsach, Masach, Makesach, Isbn, Ngaynhap, Tinhtrangsach, Trangthaisach) VALUES (?, ?, ?, ?, CURRENT_DATE(), 'Mới', 'KHA_DUNG')";
                try (PreparedStatement psCopy = conn.prepareStatement(insCopySql)) {
                    for (int i = 1; i <= initialCopies; i++) {
                        String copyBarcode = String.format("%s-C%02d", bookId, i);
                        psCopy.setString(1, copyBarcode);
                        psCopy.setString(2, bookId);
                        psCopy.setString(3, shelfId);
                        psCopy.setString(4, cleanIsbn);
                        psCopy.addBatch();
                    }
                    psCopy.executeBatch();
                }

                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw new RuntimeException("Lỗi lưu đầu sách vào CSDL: " + ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kết nối cơ sở dữ liệu: " + e.getMessage(), e);
        }
    }
}
