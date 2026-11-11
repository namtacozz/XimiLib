package com.ximilib.infrastructure.persistence.impl;

import com.ximilib.application.port.FeedbackPort;
import com.ximilib.infrastructure.persistence.connection.DatabaseConnectionManager;
import java.util.List;

public class FeedbackRepositoryImpl extends BaseJdbcRepository implements FeedbackPort {

    public FeedbackRepositoryImpl(DatabaseConnectionManager connectionManager) {
        super(connectionManager);
    }

    @Override
    public void submitFeedback(String userId, String title, String content) {
        executeUpdate("INSERT INTO gopy (id_nguoidung, tieude, noidung, ngaytao, trangthai) VALUES (?, ?, ?, NOW(), 'CHO_TIEP_NHAN')",
                userId, title, content);
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
        return queryObjectRows(sql, 6);
    }

    @Override
    public void markAsProcessed(int id) {
        executeUpdate("UPDATE gopy SET trangthai = 'DA_XU_LY' WHERE id = ?", id);
    }
}
