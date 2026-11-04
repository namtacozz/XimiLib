package com.ximilib.presentation.component;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Thành phần Giao diện PR 2: PasswordPolicyChecklist")
class PasswordPolicyChecklistTest {

    @Test
    @DisplayName("TC-PC01: Checklist tự ẩn khi mật khẩu rỗng và hiện khi có ký tự")
    void testVisibilityToggle() {
        PasswordPolicyChecklist checklist = new PasswordPolicyChecklist();
        assertFalse(checklist.isVisible(), "Mặc định checklist phải ẩn");

        checklist.updatePassword("A");
        assertTrue(checklist.isVisible(), "Khi gõ ký tự, checklist phải hiện");

        checklist.updatePassword("");
        assertFalse(checklist.isVisible(), "Khi xóa sạch, checklist phải tự ẩn");
    }

    @Test
    @DisplayName("TC-PC02: Kiểm tra các tiêu chí độ mạnh mật khẩu theo thời gian thực")
    void testPasswordStrengthValidation() {
        PasswordPolicyChecklist checklist = new PasswordPolicyChecklist();

        checklist.updatePassword("abc");
        assertFalse(checklist.isAllSatisfied());

        checklist.updatePassword("abcdefgh");
        assertFalse(checklist.isAllSatisfied());

        checklist.updatePassword("Abcdefgh");
        assertFalse(checklist.isAllSatisfied());

        checklist.updatePassword("Abcdefg1");
        assertFalse(checklist.isAllSatisfied());

        checklist.updatePassword("Admin@123");
        assertTrue(checklist.isAllSatisfied(), "Mật khẩu Admin@123 phải thỏa mãn toàn bộ tiêu chí");

        checklist.updatePassword("Kien#2026");
        assertTrue(checklist.isAllSatisfied(), "Mật khẩu Kien#2026 phải thỏa mãn toàn bộ tiêu chí");
    }
}
