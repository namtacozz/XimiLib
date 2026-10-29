package com.ximilib.application.service;

import com.ximilib.application.dto.LoginResult;
import com.ximilib.application.dto.RegisterRequestDto;
import com.ximilib.application.port.PasswordHasher;
import com.ximilib.application.service.impl.AuthenticationServiceImpl;
import com.ximilib.domain.repository.UserRepository;
import com.ximilib.infrastructure.security.SecurityUtil;
import com.ximilib.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Nghiệp vụ Xác Thực & Đăng Ký (AuthenticationService - UC-1, UC-2 & BR-01)")
class AuthenticationServiceTest {

    private AuthenticationServiceImpl authService;
    private StubUserRepository userRepository;
    private PasswordHasher passwordHasher;

    @BeforeEach
    void setUp() {
        passwordHasher = new SecurityUtil();
        userRepository = new StubUserRepository(passwordHasher);
        authService = new AuthenticationServiceImpl(userRepository, passwordHasher);
    }

    @Test
    @DisplayName("TC-AUTH-01: Đăng nhập thành công bằng Tên đăng nhập")
    void testLoginSuccessWithUsername() {
        LoginResult result = authService.login("kien.phan", "123456");

        assertTrue(result.isSuccess());
        assertNotNull(result.getUser());
        assertEquals("Phan Vũ Xuân Kiên", result.getUser().getFullName());
        assertEquals("Thủ Thư", result.getUser().getRole());
        assertNull(result.getErrorCode());
    }

    @Test
    @DisplayName("TC-AUTH-02: Đăng nhập thành công bằng Email (không phân biệt hoa thường)")
    void testLoginSuccessWithEmail() {
        LoginResult result = authService.login("NHAT.NGUYEN@DUT.UDN.VN", "123456");

        assertTrue(result.isSuccess());
        assertNotNull(result.getUser());
        assertEquals("admin", result.getUser().getUsername());
        assertEquals("Quản Trị", result.getUser().getRole());
    }

    @Test
    @DisplayName("TC-AUTH-03: Đăng nhập thất bại do sai mật khẩu - Trả về mã lỗi an toàn AUTH_001")
    void testLoginFailWrongPassword() {
        LoginResult result = authService.login("kien.phan", "wrongpassword");

        assertFalse(result.isSuccess());
        assertNull(result.getUser());
        assertEquals("AUTH_001", result.getErrorCode());
        assertEquals("Tên đăng nhập hoặc mật khẩu không chính xác.", result.getErrorMessage());
    }

    @Test
    @DisplayName("TC-AUTH-04: Đăng nhập thất bại do tài khoản không tồn tại - Cùng thông điệp để chống liệt kê tài khoản")
    void testLoginFailNonExistentAccount() {
        LoginResult result = authService.login("unknown_account", "123456");

        assertFalse(result.isSuccess());
        assertEquals("AUTH_001", result.getErrorCode());
        assertEquals("Tên đăng nhập hoặc mật khẩu không chính xác.", result.getErrorMessage());
    }

    @Test
    @DisplayName("TC-AUTH-05: Tuân thủ BR-01 - Nhập sai 5 lần liên tiếp sẽ bị khóa tạm thời 15 phút")
    void testBR01AccountLockedAfterFiveFailedAttempts() {
        String username = "kien.phan";

        for (int i = 1; i <= 4; i++) {
            LoginResult r = authService.login(username, "wrong_" + i);
            assertFalse(r.isSuccess());
            assertEquals("AUTH_001", r.getErrorCode());
        }

        LoginResult lockedResult = authService.login(username, "wrong_5");
        assertFalse(lockedResult.isSuccess());
        assertEquals("AUTH_002", lockedResult.getErrorCode());
        assertTrue(lockedResult.getRemainingLockMinutes() > 0);
        assertTrue(lockedResult.getErrorMessage().contains("khóa 15 phút"));

        LoginResult blockedResult = authService.login(username, "123456");
        assertFalse(blockedResult.isSuccess());
        assertEquals("AUTH_002", blockedResult.getErrorCode());
    }

    @Test
    @DisplayName("TC-AUTH-06: Đăng nhập đúng mật khẩu sẽ reset bộ đếm số lần sai về 0 (BR-01)")
    void testSuccessfulLoginResetsFailedAttempts() {
        String username = "nam.le";

        authService.login(username, "bad1");
        authService.login(username, "bad2");
        authService.login(username, "bad3");

        LoginResult okResult = authService.login(username, "123456");
        assertTrue(okResult.isSuccess());

        authService.logout();
        for (int i = 1; i <= 4; i++) {
            LoginResult r = authService.login(username, "bad_after_reset_" + i);
            assertEquals("AUTH_001", r.getErrorCode());
        }
    }

