package com.ximilib.application.service.impl;

import com.ximilib.application.dto.LoginResult;
import com.ximilib.application.dto.RegisterRequestDto;
import com.ximilib.application.port.PasswordHasher;
import com.ximilib.application.port.UserQueryPort;
import com.ximilib.application.service.AuthenticationService;
import com.ximilib.domain.model.LibraryPolicy;
import com.ximilib.domain.repository.UserRepository;
import com.ximilib.domain.model.User;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * AuthenticationServiceImpl: Triển khai nghiệp vụ xác thực và đăng ký bạn đọc.
 * Tuân thủ R-01, R-08, BR-01 và SRS UC-1, UC-2.
 */
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final Pattern PWD_PATTERN = Pattern.compile("^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$");
    private static final Pattern CCCD_PATTERN = Pattern.compile("^(\\d{9}|\\d{12})$");

    private final UserRepository userRepository;
    private final UserQueryPort userQueryPort;
    private final PasswordHasher passwordHasher;

    public AuthenticationServiceImpl(UserRepository userRepository, PasswordHasher passwordHasher) {
        this(userRepository, (userRepository instanceof UserQueryPort uqp) ? uqp : null, passwordHasher);
    }

    public AuthenticationServiceImpl(UserRepository userRepository, UserQueryPort userQueryPort, PasswordHasher passwordHasher) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository không được null");
        this.userQueryPort = (userQueryPort != null) ? userQueryPort : ((userRepository instanceof UserQueryPort uqp) ? uqp : null);
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher không được null");
    }

    private UserRepository getRepo() {
        return userRepository;
    }

    private PasswordHasher getHasher() {
        return passwordHasher;
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

        // 2. Kiểm tra mật khẩu bắt buộc bằng BCrypt qua PasswordHasher port (R-08, BR-01)
        boolean passwordMatches = false;
        try {
            passwordMatches = getHasher().matches(password, user.getPassword());
        } catch (Exception ignored) {
            // Hash không hợp lệ / lỗi giải mã: trả lỗi AUTH chung, không lộ thông tin nội bộ
            passwordMatches = false;
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

        // 3. Đăng nhập thành công: Reset số lần sai và trả về kết quả
        repo.resetFailedAttempts(user.getId());
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

    private boolean validatePasswordStrength(String password) {
        return password != null && PWD_PATTERN.matcher(password).matches();
    }

    private boolean validateCccd(String cccd) {
        return cccd != null && CCCD_PATTERN.matcher(cccd.trim()).matches();
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

        if (!validatePasswordStrength(rawPassword)) {
            throw new IllegalArgumentException("Mật khẩu phải từ 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt.");
        }

        if (!cccd.isEmpty() && !validateCccd(cccd)) {
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

        String hashedPassword = getHasher().hash(rawPassword);
        String encryptedCccd = (!cccd.isEmpty()) ? getHasher().encrypt(cccd) : "";

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
        // Presentation layer tự quản lý trạng thái phiên làm việc (UserSession)
    }

    @Override
    public List<com.ximilib.application.dto.PatronDto> searchPatrons(String keyword) {
        if (userQueryPort != null) {
            return userQueryPort.searchPatrons(keyword);
        }
        return List.of();
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
        if (userQueryPort != null) {
            return userQueryPort.getAllUsers(keyword, roleCode);
        }
        return List.of();
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
        String hashedPassword = getHasher().hash(rawPassword);

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
        String hashed = getHasher().hash(rawPassword);
        getRepo().resetPassword(userId.trim(), hashed);
    }
}