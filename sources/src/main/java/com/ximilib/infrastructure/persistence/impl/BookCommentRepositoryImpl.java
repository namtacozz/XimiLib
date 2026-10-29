package com.ximilib.infrastructure.persistence.impl;

import com.ximilib.application.port.BookCommentPort;
import com.ximilib.infrastructure.persistence.connection.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * BookCommentRepositoryImpl: Triển khai BookCommentPort bằng PreparedStatement kết nối MySQL.
 * Tuân thủ R-01, R-07.
 */
public class BookCommentRepositoryImpl implements BookCommentPort {

    private final DatabaseConnectionManager connectionManager;

    public BookCommentRepositoryImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public BookCommentData getBookComments(String bookId) {
        String sql = """
            SELECT b.noidung, b.diemsao, b.ngaytao, u.Hoten
            FROM binhluan b
            JOIN nguoidung u ON b.id_nguoidung = u.ID
            WHERE b.Masach = ? AND b.IsDeleted = FALSE
            ORDER BY b.ngaytao DESC
        """;
        List<BookCommentItem> comments = new ArrayList<>();
        int count = 0;
        int sumStars = 0;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn bình luận sách: " + e.getMessage());
        }

        double avgRating = count > 0 ? (double) sumStars / count : 5.0;
        return new BookCommentData(avgRating, count, comments);
    }

    @Override
    public void addComment(String bookId, String userId, String content, int stars) {
        String sql = "INSERT INTO binhluan (Masach, id_nguoidung, noidung, diemsao, ngaytao) VALUES (?, ?, ?, ?, NOW())";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookId);
            ps.setString(2, userId);
            ps.setString(3, content);
            ps.setInt(4, stars);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lưu bình luận sách: " + e.getMessage(), e);
        }
    }
}
