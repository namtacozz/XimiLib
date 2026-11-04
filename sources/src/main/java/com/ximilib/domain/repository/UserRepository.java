package com.ximilib.domain.repository;

import com.ximilib.domain.model.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findByIdentifier(String identifier);

    Optional<User> findById(String id);

    void updateFailedAttempts(String userId, int failedAttempts, LocalDateTime lockedUntil);

    void resetFailedAttempts(String userId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    default boolean existsByCccd(String cccd) {
        return false;
    }

    boolean existsByPhone(String phone);

    void registerReader(User user, String cardNumber, LocalDate issueDate, LocalDate expiryDate);

    default void registerReader(TransactionContext tx, User user, String cardNumber, LocalDate issueDate, LocalDate expiryDate) {
        registerReader(user, cardNumber, issueDate, expiryDate);
    }

    String generateNextUserId();

    String generateNextCardNumber();

    void updateCardStatus(String cardNumber, String status);

    void extendCardValidity(String cardNumber, int days);

    void createUser(User user, String roleCode);

    void updateUserRole(String userId, String roleCode);

    void updateUserStatus(String userId, int status);

    void resetPassword(String userId, String hashedPassword);
}
