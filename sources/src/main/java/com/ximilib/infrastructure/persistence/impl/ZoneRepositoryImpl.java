package com.ximilib.infrastructure.persistence.impl;

import com.ximilib.domain.exception.BookException;
import com.ximilib.domain.model.Zone;
import com.ximilib.domain.repository.ZoneRepository;
import com.ximilib.infrastructure.persistence.connection.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ZoneRepositoryImpl implements ZoneRepository {

    private final DatabaseConnectionManager connectionManager;

    public ZoneRepositoryImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = Objects.requireNonNull(connectionManager, "connectionManager không được null");
    }

    @Override
    public Optional<Zone> findById(String zoneId) {
        String sql = "SELECT Makhuvuc, Tenkhuvuc, Motakhuvuc FROM khuvuc WHERE Makhuvuc = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, zoneId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Zone(
                            rs.getString("Makhuvuc"),
                            rs.getString("Tenkhuvuc"),
                            rs.getString("Motakhuvuc")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn khu vực theo mã: " + zoneId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Zone> findAll() {
        String sql = "SELECT Makhuvuc, Tenkhuvuc, Motakhuvuc FROM khuvuc ORDER BY Makhuvuc ASC";
        List<Zone> zones = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                zones.add(new Zone(
                        rs.getString("Makhuvuc"),
                        rs.getString("Tenkhuvuc"),
                        rs.getString("Motakhuvuc")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn danh sách phân khu: " + e.getMessage(), e);
        }
        return zones;
    }

    @Override
    public void save(Zone zone) {
        String sql = "INSERT INTO khuvuc (Makhuvuc, Tenkhuvuc, Motakhuvuc) VALUES (?, ?, ?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, zone.getZoneId());
            ps.setString(2, zone.getZoneName());
            ps.setString(3, zone.getDescription());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lưu phân khu mới: " + zone.getZoneId(), e);
        }
    }

    @Override
    public void update(Zone zone) {
        String sql = "UPDATE khuvuc SET Tenkhuvuc = ?, Motakhuvuc = ? WHERE Makhuvuc = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, zone.getZoneName());
            ps.setString(2, zone.getDescription());
            ps.setString(3, zone.getZoneId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật phân khu: " + zone.getZoneId(), e);
        }
    }

    @Override
    public void delete(String zoneId) {
        int shelves = countShelvesInZone(zoneId);
        if (shelves > 0) {
            throw new BookException("ZONE_NOT_EMPTY",
                    "Không thể xóa khu vực \"" + zoneId + "\": Hiện vẫn còn " + shelves + " kệ sách trực thuộc khu vực này.");
        }
        String sql = "DELETE FROM khuvuc WHERE Makhuvuc = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, zoneId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa phân khu: " + zoneId, e);
        }
    }

    @Override
    public int countShelvesInZone(String zoneId) {
        String sql = "SELECT COUNT(*) FROM kesach WHERE Makhuvuc = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, zoneId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đếm số lượng kệ trong khu vực: " + zoneId, e);
        }
        return 0;
    }
}
