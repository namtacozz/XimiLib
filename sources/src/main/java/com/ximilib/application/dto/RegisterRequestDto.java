package com.ximilib.application.dto;

import java.time.LocalDate;

/**
 * RegisterRequestDto: Dữ liệu đăng ký tài khoản bạn đọc mới (UC-2).
 */
public class RegisterRequestDto {
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String cccd;
    private String address;
    private LocalDate birthDate;

    public RegisterRequestDto() {}

    public RegisterRequestDto(String username, String password, String fullName, String email, String phone, String cccd, String address, LocalDate birthDate) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.cccd = cccd;
        this.address = address;
        this.birthDate = birthDate;
    }

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

    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
}