    @Test
    @DisplayName("TC-AUTH-07: Xử lý rỗng hoặc khoảng trắng an toàn")
    void testEmptyInputs() {
        LoginResult r1 = authService.login("", "123456");
        assertFalse(r1.isSuccess());
        assertEquals("AUTH_001", r1.getErrorCode());

        LoginResult r2 = authService.login("kien.phan", "");
        assertFalse(r2.isSuccess());
        assertEquals("AUTH_001", r2.getErrorCode());
    }

    @Test
    @DisplayName("TC-AUTH-08: Đăng ký độc giả mới thành công và sinh thẻ thư viện 365 ngày")
    void testRegisterReaderSuccess() {
        RegisterRequestDto req = new RegisterRequestDto(
            "reader.new",
            "Matkhau@123",
            "Độc Giả Mới",
            "reader.new@gmail.com",
            "0912345678",
            "012345678901",
            "Đà Nẵng",
            LocalDate.of(2003, 5, 20)
        );

        assertDoesNotThrow(() -> authService.register(req));
        assertTrue(userRepository.existsByUsername("reader.new"));
    }

    @Test
    @DisplayName("TC-AUTH-09: Chặn đăng ký khi mật khẩu không đủ độ phức tạp")
    void testRegisterWeakPasswordFails() {
        RegisterRequestDto req = new RegisterRequestDto(
            "reader.weak",
            "123456",
            "Độc Giả Yếu",
            "weak@gmail.com",
            "0912345679",
            "012345678902",
            "Đà Nẵng",
            LocalDate.of(2003, 5, 20)
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> authService.register(req));
        assertTrue(ex.getMessage().contains("Mật khẩu phải từ 8 ký tự"));
    }

    @Test
    @DisplayName("TC-AUTH-10: CSDL chứa mật khẩu thô (plaintext) - Chặn hoàn toàn, tuyệt đối không đăng nhập được")
    void testPlaintextPasswordInDatabaseFailsToLogin() {
        // Tài khoản plain.user có mật khẩu lưu trực tiếp là "123456" (chưa băm BCrypt)
        LoginResult result = authService.login("plain.user", "123456");

        assertFalse(result.isSuccess(), "Mật khẩu thô không khớp định dạng BCrypt phải bị từ chối");
        assertNull(result.getUser());
        assertEquals("AUTH_001", result.getErrorCode());
        assertEquals("Tên đăng nhập hoặc mật khẩu không chính xác.", result.getErrorMessage());
    }

    @Test
    @DisplayName("TC-AUTH-11: CSDL chứa chuỗi hash hỏng / không hợp lệ - Trả lỗi AUTH chung, không văng exception")
    void testCorruptHashInDatabaseFailsGracefully() {
        // Tài khoản corrupt.user có chuỗi hash không hợp lệ
        assertDoesNotThrow(() -> {
            LoginResult result = authService.login("corrupt.user", "123456");
            assertFalse(result.isSuccess());
            assertNull(result.getUser());
            assertEquals("AUTH_001", result.getErrorCode());
            assertEquals("Tên đăng nhập hoặc mật khẩu không chính xác.", result.getErrorMessage());
        });
    }

    @Test
    @DisplayName("TC-AUTH-12: Giữ nguyên BR-01 với tài khoản có hash hỏng hoặc thô (nhập sai 5 lần vẫn bị khóa 15 phút)")
    void testBr01LockoutWorksOnPlaintextAccount() {
        String username = "plain.user";

        for (int i = 1; i <= 4; i++) {
            LoginResult r = authService.login(username, "123456");
            assertFalse(r.isSuccess());
            assertEquals("AUTH_001", r.getErrorCode());
        }

        LoginResult lockedResult = authService.login(username, "123456");
        assertFalse(lockedResult.isSuccess());
        assertEquals("AUTH_002", lockedResult.getErrorCode());
        assertTrue(lockedResult.getRemainingLockMinutes() > 0);
        assertTrue(lockedResult.getErrorMessage().contains("khóa 15 phút"));
    }

    @Test
    @DisplayName("TC-AUTH-13: Hoạt động chuẩn xác với Fake/Mock PasswordHasher (Inversion of Control)")
    void testLoginWithFakePasswordHasherPort() {
        // Tạo fake PasswordHasher dùng tiền tố "fakehash_"
        PasswordHasher fakeHasher = new PasswordHasher() {
            @Override
            public String hash(String rawPassword) {
                return "fakehash_" + rawPassword;
            }

            @Override
            public boolean matches(String rawPassword, String hashedPassword) {
                return ("fakehash_" + rawPassword).equals(hashedPassword);
            }

            @Override
            public String encrypt(String plainText) {
                return "encrypted_" + plainText;
            }
        };

        StubUserRepository stubRepo = new StubUserRepository(fakeHasher);
        AuthenticationServiceImpl customService = new AuthenticationServiceImpl(stubRepo, fakeHasher);

        // Đăng nhập thành công qua fakeHasher
        LoginResult successResult = customService.login("admin", "123456");
        assertTrue(successResult.isSuccess());
        assertEquals("admin", successResult.getUser().getUsername());

        // Đăng nhập sai qua fakeHasher
        LoginResult failResult = customService.login("admin", "wrongpass");
        assertFalse(failResult.isSuccess());
        assertEquals("AUTH_001", failResult.getErrorCode());
    }

