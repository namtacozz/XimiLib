package com.ximilib.application.service;

import com.ximilib.application.dto.LoginResult;
import com.ximilib.application.dto.PatronDto;
import com.ximilib.application.dto.RegisterRequestDto;
import com.ximilib.application.dto.UserAccountDto;
import java.util.List;

public interface AuthenticationService {

    LoginResult login(String identifier, String password);

    long getRemainingLockMinutes(String identifier);

    void register(RegisterRequestDto request);

    void logout();

    List<PatronDto> searchPatrons(String keyword);

    void updateCardStatus(String cardNumber, String status);

    void extendCardValidity(String cardNumber, int days);

    List<UserAccountDto> getAllUsers(String keyword, String roleCode);

    void createUserAccount(String username, String rawPassword, String fullName, String email, String phone, String roleCode);

    void updateUserRole(String userId, String newRoleCode);

    void toggleUserStatus(String userId, boolean active);

    void resetUserPassword(String userId, String rawPassword);
}
