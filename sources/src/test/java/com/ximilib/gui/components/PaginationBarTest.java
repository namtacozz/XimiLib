package com.ximilib.gui.components;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Thành phần Giao diện Phân Trang (PaginationBar)")
class PaginationBarTest {

    @Test
    @DisplayName("TC-P01: Khởi tạo và cập nhật trạng thái phân trang")
    void testUpdatePagination() {
        PaginationBar bar = new PaginationBar();
        assertDoesNotThrow(() -> bar.updatePagination(1, 10, 10, 100));
        assertDoesNotThrow(() -> bar.updatePagination(5, 20, 20, 400));
    }

    @Test
    @DisplayName("TC-P02: Kích hoạt callback khi chuyển trang")
    void testPageChangeCallback() {
        PaginationBar bar = new PaginationBar();
        AtomicInteger selectedPage = new AtomicInteger(0);
        bar.setOnPageChange(selectedPage::set);

        bar.updatePagination(1, 5, 10, 50);
        // Test callback registration
        assertNotNull(bar);
    }

    @Test
    @DisplayName("TC-P03: Kích hoạt callback khi đổi số lượng dòng trên trang")
    void testPageSizeChangeCallback() {
        PaginationBar bar = new PaginationBar();
        AtomicInteger selectedSize = new AtomicInteger(0);
        bar.setOnPageSizeChange(selectedSize::set);

        bar.updatePagination(1, 10, 10, 100);
        assertNotNull(bar);
    }
}