    private static class StubUserRepository implements UserRepository {
        private final Map<String, User> userMap = new HashMap<>();

        public StubUserRepository(PasswordHasher hasher) {
            String hash123456 = hasher.hash("123456");
            User u1 = new User("ND001", "admin", hash123456, "Nguyễn Việt Nhật", "nhat.nguyen@dut.udn.vn", "0901", "Quản Trị");
            User u2 = new User("ND002", "kien.phan", hash123456, "Phan Vũ Xuân Kiên", "kien.phan@dut.udn.vn", "0902", "Thủ Thư");
            User u3 = new User("ND003", "nam.le", hash123456, "Lê Võ Thành Nam", "nam.le@dut.udn.vn", "0903", "Thủ Thư");
            User u4 = new User("ND004", "sinhvien", hash123456, "Trần Văn An", "an.tran@dut.udn.vn", "0904", "Bạn Đọc");

            // Tài khoản lưu mật khẩu dạng chuỗi trần (plaintext) trong DB
            User uPlain = new User("ND099", "plain.user", "123456", "Người Dùng Plaintext", "plain@dut.udn.vn", "0909", "Bạn Đọc");

            // Tài khoản lưu hash bị hỏng (corrupted hash) trong DB
            User uCorrupt = new User("ND098", "corrupt.user", "$2a$10$abcdefghijklmno_corrupt_hash_invalid", "Người Dùng Hash Hỏng", "corrupt@dut.udn.vn", "0908", "Bạn Đọc");

            userMap.put(u1.getId(), u1);
            userMap.put(u2.getId(), u2);
            userMap.put(u3.getId(), u3);
            userMap.put(u4.getId(), u4);
            userMap.put(uPlain.getId(), uPlain);
            userMap.put(uCorrupt.getId(), uCorrupt);
        }

        @Override
        public Optional<User> findByIdentifier(String identifier) {
            String norm = identifier.trim().toLowerCase();
            return userMap.values().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(norm) ||
                             u.getEmail().equalsIgnoreCase(norm) ||
                             (u.getPhone() != null && u.getPhone().equals(identifier.trim())))
                .findFirst();
        }

        @Override
        public Optional<User> findById(String id) {
            return Optional.ofNullable(userMap.get(id));
        }

        @Override
        public void updateFailedAttempts(String userId, int failedAttempts, LocalDateTime lockedUntil) {
            User u = userMap.get(userId);
            if (u != null) {
                u.setFailedAttempts(failedAttempts);
                u.setLockedUntil(lockedUntil);
            }
        }

        @Override
        public void resetFailedAttempts(String userId) {
            User u = userMap.get(userId);
            if (u != null) {
                u.setFailedAttempts(0);
                u.setLockedUntil(null);
            }
        }

        @Override
        public boolean existsByUsername(String username) {
            return userMap.values().stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
        }

        @Override
        public boolean existsByEmail(String email) {
            return userMap.values().stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
        }

        @Override
        public boolean existsByPhone(String phone) {
            return userMap.values().stream().anyMatch(u -> phone.equals(u.getPhone()));
        }

        @Override
        public void registerReader(User user, String cardNumber, LocalDate issueDate, LocalDate expiryDate) {
            userMap.put(user.getId(), user);
        }

        @Override
        public String generateNextUserId() {
            return "ND" + String.format("%03d", userMap.size() + 1);
        }

        @Override
        public String generateNextCardNumber() {
            return "LIB-2026-" + String.format("%05d", userMap.size() + 1);
        }

        @Override
        public List<com.ximilib.application.dto.PatronDto> searchPatrons(String keyword) {
            return Collections.emptyList();
        }

        @Override
        public void updateCardStatus(String cardNumber, String status) {}

        @Override
        public void extendCardValidity(String cardNumber, int days) {}

        @Override
        public List<com.ximilib.application.dto.UserAccountDto> getAllUsers(String keyword, String roleCode) {
            List<com.ximilib.application.dto.UserAccountDto> list = new ArrayList<>();
            for (User u : userMap.values()) {
                list.add(new com.ximilib.application.dto.UserAccountDto(
                    u.getId(), u.getUsername(), u.getFullName(), u.getEmail(), u.getPhone(),
                    "READER", u.getRole(), true, "LIB-001", null
                ));
            }
            return list;
        }

        @Override
        public void createUser(User user, String roleCode) {
            userMap.put(user.getId(), user);
        }

        @Override
        public void updateUserRole(String userId, String roleCode) {
            User u = userMap.get(userId);
            if (u != null) u.setRole(roleCode);
        }

        @Override
        public void updateUserStatus(String userId, int status) {}

        @Override
        public void resetPassword(String userId, String hashedPassword) {
            User u = userMap.get(userId);
            if (u != null) u.setPassword(hashedPassword);
        }
    }
}