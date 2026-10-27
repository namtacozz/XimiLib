package com.ximilib.application.service.impl;

import com.ximilib.application.dto.LoginResult;
import com.ximilib.application.dto.RegisterRequestDto;
import com.ximilib.application.service.AuthenticationService;
import com.ximilib.domain.model.LibraryPolicy;
import com.ximilib.domain.repository.UserRepository;
import com.ximilib.gui.util.UserSession;
import com.ximilib.infrastructure.config.AppContainer;
import com.ximilib.infrastructure.security.SecurityUtil;
import com.ximilib.models.User;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * AuthenticationServiceImpl: Triển khai nghiệp vụ xác thực và đăng ký bạn đọc.
 * Tuân thủ R-01, R-08, BR-01 và SRS UC-1, UC-2.
 */
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;

    public AuthenticationServiceImpl() {
        this(null);
    }

    public AuthenticationServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private UserRepository getRepo() {
        return (userRepository != null) ? userRepository : AppContainer.getInstance().getUserRepository();
    }

    @Override
    public LoginResult login(String identifier, String password) {
        if (identifier == null || identifier.trim().isEmpty() || password == null || password.isEmpty()) {
            return LoginResult.fail("AUTH_001", "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.");
        }

        UserRepository repo = getRepo();
        Optional<User> userOpt = repo.findByIdentifier(identifier.trim());

        if (userOpt.isEmpty()) {
            return LoginResult.fail("AUTH_001", "Tên đăng nhập hoặc mật khẩu không chính xác.");
        }

        User user = userOpt.get();

        // 1. Kiểm tra khóa tạm 15 phút theo BR-01
        if (user.getLockedUntil() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (user.getLockedUntil().isAfter(now)) {
                long remainingMinutes = Math.max(1, Duration.between(now, user.getLockedUntil()).toMinutes() + 1);
                return LoginResult.locked(remainingMinutes,
                    "Tài khoản bị tạm khóa " + remainingMinutes + " phút do nhập sai quá 5 lần quy định (Quy tắc BR-01).");
            } else {
                repo.resetFailedAttempts(user.getId());
                user.setFailedAttempts(0);
                user.setLockedUntil(null);
            }
        }

        // 2. Kiểm tra mật khẩu (hỗ trợ cả băm BCrypt và chuỗi trần cho môi trường mock/test)
        boolean passwordMatches = false;
        try {
            passwordMatches = SecurityUtil.checkPassword(password, user.getPassword());
        } catch (Exception ignored) {}

        if (!passwordMatches && user.getPassword() != null && user.getPassword().equals(password)) {
            passwordMatches = true;
        }

        if (!passwordMatches) {
            int attempts = user.getFailedAttempts() + 1;
            if (attempts >= LibraryPolicy.MAX_LOGIN_ATTEMPTS) {
                LocalDateTime lockTime = LocalDateTime.now().plusMinutes(LibraryPolicy.LOCKOUT_MINUTES);
                repo.updateFailedAttempts(user.getId(), attempts, lockTime);
                return LoginResult.locked(LibraryPolicy.LOCKOUT_MINUTES,
                    "Tài khoản bị tạm khóa " + LibraryPolicy.LOCKOUT_MINUTES + " phút do nhập sai quá 5 lần quy định (Quy tắc BR-01).");
            } else {
                repo.updateFailedAttempts(user.getId(), attempts, null);
                return LoginResult.fail("AUTH_001", "Tên đăng nhập hoặc mật khẩu không chính xác.");
            }
        }

        // 3. Đăng nhập thành công: Reset số lần sai và cập nhật UserSession
        repo.resetFailedAttempts(user.getId());
        UserSession.setCurrentUser(user);
        return LoginResult.success(user);
    }

    @Override
    public long getRemainingLockMinutes(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) return 0;
        Optional<User> userOpt = getRepo().findByIdentifier(identifier.trim());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
                return Math.max(1, Duration.between(LocalDateTime.now(), user.getLockedUntil()).toMinutes() + 1);
            }
        }
        return 0;
    }

    @Override
    public void register(RegisterRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Thông tin đăng ký không được để trống.");
        }

        String username = request.getUsername() != null ? request.getUsername().trim() : "";
        String rawPassword = request.getPassword() != null ? request.getPassword().trim() : "";
        String fullName = request.getFullName() != null ? request.getFullName().trim() : "";
        String email = request.getEmail() != null ? request.getEmail().trim() : "";
        String phone = request.getPhone() != null ? request.getPhone().trim() : "";
        String cccd = request.getCccd() != null ? request.getCccd().trim() : "";

        if (username.isEmpty() || rawPassword.isEmpty() || fullName.isEmpty() || email.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng điền đầy đủ các thông tin bắt buộc!");
        }

        if (!SecurityUtil.validatePasswordStrength(rawPassword)) {
            throw new IllegalArgumentException("Mật khẩu phải từ 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt.");
        }

        if (!cccd.isEmpty() && !SecurityUtil.validateCccd(cccd)) {
            throw new IllegalArgumentException("Số CCCD/CMND không hợp lệ (phải gồm 9 hoặc 12 chữ số).");
        }

        UserRepository repo = getRepo();

        if (repo.existsByUsername(username)) {
            throw new IllegalArgumentException("Tên đăng nhập này đã tồn tại trong hệ thống.");
        }
        if (repo.existsByEmail(email)) {
            throw new IllegalArgumentException("Địa chỉ email này đã được đăng ký.");
        }
        if (!phone.isEmpty() && repo.existsByPhone(phone)) {
            throw new IllegalArgumentException("Số điện thoại này đã được đăng ký.");
        }

        String nextUserId = repo.generateNextUserId();
        String nextCardNumber = repo.generateNextCardNumber();

        String hashedPassword = SecurityUtil.hashPassword(rawPassword);
        String encryptedCccd = (!cccd.isEmpty()) ? SecurityUtil.encryptAes(cccd) : "";

        User newUser = new User();
        newUser.setId(nextUserId);
        newUser.setUsername(username);
        newUser.setPassword(hashedPassword);
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setPhone(phone);
        newUser.setRole("READER");
        newUser.setBirthDate(request.getBirthDate());
        newUser.setAddress(request.getAddress());
        newUser.setCccd(encryptedCccd);
        newUser.setCardNumber(nextCardNumber);

        LocalDate issueDate = LocalDate.now();
        LocalDate expiryDate = issueDate.plusDays(LibraryPolicy.CARD_VALIDITY_DAYS);

        repo.registerReader(newUser, nextCardNumber, issueDate, expiryDate);
    }

    @Override
    public void logout() {
        UserSession.setCurrentUser(null);
    }

    @Override
    public List<com.ximilib.application.dto.PatronDto> searchPatrons(String keyword) {
        return getRepo().searchPatrons(keyword);
    }

    @Override
    public void updateCardStatus(String cardNumber, String status) {
        getRepo().updateCardStatus(cardNumber, status);
    }

    @Override
    public void extendCardValidity(String cardNumber, int days) {
        getRepo().extendCardValidity(cardNumber, days);
    }

    @Override
    public List<com.ximilib.application.dto.UserAccountDto> getAllUsers(String keyword, String roleCode) {
        return getRepo().getAllUsers(keyword, roleCode);
    }

    @Override
    public void createUserAccount(String username, String rawPassword, String fullName, String email, String phone, String roleCode) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống.");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự.");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ và tên không được để trống.");
        }
        UserRepository repo = getRepo();
        if (repo.existsByUsername(username.trim())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại trong hệ thống.");
        }
        if (email != null && !email.trim().isEmpty() && repo.existsByEmail(email.trim())) {
            throw new IllegalArgumentException("Email đã được sử dụng bởi tài khoản khác.");
        }
        if (phone != null && !phone.trim().isEmpty() && repo.existsByPhone(phone.trim())) {
            throw new IllegalArgumentException("Số điện thoại đã được sử dụng bởi tài khoản khác.");
        }

        String nextUserId = repo.generateNextUserId();
        String hashedPassword = SecurityUtil.hashPassword(rawPassword);

        User newUser = new User();
        newUser.setId(nextUserId);
        newUser.setUsername(username.trim());
        newUser.setPassword(hashedPassword);
        newUser.setFullName(fullName.trim());
        newUser.setEmail(email != null ? email.trim() : "");
        newUser.setPhone(phone != null ? phone.trim() : "");
        newUser.setRole(roleCode);

        repo.createUser(newUser, roleCode);
    }

    @Override
    public void updateUserRole(String userId, String newRoleCode) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã người dùng không hợp lệ.");
        }
        getRepo().updateUserRole(userId.trim(), newRoleCode);
    }

    @Override
    public void toggleUserStatus(String userId, boolean active) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã người dùng không hợp lệ.");
        }
        getRepo().updateUserStatus(userId.trim(), active ? 1 : 0);
    }

    @Override
    public void resetUserPassword(String userId, String rawPassword) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã người dùng không hợp lệ.");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự.");
        }
        String hashed = SecurityUtil.hashPassword(rawPassword);
        getRepo().resetPassword(userId.trim(), hashed);
    }
}