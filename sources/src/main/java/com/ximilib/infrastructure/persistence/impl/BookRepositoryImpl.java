package com.ximilib.infrastructure.persistence.impl;

import com.ximilib.application.dto.BookSearchCriteria;
import com.ximilib.application.dto.BookSummaryDto;
import com.ximilib.application.dto.PagedResult;
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
public class BookRepositoryImpl implements BookRepository {

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
                       + "       GROUP_CONCAT(tg.Tentacgia SEPARATOR ', ') AS Tentacgia, s.Isbn, s.Namxb, s.Giabia, s.Hinhanh, s.IsDeleted, "
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
                       + "ORDER BY s.Masach ASC "
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
        if (bookIds == null || bookIds.isEmpty()) return 0;
        String sql = "UPDATE sach SET IsDeleted = TRUE WHERE Masach = ?";
        int count = 0;
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (String id : bookIds) {
                ps.setString(1, id);
                ps.addBatch();
            }
            int[] results = ps.executeBatch();
            conn.commit();
            for (int r : results) {
                if (r > 0 || r == Statement.SUCCESS_NO_INFO) count++;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa mềm hàng loạt sách từ MySQL: " + e.getMessage(), e);
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
}
