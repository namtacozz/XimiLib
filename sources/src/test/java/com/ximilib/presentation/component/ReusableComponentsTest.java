package com.ximilib.presentation.component;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReusableComponentsTest {

    @Test
    @DisplayName("Khởi tạo và kiểm tra trạng thái các component giao diện cơ bản")
    public void testComponentInitialization() {
        PrimaryButton btnPrimary = new PrimaryButton("Lưu sách");
        assertEquals("Lưu sách", btnPrimary.getText());

        SecondaryButton btnSecondary = new SecondaryButton("Hủy");
        assertEquals("Hủy", btnSecondary.getText());

        TextInputField txtInput = new TextInputField("Nhập họ và tên", 20);
        txtInput.SetError(true);
        txtInput.SetError(false);
        assertNotNull(txtInput);

        PasswordInputField pwdInput = new PasswordInputField("Nhập mật khẩu", 20);
        pwdInput.SetError(true);
        pwdInput.SetError(false);
        assertNotNull(pwdInput);

        StatusBadge badge = new StatusBadge("Còn sách", StatusBadge.BadgeType.SUCCESS);
        assertEquals("Còn sách", badge.getText());

        SectionCard card = new SectionCard(20);
        assertNotNull(card);

        PageHeader header = new PageHeader("Kho Sách", "Tra cứu và quản lý đầu sách");
        header.AddAction(btnPrimary);
        assertNotNull(header);

        FormErrorLabel lblError = new FormErrorLabel();
        assertFalse(lblError.isVisible());
        lblError.SetErrorMessage("Trường này không được để trống");
        assertTrue(lblError.isVisible());
        assertEquals("Trường này không được để trống", lblError.getText());
        lblError.ClearError();
        assertFalse(lblError.isVisible());
    }
}
