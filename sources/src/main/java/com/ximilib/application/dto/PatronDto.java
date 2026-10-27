package com.ximilib.application.dto;

import java.time.LocalDate;

/**
 * PatronDto: Thông tin hồ sơ bạn đọc và thẻ thư viện (UC-2, F-04).
 */
public class PatronDto {
    private final String userId;
    private final String username;
    private final String fullName;
    private final String email;
    private final String phone;
    private final String cardNumber;
    private final LocalDate issueDate;
    private final LocalDate expiryDate;
    private final String cardStatus;
    private final boolean expired;

    public PatronDto(String userId, String username, String fullName, String email, String phone,
                     String cardNumber, LocalDate issueDate, LocalDate expiryDate, String cardStatus) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.cardNumber = cardNumber;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.cardStatus = cardStatus;
        this.expired = (expiryDate != null && expiryDate.isBefore(LocalDate.now()));
    }

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getCardNumber() { return cardNumber; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public String getCardStatus() { return cardStatus; }
    public boolean isExpired() { return expired; }
}
