package com.ximilib.application.service;

import com.ximilib.application.dto.LoginResult;
import com.ximilib.application.dto.RegisterRequestDto;
import com.ximilib.application.service.impl.AuthenticationServiceImpl;
import com.ximilib.domain.repository.UserRepository;
import com.ximilib.models.User;
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

    @BeforeEach
    void setUp() {
        userRepository = new StubUserRepository();
        authService = new AuthenticationServiceImpl(userRepository);
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

    private static class StubUserRepository implements UserRepository {
        private final Map<String, User> userMap = new HashMap<>();

        public StubUserRepository() {
            User u1 = new User("ND001", "admin", "123456", "Nguyễn Việt Nhật", "nhat.nguyen@dut.udn.vn", "0901", "Quản Trị");
            User u2 = new User("ND002", "kien.phan", "123456", "Phan Vũ Xuân Kiên", "kien.phan@dut.udn.vn", "0902", "Thủ Thư");
            User u3 = new User("ND003", "nam.le", "123456", "Lê Võ Thành Nam", "nam.le@dut.udn.vn", "0903", "Thủ Thư");
            User u4 = new User("ND004", "sinhvien", "123456", "Trần Văn An", "an.tran@dut.udn.vn", "0904", "Bạn Đọc");

            userMap.put(u1.getId(), u1);
            userMap.put(u2.getId(), u2);
            userMap.put(u3.getId(), u3);
            userMap.put(u4.getId(), u4);
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