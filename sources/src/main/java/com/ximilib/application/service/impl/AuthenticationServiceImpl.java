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

    // Bộ nhớ đệm theo dõi số lần đăng nhập sai và khóa tạm thời đảm bảo độ chính xác tuyệt đối
    private static final java.util.concurrent.ConcurrentHashMap<String, Integer> FAILED_ATTEMPTS_CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.concurrent.ConcurrentHashMap<String, LocalDateTime> LOCKED_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

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

        String normKey = identifier.trim().toLowerCase();
        LocalDateTime cachedLock = LOCKED_CACHE.get(normKey);
        LocalDateTime now = LocalDateTime.now();

        // 1. Kiểm tra khóa tạm thời trong bộ nhớ đệm
        if (cachedLock != null) {
            if (cachedLock.isAfter(now)) {
                long remainingMinutes = Math.max(1, Duration.between(now, cachedLock).toMinutes() + 1);
                return LoginResult.locked(remainingMinutes,
                    "Tài khoản đang bị tạm khóa " + remainingMinutes + " phút do nhập sai mật khẩu quá 5 lần. Vui lòng thử lại sau.");
            } else {
                LOCKED_CACHE.remove(normKey);
                FAILED_ATTEMPTS_CACHE.remove(normKey);
            }
        }

        UserRepository repo = getRepo();
        Optional<User> userOpt = repo.findByIdentifier(identifier.trim());

        if (userOpt.isEmpty()) {
            return LoginResult.fail("AUTH_001", "Tên đăng nhập hoặc mật khẩu không chính xác.");
        }

        User user = userOpt.get();

        // 2. Kiểm tra khóa tạm thời từ CSDL
        LocalDateTime dbLock = user.getLockedUntil();
        if (dbLock != null) {
            if (dbLock.isAfter(now)) {
                long remainingMinutes = Math.max(1, Duration.between(now, dbLock).toMinutes() + 1);
                LOCKED_CACHE.put(normKey, dbLock);
                return LoginResult.locked(remainingMinutes,
                    "Tài khoản đang bị tạm khóa " + remainingMinutes + " phút do nhập sai mật khẩu quá 5 lần. Vui lòng thử lại sau.");
            } else {
                repo.resetFailedAttempts(user.getId());
                user.setFailedAttempts(0);
                user.setLockedUntil(null);
                FAILED_ATTEMPTS_CACHE.remove(normKey);
                LOCKED_CACHE.remove(normKey);
            }
        }

        // 3. Kiểm tra mật khẩu bằng BCrypt
        boolean passwordMatches = false;
        try {
            passwordMatches = getHasher().matches(password, user.getPassword());
        } catch (Exception ignored) {
            passwordMatches = false;
        }

        if (!passwordMatches) {
            int currentFailures = Math.max(user.getFailedAttempts(), FAILED_ATTEMPTS_CACHE.getOrDefault(normKey, 0));
            int attempts = currentFailures + 1;
            FAILED_ATTEMPTS_CACHE.put(normKey, attempts);

            if (attempts >= LibraryPolicy.MAX_LOGIN_ATTEMPTS) {
                LocalDateTime lockTime = LocalDateTime.now().plusMinutes(LibraryPolicy.LOCKOUT_MINUTES);
                LOCKED_CACHE.put(normKey, lockTime);
                try {
                    repo.updateFailedAttempts(user.getId(), attempts, lockTime);
                } catch (Exception ignored) {}
                return LoginResult.locked(LibraryPolicy.LOCKOUT_MINUTES,
                    "Tài khoản bị tạm khóa 15 phút do nhập sai mật khẩu 5 lần liên tiếp. Vui lòng thử lại sau.");
            } else {
                try {
                    repo.updateFailedAttempts(user.getId(), attempts, null);
                } catch (Exception ignored) {}
                int remaining = LibraryPolicy.MAX_LOGIN_ATTEMPTS - attempts;
                return LoginResult.fail("AUTH_001",
                    "Tên đăng nhập hoặc mật khẩu không chính xác. Bạn đã nhập sai " + attempts + "/5 lần (còn " + remaining + " lần thử).");
            }
        }

        // 4. Đăng nhập thành công: Đặt lại toàn bộ số lần sai
        FAILED_ATTEMPTS_CACHE.remove(normKey);
        LOCKED_CACHE.remove(normKey);
        try {
            repo.resetFailedAttempts(user.getId());
        } catch (Exception ignored) {}
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