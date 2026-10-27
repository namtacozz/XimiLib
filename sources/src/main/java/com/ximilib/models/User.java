package com.ximilib.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * User: Đối tượng đại diện cho tài khoản người dùng trong hệ thống XimiLib.
 * Khớp với cấu trúc bảng 'nguoidung' và thẻ 'thethuvien' trong CSDL MySQL.
 */
public class User {
    private String id;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String role; // "Quản Trị", "Thủ Thư", "Bạn Đọc"
    private LocalDate birthDate;
    private String address;
    private String cccd;
    private int failedAttempts;
    private LocalDateTime lockedUntil;
    private String cardNumber;
    private boolean isDeleted;

    public User() {}

    public User(String id, String username, String password, String fullName, String email, String phone, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }

    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public boolean isAdmin() {
        if (role == null) return false;
        String s = role.trim().toUpperCase();
        return s.contains("QUẢN TRỊ") || s.contains("ADMIN");
    }

    public boolean isLibrarian() {
        if (role == null) return false;
        String s = role.trim().toUpperCase();
        return s.contains("THỦ THƯ") || s.contains("LIBRARIAN");
    }

    public boolean isReader() {
        return !isAdmin() && !isLibrarian();
    }
}
