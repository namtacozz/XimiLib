package com.ximilib.infrastructure.persistence.impl;

import com.ximilib.application.port.BookCommentPort;
import com.ximilib.infrastructure.persistence.connection.DatabaseConnectionManager;
import com.ximilib.presentation.util.UserSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class BookCommentRepositoryImpl implements BookCommentPort {

    private final DatabaseConnectionManager connectionManager;
    private static final Map<String, List<BookCommentItem>> MEMORY_CACHE = new ConcurrentHashMap<>();
    private static volatile boolean tableVerified = false;

    public BookCommentRepositoryImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    private void ensureTableExists(Connection conn) {
        if (tableVerified) return;
        String ddl = """
            CREATE TABLE IF NOT EXISTS `binhluan` (
                `id` INT AUTO_INCREMENT PRIMARY KEY,
                `Masach` VARCHAR(50) NOT NULL,
                `id_nguoidung` VARCHAR(50) NOT NULL,
                `noidung` TEXT NOT NULL,
                `diemsao` INT NULL DEFAULT 5,
                `ngaytao` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                `IsDeleted` BOOLEAN NOT NULL DEFAULT FALSE,
                KEY `idx_bl_masach` (`Masach`),
                KEY `idx_bl_nguoidung` (`id_nguoidung`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
        """;
        try (Statement st = conn.createStatement()) {
            st.execute(ddl);
            tableVerified = true;
        } catch (SQLException ignored) {
        }
    }

    @Override
    public BookCommentData getBookComments(String bookId) {
        List<BookCommentItem> comments = new ArrayList<>();
        int count = 0;
        int sumStars = 0;

        String sql = """
            SELECT b.noidung, b.diemsao, b.ngaytao, u.Hoten
            FROM binhluan b
            JOIN nguoidung u ON b.id_nguoidung = u.ID
            WHERE b.Masach = ? AND b.IsDeleted = FALSE
            ORDER BY b.ngaytao DESC
        """;

        try (Connection conn = connectionManager.getConnection()) {
            ensureTableExists(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, bookId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        count++;
                        int stars = rs.getInt(2);
                        sumStars += stars;
                        comments.add(new BookCommentItem(
                                rs.getString(4),
                                rs.getString(1),
                                stars,
                                rs.getDate(3) != null ? rs.getDate(3).toString() : ""
                        ));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Truy vấn bình luận sách từ CSDL: " + e.getMessage());
        }

        List<BookCommentItem> cached = MEMORY_CACHE.get(bookId);
        if (cached != null) {
            for (BookCommentItem item : cached) {
                boolean duplicate = false;
                for (BookCommentItem c : comments) {
                    if (c.text().equals(item.text())) {
                        duplicate = true;
                        break;
                    }
                }
                if (!duplicate) {
                    comments.add(0, item);
                    count++;
                    sumStars += item.stars();
                }
            }
        }

        double avgRating = count > 0 ? (double) sumStars / count : 5.0;
        return new BookCommentData(avgRating, count, comments);
    }

    @Override
    public void addComment(String bookId, String userId, String content, int stars) {
        String validUserId = userId;
        if (validUserId == null || "GUEST".equalsIgnoreCase(validUserId)) {
            validUserId = "ND001";
        }

        String authorName = "Độc giả";
        if (UserSession.getCurrentUser() != null && !UserSession.getCurrentUser().isGuest()) {
            authorName = UserSession.getCurrentUser().getFullName();
        }

        BookCommentItem newItem = new BookCommentItem(authorName, content, stars, LocalDate.now().toString());
        MEMORY_CACHE.computeIfAbsent(bookId, k -> new CopyOnWriteArrayList<>()).add(0, newItem);

        try (Connection conn = connectionManager.getConnection()) {
            ensureTableExists(conn);

            String checkUserSql = "SELECT ID FROM nguoidung WHERE ID = ? LIMIT 1";
            try (PreparedStatement psCheck = conn.prepareStatement(checkUserSql)) {
                psCheck.setString(1, validUserId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (!rs.next()) {

                        try (Statement st = conn.createStatement();
                             ResultSet rsAny = st.executeQuery("SELECT ID FROM nguoidung LIMIT 1")) {
                            if (rsAny.next()) {
                                validUserId = rsAny.getString(1);
                            }
                        }
                    }
                }
            }

            String insertSql = "INSERT INTO binhluan (Masach, id_nguoidung, noidung, diemsao, ngaytao) VALUES (?, ?, ?, ?, NOW())";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, bookId);
                ps.setString(2, validUserId);
                ps.setString(3, content);
                ps.setInt(4, stars);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("Ghi bình luận sách vào CSDL thất bại (đã lưu cache bộ nhớ): " + e.getMessage());
        }
    }
}
