package com.ximilib.domain.repository;

import com.ximilib.domain.model.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * UserRepository: Giao diện lưu trữ và truy vấn người dùng từ CSDL MySQL (bảng nguoidung, thethuvien, vaitro).
 * Tuân thủ Clean Architecture (Domain interface, không import SQL/JDBC/Swing - R-01).
 */
public interface UserRepository {

    Optional<User> findByIdentifier(String identifier);

    Optional<User> findById(String id);

    void updateFailedAttempts(String userId, int failedAttempts, LocalDateTime lockedUntil);

    void resetFailedAttempts(String userId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    void registerReader(User user, String cardNumber, LocalDate issueDate, LocalDate expiryDate);

    String generateNextUserId();

    String generateNextCardNumber();

    void updateCardStatus(String cardNumber, String status);

    void extendCardValidity(String cardNumber, int days);

    void createUser(User user, String roleCode);

    void updateUserRole(String userId, String roleCode);

    void updateUserStatus(String userId, int status);

    void resetPassword(String userId, String hashedPassword);
}
