package com.ximilib.models;

/**
 * User: Đối tượng đại diện cho tài khoản người dùng trong hệ thống XimiLib.
 * Khớp với cấu trúc bảng 'nguoidung' trong CSDL MySQL.
 */
public class User {
    private String id;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String role; // "Quản Trị Viên (Admin)", "Thủ Thư (Librarian)", "Bạn Đọc (Sinh Viên)"

    public User(String id, String username, String password, String fullName, String email, String phone, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    // Getters and Setters
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
}
