package com.ximilib.infrastructure.persistence.impl;

import com.ximilib.application.dto.ShelfDto;
import com.ximilib.application.dto.ZoneDto;
import com.ximilib.application.port.StorageQueryPort;
import com.ximilib.domain.exception.CopyException;
import com.ximilib.domain.model.Shelf;
import com.ximilib.domain.repository.ShelfRepository;
import com.ximilib.infrastructure.persistence.connection.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * ShelfRepositoryImpl: Triển khai JDBC cho bảng kesach và StorageQueryPort (CQRS Read-Model).
 * 100% PreparedStatement theo quy tắc R-07.
 */
public class ShelfRepositoryImpl implements ShelfRepository, StorageQueryPort {

    private final DatabaseConnectionManager connectionManager;

    public ShelfRepositoryImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = Objects.requireNonNull(connectionManager, "connectionManager không được null");
    }

    @Override
    public Optional<Shelf> findById(String shelfId) {
        String sql = "SELECT Makesach, Makhuvuc, Tenkesach, Tang, Motake FROM kesach WHERE Makesach = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, shelfId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Shelf(
                            rs.getString("Makesach"),
                            rs.getString("Makhuvuc"),
                            rs.getString("Tenkesach"),
                            rs.getInt("Tang"),
                            rs.getString("Motake")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn kệ sách theo mã: " + shelfId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Shelf> findAll() {
        String sql = "SELECT Makesach, Makhuvuc, Tenkesach, Tang, Motake FROM kesach ORDER BY Makesach ASC";
        List<Shelf> shelves = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                shelves.add(new Shelf(
                        rs.getString("Makesach"),
                        rs.getString("Makhuvuc"),
                        rs.getString("Tenkesach"),
                        rs.getInt("Tang"),
                        rs.getString("Motake")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn danh sách kệ sách: " + e.getMessage(), e);
        }
        return shelves;
    }

    @Override
    public List<Shelf> findByZoneId(String zoneId) {
        String sql = "SELECT Makesach, Makhuvuc, Tenkesach, Tang, Motake FROM kesach WHERE Makhuvuc = ? ORDER BY Makesach ASC";
        List<Shelf> shelves = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, zoneId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    shelves.add(new Shelf(
                            rs.getString("Makesach"),
                            rs.getString("Makhuvuc"),
                            rs.getString("Tenkesach"),
                            rs.getInt("Tang"),
                            rs.getString("Motake")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn kệ sách theo khu vực: " + zoneId, e);
        }
        return shelves;
    }

    @Override
    public void save(Shelf shelf) {
        String sql = "INSERT INTO kesach (Makesach, Makhuvuc, Tenkesach, Tang, Motake) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, shelf.getShelfId());
            ps.setString(2, shelf.getZoneId());
            ps.setString(3, shelf.getShelfName());
            ps.setInt(4, shelf.getFloor());
            ps.setString(5, shelf.getDescription());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lưu kệ sách mới: " + shelf.getShelfId(), e);
        }
    }

    @Override
    public void update(Shelf shelf) {
        String sql = "UPDATE kesach SET Makhuvuc = ?, Tenkesach = ?, Tang = ?, Motake = ? WHERE Makesach = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, shelf.getZoneId());
            ps.setString(2, shelf.getShelfName());
            ps.setInt(3, shelf.getFloor());
            ps.setString(4, shelf.getDescription());
            ps.setString(5, shelf.getShelfId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật kệ sách: " + shelf.getShelfId(), e);
        }
    }

    @Override
    public void delete(String shelfId) {
        int copies = countCopiesOnShelf(shelfId);
        if (copies > 0) {
            throw new CopyException("SHELF_NOT_EMPTY",
                    "Không thể xóa kệ sách \"" + shelfId + "\": Hiện vẫn còn " + copies + " cuốn sách đang được xếp trên kệ.");
        }
        String sql = "DELETE FROM kesach WHERE Makesach = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, shelfId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa kệ sách: " + shelfId, e);
        }
    }

    @Override
    public int countCopiesOnShelf(String shelfId) {
        String sql = "SELECT COUNT(*) FROM cuonsach WHERE Makesach = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, shelfId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đếm số lượng cuốn sách trên kệ: " + shelfId, e);
        }
        return 0;
    }

    @Override
    public List<ZoneDto> getAllZoneSummaries() {
        String sql = "SELECT k.Makhuvuc, k.Tenkhuvuc, k.Motakhuvuc, COUNT(ks.Makesach) AS shelf_count "
                   + "FROM khuvuc k "
                   + "LEFT JOIN kesach ks ON k.Makhuvuc = ks.Makhuvuc "
                   + "GROUP BY k.Makhuvuc, k.Tenkhuvuc, k.Motakhuvuc "
                   + "ORDER BY k.Makhuvuc ASC";
        List<ZoneDto> list = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new ZoneDto(
                        rs.getString("Makhuvuc"),
                        rs.getString("Tenkhuvuc"),
                        rs.getString("Motakhuvuc"),
                        rs.getInt("shelf_count")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn tóm tắt danh sách phân khu: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<ShelfDto> getShelvesByZone(String zoneId) {
        String sql = "SELECT ks.Makesach, ks.Makhuvuc, k.Tenkhuvuc, ks.Tenkesach, ks.Tang, ks.Motake, COUNT(cs.Macuonsach) AS copy_count "
                   + "FROM kesach ks "
                   + "JOIN khuvuc k ON ks.Makhuvuc = k.Makhuvuc "
                   + "LEFT JOIN cuonsach cs ON ks.Makesach = cs.Makesach "
                   + "WHERE ks.Makhuvuc = ? "
                   + "GROUP BY ks.Makesach, ks.Makhuvuc, k.Tenkhuvuc, ks.Tenkesach, ks.Tang, ks.Motake "
                   + "ORDER BY ks.Makesach ASC";
        List<ShelfDto> list = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, zoneId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ShelfDto(
                            rs.getString("Makesach"),
                            rs.getString("Makhuvuc"),
                            rs.getString("Tenkhuvuc"),
                            rs.getString("Tenkesach"),
                            rs.getInt("Tang"),
                            rs.getString("Motake"),
                            rs.getInt("copy_count")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn tóm tắt kệ sách theo khu vực: " + zoneId, e);
        }
        return list;
    }

    @Override
    public List<ShelfDto> getAllShelfSummaries() {
        String sql = "SELECT ks.Makesach, ks.Makhuvuc, k.Tenkhuvuc, ks.Tenkesach, ks.Tang, ks.Motake, COUNT(cs.Macuonsach) AS copy_count "
                   + "FROM kesach ks "
                   + "JOIN khuvuc k ON ks.Makhuvuc = k.Makhuvuc "
                   + "LEFT JOIN cuonsach cs ON ks.Makesach = cs.Makesach "
                   + "GROUP BY ks.Makesach, ks.Makhuvuc, k.Tenkhuvuc, ks.Tenkesach, ks.Tang, ks.Motake "
                   + "ORDER BY ks.Makesach ASC";
        List<ShelfDto> list = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new ShelfDto(
                        rs.getString("Makesach"),
                        rs.getString("Makhuvuc"),
                        rs.getString("Tenkhuvuc"),
                        rs.getString("Tenkesach"),
                        rs.getInt("Tang"),
                        rs.getString("Motake"),
                        rs.getInt("copy_count")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn toàn bộ tóm tắt kệ sách: " + e.getMessage(), e);
        }
        return list;
    }
}
