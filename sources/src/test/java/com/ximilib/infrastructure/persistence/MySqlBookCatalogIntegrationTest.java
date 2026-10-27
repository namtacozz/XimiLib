package com.ximilib.infrastructure.persistence;

import com.ximilib.application.dto.BookSearchCriteria;
import com.ximilib.application.dto.BookSummaryDto;
import com.ximilib.application.dto.PagedResult;
import com.ximilib.application.service.impl.BookCatalogServiceImpl;
import com.ximilib.infrastructure.persistence.connection.DatabaseConnectionManager;
import com.ximilib.infrastructure.persistence.impl.BookRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Tích hợp CSDL MySQL Thật (BookRepositoryImpl & BookCatalogServiceImpl)")
class MySqlBookCatalogIntegrationTest {

    private DatabaseConnectionManager connectionManager;
    private BookRepositoryImpl repository;
    private BookCatalogServiceImpl service;

    @BeforeEach
    void setUp() {
        connectionManager = new DatabaseConnectionManager();
        repository = new BookRepositoryImpl(connectionManager);
        service = new BookCatalogServiceImpl(repository);
    }

    @Test
    @DisplayName("IT-01: CSDL MySQL thật chứa đúng 221 đầu sách đã nhập từ kho cũ")
    void testTotalBooksCountInMySql() {
        int total = repository.countTotalBooks() + repository.getDeletedBooks().size();
        assertEquals(221, total, "Tổng số đầu sách trong MySQL phải đúng chính xác 221");
    }

    @Test
    @DisplayName("IT-02: Lấy đầy đủ 11 thể loại thực tế từ bảng theloai trong MySQL")
    void testGetCategoriesFromMySql() {
        Map<Integer, String> categories = service.getCategories();
        assertNotNull(categories);
        assertEquals(11, categories.size(), "Bảng theloai phải có đúng 11 thể loại đã nhập");
        assertTrue(categories.containsKey(1));
        assertTrue(categories.containsValue("Lý luận chính trị & Triết học"));
        assertTrue(categories.containsValue("Công nghệ thông tin"));
    }

    @Test
    @DisplayName("IT-03: Tìm kiếm sách theo từ khóa từ MySQL và kiểm tra đường dẫn ảnh bìa")
    void testSearchBookByKeyword() {
        BookSearchCriteria criteria = new BookSearchCriteria("Sapiens", null, BookSearchCriteria.AvailabilityFilter.ALL);
        PagedResult<BookSummaryDto> result = service.searchBooks(criteria, 1, 10);

        assertNotNull(result);
        assertFalse(result.getItems().isEmpty(), "Phải tìm thấy sách Sapiens trong MySQL");
        BookSummaryDto book = result.getItems().get(0);
        assertTrue(book.getTitle().contains("Sapiens"));
        assertEquals("Yuval Noah Harari", book.getAuthor());
        assertEquals("/assets/covers/sapiens.jpg", book.getCoverImage());
    }

    @Test
    @DisplayName("IT-04: Phân trang từ MySQL đúng cấu trúc (10 cuốn / trang)")
    void testPaginationFromMySql() {
        PagedResult<BookSummaryDto> page1 = service.searchBooks(null, 1, 10);
        assertEquals(10, page1.getItems().size());
        assertTrue(page1.getTotalElements() >= 220, "Số lượng sách khả dụng phải >= 220");
        assertTrue(page1.getTotalPages() >= 22, "Tổng số trang phải >= 22");

        PagedResult<BookSummaryDto> page2 = service.searchBooks(null, 2, 10);
        assertEquals(10, page2.getItems().size());
        assertNotEquals(page1.getItems().get(0).getBookId(), page2.getItems().get(0).getBookId());
    }

    @Test
    @DisplayName("IT-05: Chứng minh không fallback dữ liệu cứng - Ném lỗi khi CSDL bị ngắt")
    void testFailsGracefullyWithoutHardcodedFallbackWhenDbUnavailable() {
        DatabaseConnectionManager brokenManager = new DatabaseConnectionManager(
            "jdbc:mysql://localhost:9999/non_existent_db?connectTimeout=1000",
            "wrong_user",
            "wrong_pass"
        );
        BookRepositoryImpl brokenRepo = new BookRepositoryImpl(brokenManager);
        BookCatalogServiceImpl brokenService = new BookCatalogServiceImpl(brokenRepo);

        assertThrows(RuntimeException.class, () -> {
            brokenService.searchBooks(null, 1, 10);
        }, "Khi CSDL MySQL không khả dụng, hệ thống phải ném lỗi RuntimeException/SQLException, tuyệt đối cấm fallback về dữ liệu mẫu");
    }

    @Test
    @DisplayName("IT-06: Xác thực tài khoản người dùng thật từ bảng nguoidung (không bị lỗi cột Sodienthoai)")
    void testFindUserByIdentifierInMySql() {
        com.ximilib.infrastructure.persistence.impl.UserRepositoryImpl userRepo = 
            new com.ximilib.infrastructure.persistence.impl.UserRepositoryImpl(connectionManager);
        var userOpt = userRepo.findByIdentifier("kien.phan");
        assertTrue(userOpt.isPresent(), "Phải tìm thấy tài khoản kien.phan trong MySQL");
        assertEquals("Phan Vũ Xuân Kiên", userOpt.get().getFullName());
        assertEquals("0905123456", userOpt.get().getPhone());
    }
}
