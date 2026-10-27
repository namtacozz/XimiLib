package com.ximilib.application.dto;

import java.time.LocalDate;

/**
 * UserAccountDto: DTO quản trị tài khoản người dùng và phân quyền RBAC (YC-10).
 * Đại diện cho dữ liệu từ bảng `nguoidung` kết hợp `vaitro` và `thethuvien`.
 */
public class UserAccountDto {

    private String userId;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String roleCode;  // "ADMIN", "LIBRARIAN", "READER"
    private String roleName;  // "Quản trị viên", "Thủ thư", "Độc giả"
    private boolean active;   // Trangthai == 1
    private String cardNumber;
    private LocalDate birthDate;

    public UserAccountDto() {}

    public UserAccountDto(String userId, String username, String fullName, String email,
                          String phone, String roleCode, String roleName, boolean active,
                          String cardNumber, LocalDate birthDate) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.roleCode = roleCode;
        this.roleName = roleName;
        this.active = active;
        this.cardNumber = cardNumber;
        this.birthDate = birthDate;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
}
