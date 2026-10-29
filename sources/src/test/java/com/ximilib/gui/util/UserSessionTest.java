package com.ximilib.gui.util;

import com.ximilib.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserSessionTest {

    @BeforeEach
    public void setUp() {
        UserSession.resetDefaultUsers();
    }

    @Test
    public void testAuthenticateSuccessWithUsername() {
        User user = UserSession.authenticate("kien.phan", "123456");
        assertNotNull(user);
        assertEquals("Phan Vũ Xuân Kiên", user.getFullName());
        assertEquals(user, UserSession.getCurrentUser());
    }

    @Test
    public void testAuthenticateSuccessWithEmailCaseInsensitive() {
        User user = UserSession.authenticate("NHAT.NGUYEN@DUT.UDN.VN", "123456");
        assertNotNull(user);
        assertEquals("admin", user.getUsername());
        assertEquals("Quản Trị", user.getRole());
    }

    @Test
    public void testAuthenticateFailWrongPassword() {
        User user = UserSession.authenticate("kien.phan", "wrongpassword");
        assertNull(user);
    }

    @Test
    public void testAuthenticateFailNonExistentUser() {
        User user = UserSession.authenticate("nonexistent", "123456");
        assertNull(user);
    }

    @Test
    public void testAuthenticateNullInputsDoesNotCrash() {
        assertNull(UserSession.authenticate(null, "123456"));
        assertNull(UserSession.authenticate("kien.phan", null));
        assertNull(UserSession.authenticate(null, null));
    }

    @Test
    public void testRegisterNewUserSuccess() {
        User newUser = new User("ND005", "testuser", "secret123", "Test User", "test@dut.udn.vn", "0905999888", "Độc Giả");
        boolean ok = UserSession.register(newUser);
        assertTrue(ok);

        User authenticated = UserSession.authenticate("testuser", "secret123");
        assertNotNull(authenticated);
        assertEquals("Test User", authenticated.getFullName());
    }

    @Test
    public void testRegisterDuplicateUsernameFails() {
        User duplicate = new User("ND006", "kien.phan", "pass", "Duplicate Kiên", "diff@dut.udn.vn", "090", "Độc Giả");
        boolean ok = UserSession.register(duplicate);
        assertFalse(ok);
    }

    @Test
    public void testRegisterDuplicateEmailFails() {
        User duplicate = new User("ND006", "unique.user", "pass", "Duplicate Email", "kien.phan@dut.udn.vn", "090", "Độc Giả");
        boolean ok = UserSession.register(duplicate);
        assertFalse(ok);
    }

    @Test
    public void testRegisterWithNullOrEmptyEmailDoesNotCrash() {
        User noEmailUser = new User("ND007", "user.no.email", "pass", "No Email", null, "090", "Độc Giả");
        assertTrue(UserSession.register(noEmailUser));

        User authenticated = UserSession.authenticate("user.no.email", "pass");
        assertNotNull(authenticated);
    }

    @Test
    public void testSetCurrentUserAndLogout() {
        User user = UserSession.authenticate("admin", "123456");
        assertEquals(user, UserSession.getCurrentUser());

        UserSession.setCurrentUser(null);
        assertNull(UserSession.getCurrentUser());
    }

    @Test
    public void testAvatarInitialCalculationSafety() {
        // Tránh lỗi StringIndexOutOfBoundsException khi có khoảng trắng ở cuối
        assertEquals("A", UITheme.getAvatarInitial("Trần Văn An "));
        assertEquals("K", UITheme.getAvatarInitial("Phan Vũ Xuân Kiên"));
        assertEquals("A", UITheme.getAvatarInitial("Admin"));
        assertEquals("U", UITheme.getAvatarInitial(""));
        assertEquals("U", UITheme.getAvatarInitial("   "));
        assertEquals("U", UITheme.getAvatarInitial(null));
        assertEquals("B", UITheme.getAvatarInitial("  Bob  "));
    }
}
