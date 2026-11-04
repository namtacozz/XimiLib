package com.ximilib.infrastructure.persistence.impl;

import com.ximilib.application.port.SystemSettingsPort;
import com.ximilib.infrastructure.logging.AuditLogger;
import com.ximilib.infrastructure.persistence.connection.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SystemSettingsRepositoryImpl implements SystemSettingsPort {

    private final DatabaseConnectionManager connectionManager;

    public SystemSettingsRepositoryImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public List<Object[]> getAllSettings() {
        String sql = "SELECT khoa, giatri, mota FROM cauhinh ORDER BY khoa ASC";
        List<Object[]> rows = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3)});
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn cấu hình hệ thống: " + e.getMessage());
        }
        return rows;
    }

    @Override
    public void updateSetting(String key, String value, String currentUserId) {
        String sql = "UPDATE cauhinh SET giatri = ? WHERE khoa = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value != null ? value.trim() : "");
            ps.setString(2, key);
            ps.executeUpdate();

            String user = (currentUserId != null && !currentUserId.isBlank()) ? currentUserId : "ADMIN";
            AuditLogger.log(user, "UPDATE_CONFIG", "Cập nhật tham số " + key + " = " + value);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật cấu hình hệ thống: " + e.getMessage(), e);
        }
    }
}
