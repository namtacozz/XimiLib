package com.ximilib.infrastructure.persistence.impl;

import com.ximilib.application.port.FeedbackPort;
import com.ximilib.infrastructure.persistence.connection.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FeedbackRepositoryImpl implements FeedbackPort {

    private final DatabaseConnectionManager connectionManager;

    public FeedbackRepositoryImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void submitFeedback(String userId, String title, String content) {
        String sql = "INSERT INTO gopy (id_nguoidung, tieude, noidung, ngaytao, trangthai) VALUES (?, ?, ?, NOW(), 'CHO_TIEP_NHAN')";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, title);
            ps.setString(3, content);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lưu góp ý: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Object[]> getAllFeedbacks() {
        String sql = """
            SELECT g.id, COALESCE(u.Hoten, 'Ẩn danh'), g.tieude, g.noidung, g.ngaytao,
                   CASE g.trangthai WHEN 'CHO_TIEP_NHAN' THEN 'Chờ tiếp nhận' ELSE 'Đã xử lý' END
            FROM gopy g
            LEFT JOIN nguoidung u ON g.id_nguoidung = u.ID
            ORDER BY g.ngaytao DESC
        """;
        List<Object[]> list = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt(1), rs.getString(2), rs.getString(3),
                    rs.getString(4), rs.getString(5), rs.getString(6)
                });
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn góp ý: " + e.getMessage());
        }
        return list;
    }

    @Override
    public void markAsProcessed(int id) {
        String sql = "UPDATE gopy SET trangthai = 'DA_XU_LY' WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật xử lý góp ý: " + e.getMessage(), e);
        }
    }
}
