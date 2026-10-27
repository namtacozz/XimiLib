package com.ximilib.infrastructure.persistence.impl;

import com.ximilib.application.dto.PatronDto;
import com.ximilib.application.dto.UserAccountDto;
import com.ximilib.domain.repository.UserRepository;
import com.ximilib.infrastructure.persistence.connection.DatabaseConnectionManager;
import com.ximilib.models.User;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * UserRepositoryImpl: Truy vấn và lưu trữ dữ liệu người dùng qua JDBC PreparedStatement.
 * Tuân thủ R-01, R-03, R-05, R-07.
 */
public class UserRepositoryImpl implements UserRepository {

    private final DatabaseConnectionManager connectionManager;

    public UserRepositoryImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = (connectionManager != null) ? connectionManager : new DatabaseConnectionManager();
    }

    @Override
    public Optional<User> findByIdentifier(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return Optional.empty();
        }
        String sql = """
            SELECT u.ID, u.Tendangnhap, u.Matkhau, u.Hoten, u.Email, u.Sdt,
                   v.Tenvaitro, u.Ngaysinh, u.Diachi, u.Cccd, u.solansai, u.khoaden,
                   t.Mathe, u.Trangthai
            FROM nguoidung u
            LEFT JOIN vaitro v ON u.Mavaitro = v.Mavaitro
            LEFT JOIN thethuvien t ON u.ID = t.ID
            WHERE u.Trangthai = 1 AND (LOWER(u.Tendangnhap) = ? OR LOWER(u.Email) = ? OR u.Sdt = ?)
            LIMIT 1;
        """;
        String normalized = identifier.trim().toLowerCase();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalized);
            ps.setString(2, normalized);
            ps.setString(3, identifier.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn người dùng: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findById(String id) {
        if (id == null || id.trim().isEmpty()) return Optional.empty();
        String sql = """
            SELECT u.ID, u.Tendangnhap, u.Matkhau, u.Hoten, u.Email, u.Sdt,
                   v.Tenvaitro, u.Ngaysinh, u.Diachi, u.Cccd, u.solansai, u.khoaden,
                   t.Mathe, u.Trangthai
            FROM nguoidung u
            LEFT JOIN vaitro v ON u.Mavaitro = v.Mavaitro
            LEFT JOIN thethuvien t ON u.ID = t.ID
            WHERE u.ID = ? AND u.Trangthai = 1
            LIMIT 1;
        """;
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn người dùng theo ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public void updateFailedAttempts(String userId, int failedAttempts, LocalDateTime lockedUntil) {
        String sql = "UPDATE nguoidung SET solansai = ?, khoaden = ? WHERE ID = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, failedAttempts);
            if (lockedUntil != null) {
                ps.setTimestamp(2, Timestamp.valueOf(lockedUntil));
            } else {
                ps.setNull(2, Types.TIMESTAMP);
            }
            ps.setString(3, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật số lần đăng nhập sai: " + e.getMessage(), e);
        }
    }

    @Override
    public void resetFailedAttempts(String userId) {
        String sql = "UPDATE nguoidung SET solansai = 0, khoaden = NULL WHERE ID = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đặt lại số lần đăng nhập sai: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        return checkExists("SELECT 1 FROM nguoidung WHERE LOWER(Tendangnhap) = ?", username.trim().toLowerCase());
    }

    @Override
    public boolean existsByEmail(String email) {
        return checkExists("SELECT 1 FROM nguoidung WHERE LOWER(Email) = ?", email.trim().toLowerCase());
    }

    @Override
    public boolean existsByPhone(String phone) {
        return checkExists("SELECT 1 FROM nguoidung WHERE Sdt = ?", phone.trim());
    }

    @Override
    public void registerReader(User user, String cardNumber, LocalDate issueDate, LocalDate expiryDate) {
        String insertUserSql = """
            INSERT INTO nguoidung (ID, Tendangnhap, Matkhau, Hoten, Email, Sdt, Mavaitro, Trangthai, Ngaysinh, Diachi, Cccd, solansai)
            VALUES (?, ?, ?, ?, ?, ?, 'READER', 1, ?, ?, ?, 0)
        """;
        String insertCardSql = """
            INSERT INTO thethuvien (Mathe, ID, Ngaycap, Ngayhethan, Trangthai)
            VALUES (?, ?, ?, ?, 1)
        """;

        try (Connection conn = connectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement psUser = conn.prepareStatement(insertUserSql)) {
                    psUser.setString(1, user.getId());
                    psUser.setString(2, user.getUsername());
                    psUser.setString(3, user.getPassword());
                    psUser.setString(4, user.getFullName());
                    psUser.setString(5, user.getEmail());
                    psUser.setString(6, user.getPhone());
                    if (user.getBirthDate() != null) {
                        psUser.setDate(7, Date.valueOf(user.getBirthDate()));
                    } else {
                        psUser.setNull(7, Types.DATE);
                    }
                    psUser.setString(8, user.getAddress());
                    psUser.setString(9, user.getCccd());
                    psUser.executeUpdate();
                }

                try (PreparedStatement psCard = conn.prepareStatement(insertCardSql)) {
                    psCard.setString(1, cardNumber);
                    psCard.setString(2, user.getId());
                    psCard.setDate(3, Date.valueOf(issueDate));
                    psCard.setDate(4, Date.valueOf(expiryDate));
                    psCard.executeUpdate();
                }

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lưu đăng ký tài khoản bạn đọc: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateNextUserId() {
        String sql = "SELECT MAX(CAST(SUBSTRING(ID, 3) AS UNSIGNED)) FROM nguoidung WHERE ID LIKE 'ND%'";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int nextNum = 1;
            if (rs.next()) {
                nextNum = rs.getInt(1) + 1;
            }
            return String.format("ND%03d", nextNum);
        } catch (SQLException e) {
            return "ND" + System.currentTimeMillis();
        }
    }

    @Override
    public String generateNextCardNumber() {
        int year = LocalDate.now().getYear();
        String prefix = "LIB-" + year + "-";
        String sql = "SELECT MAX(CAST(SUBSTRING_INDEX(Mathe, '-', -1) AS UNSIGNED)) FROM thethuvien WHERE Mathe LIKE ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                int nextNum = 1;
                if (rs.next()) {
                    nextNum = rs.getInt(1) + 1;
                }
                return String.format("%s%05d", prefix, nextNum);
            }
        } catch (SQLException e) {
            return prefix + "00001";
        }
    }

    private boolean checkExists(String sql, String param) {
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getString("ID"));
        u.setUsername(rs.getString("Tendangnhap"));
        u.setPassword(rs.getString("Matkhau"));
        u.setFullName(rs.getString("Hoten"));
        u.setEmail(rs.getString("Email"));
        u.setPhone(rs.getString("Sdt"));
        u.setRole(rs.getString("Tenvaitro"));
        Date birth = rs.getDate("Ngaysinh");
        if (birth != null) u.setBirthDate(birth.toLocalDate());
        u.setAddress(rs.getString("Diachi"));
        u.setCccd(rs.getString("Cccd"));
        u.setFailedAttempts(rs.getInt("solansai"));
        Timestamp locked = rs.getTimestamp("khoaden");
        if (locked != null) u.setLockedUntil(locked.toLocalDateTime());
        u.setCardNumber(rs.getString("Mathe"));
        u.setDeleted(rs.getInt("Trangthai") != 1);
        return u;
    }

    @Override
    public List<PatronDto> searchPatrons(String keyword) {
        StringBuilder sql = new StringBuilder("""
            SELECT u.ID, u.Tendangnhap, u.Hoten, u.Email, u.Sdt,
                   t.Mathe, t.Ngaycap, t.Ngayhethan, t.Trangthai
            FROM nguoidung u
            LEFT JOIN thethuvien t ON u.ID = t.ID
            WHERE u.Trangthai = 1 AND u.Mavaitro = 'READER'
        """);
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(u.Hoten) LIKE ? OR LOWER(u.Email) LIKE ? OR LOWER(t.Mathe) LIKE ? OR LOWER(u.Tendangnhap) LIKE ?) ");
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            params.add(kw); params.add(kw); params.add(kw); params.add(kw);
        }
        sql.append(" ORDER BY u.ID DESC LIMIT 100");

        List<PatronDto> list = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date issue = rs.getDate("Ngaycap");
                    Date expiry = rs.getDate("Ngayhethan");
                    list.add(new PatronDto(
                        rs.getString("ID"),
                        rs.getString("Tendangnhap"),
                        rs.getString("Hoten"),
                        rs.getString("Email"),
                        rs.getString("Sdt"),
                        rs.getString("Mathe"),
                        issue != null ? issue.toLocalDate() : null,
                        expiry != null ? expiry.toLocalDate() : null,
                        rs.getString("Mathe") == null ? "CHUA_CAP" : (rs.getInt("Trangthai") == 1 ? "HOAT_DONG" : "KHOA")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm kiếm danh sách bạn đọc: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void updateCardStatus(String cardNumber, String status) {
        String sql = "UPDATE thethuvien SET Trangthai = ? WHERE Mathe = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ("HOAT_DONG".equalsIgnoreCase(status) || "1".equals(status)) ? 1 : 0);
            ps.setString(2, cardNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật trạng thái thẻ: " + e.getMessage(), e);
        }
    }

    @Override
    public void extendCardValidity(String cardNumber, int days) {
        String sql = "UPDATE thethuvien SET Ngayhethan = DATE_ADD(GREATEST(Ngayhethan, CURDATE()), INTERVAL ? DAY) WHERE Mathe = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days);
            ps.setString(2, cardNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi gia hạn thẻ thư viện: " + e.getMessage(), e);
        }
    }

    @Override
    public List<UserAccountDto> getAllUsers(String keyword, String roleCode) {
        List<UserAccountDto> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT u.ID, u.Tendangnhap, u.Hoten, u.Email, u.Sdt,
                   u.Mavaitro, v.Tenvaitro, u.Trangthai, t.Mathe, u.Ngaysinh
            FROM nguoidung u
            LEFT JOIN vaitro v ON u.Mavaitro = v.Mavaitro
            LEFT JOIN thethuvien t ON u.ID = t.ID
            WHERE 1=1
        """);
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(u.Tendangnhap) LIKE ? OR LOWER(u.Hoten) LIKE ? OR LOWER(u.Email) LIKE ? OR u.Sdt LIKE ?)");
            String k = "%" + keyword.trim().toLowerCase() + "%";
            params.add(k); params.add(k); params.add(k); params.add("%" + keyword.trim() + "%");
        }
        if (roleCode != null && !roleCode.trim().isEmpty() && !"ALL".equalsIgnoreCase(roleCode)) {
            sql.append(" AND u.Mavaitro = ?");
            params.add(roleCode.trim());
        }
        sql.append(" ORDER BY u.Mavaitro ASC, u.ID ASC");

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date bDate = rs.getDate("Ngaysinh");
                    list.add(new UserAccountDto(
                        rs.getString("ID"),
                        rs.getString("Tendangnhap"),
                        rs.getString("Hoten"),
                        rs.getString("Email"),
                        rs.getString("Sdt"),
                        rs.getString("Mavaitro"),
                        rs.getString("Tenvaitro"),
                        rs.getInt("Trangthai") == 1,
                        rs.getString("Mathe"),
                        bDate != null ? bDate.toLocalDate() : null
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy danh sách tài khoản: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void createUser(User user, String roleCode) {
        String sql = """
            INSERT INTO nguoidung (ID, Tendangnhap, Matkhau, Hoten, Email, Sdt, Mavaitro, Trangthai, solansai)
            VALUES (?, ?, ?, ?, ?, ?, ?, 1, 0)
        """;
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getId());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getPhone());
            ps.setString(7, roleCode);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tạo tài khoản người dùng: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateUserRole(String userId, String roleCode) {
        String sql = "UPDATE nguoidung SET Mavaitro = ? WHERE ID = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleCode);
            ps.setString(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật vai trò người dùng: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateUserStatus(String userId, int status) {
        String sql = "UPDATE nguoidung SET Trangthai = ? WHERE ID = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, status);
            ps.setString(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật trạng thái người dùng: " + e.getMessage(), e);
        }
    }

    @Override
    public void resetPassword(String userId, String hashedPassword) {
        String sql = "UPDATE nguoidung SET Matkhau = ?, solansai = 0, khoaden = NULL WHERE ID = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setString(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đặt lại mật khẩu: " + e.getMessage(), e);
        }
    }
}