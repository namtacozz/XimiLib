package com.ximilib.application.service;

import com.ximilib.application.dto.BookSearchCriteria;
import com.ximilib.application.dto.BookSummaryDto;
import com.ximilib.application.dto.PagedResult;
import com.ximilib.application.service.impl.BookCatalogServiceImpl;
import com.ximilib.domain.exception.BookInUseException;
import com.ximilib.domain.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Nghiệp vụ Kho Sách (BookCatalogService - F-01 & BR-09)")
class BookCatalogServiceTest {

    private BookCatalogServiceImpl service;
    private StubBookRepository repository;

    @BeforeEach
    void setUp() {
        repository = new StubBookRepository();
        service = new BookCatalogServiceImpl(repository);
    }

    @Test
    @DisplayName("TC-01: Tìm kiếm sách theo từ khóa tên hoặc tác giả")
    void testSearchByKeyword() {
        BookSearchCriteria criteria = new BookSearchCriteria("Atomic Habits", null, BookSearchCriteria.AvailabilityFilter.ALL);
        PagedResult<BookSummaryDto> result = service.searchBooks(criteria, 1, 10);

        assertNotNull(result);
        assertFalse(result.getItems().isEmpty());
        assertTrue(result.getItems().get(0).getTitle().contains("Atomic Habits"));
    }

    @Test
    @DisplayName("TC-02: Lọc sách theo Thể loại (Category)")
    void testFilterByCategory() {
        // Category 4 = Công nghệ thông tin
        BookSearchCriteria criteria = new BookSearchCriteria("", 4, BookSearchCriteria.AvailabilityFilter.ALL);
        PagedResult<BookSummaryDto> result = service.searchBooks(criteria, 1, 10);

        assertNotNull(result);
        for (BookSummaryDto book : result.getItems()) {
            assertEquals(4, book.getCategoryId());
        }
    }

    @Test
    @DisplayName("TC-03: Lọc sách theo Tình trạng (Còn sách / Hết sách)")
    void testFilterByAvailability() {
        // Chỉ lấy sách còn bản sao khả dụng
        BookSearchCriteria criteriaAvailable = new BookSearchCriteria("", null, BookSearchCriteria.AvailabilityFilter.AVAILABLE_ONLY);
        PagedResult<BookSummaryDto> availResult = service.searchBooks(criteriaAvailable, 1, 10);

        assertNotNull(availResult);
        for (BookSummaryDto b : availResult.getItems()) {
            assertTrue(b.getAvailableCopies() > 0);
        }

        // Chỉ lấy sách đã hết bản sao khả dụng
        BookSearchCriteria criteriaOutOfStock = new BookSearchCriteria("", null, BookSearchCriteria.AvailabilityFilter.OUT_OF_STOCK);
        PagedResult<BookSummaryDto> outResult = service.searchBooks(criteriaOutOfStock, 1, 10);

        assertNotNull(outResult);
        for (BookSummaryDto b : outResult.getItems()) {
            assertEquals(0, b.getAvailableCopies());
        }
    }

    @Test
    @DisplayName("TC-04: Phân trang phía Server (LIMIT / OFFSET)")
    void testPagination() {
        PagedResult<BookSummaryDto> page1 = service.searchBooks(null, 1, 5);
        assertEquals(5, page1.getItems().size());
        assertEquals(1, page1.getPage());
        assertEquals(5, page1.getPageSize());
        assertTrue(page1.getTotalPages() >= 2);

        PagedResult<BookSummaryDto> page2 = service.searchBooks(null, 2, 5);
        assertEquals(5, page2.getItems().size());
        assertEquals(2, page2.getPage());

        // Kiểm tra hai trang không trùng lặp phần tử đầu tiên
        assertNotEquals(page1.getItems().get(0).getBookId(), page2.getItems().get(0).getBookId());
    }

    @Test
    @DisplayName("TC-05: Tuân thủ BR-09 - Chặn xóa sách đang có người mượn")
    void testSoftDeleteFailsWhenBookHasActiveLoans() {
        // MS-0011 có activeLoansCount = 2 > 0
        BookInUseException ex = assertThrows(BookInUseException.class, () -> {
            service.deleteBook("MS-0011");
        });

        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("BR-09") || ex.getMessage().contains("mượn"));
    }

    @Test
    @DisplayName("TC-06: Xóa mềm thành công khi sách không có người mượn")
    void testSoftDeleteSucceedsWhenNoActiveLoans() {
        // MS-0013 có activeLoansCount = 0
        assertDoesNotThrow(() -> {
            service.deleteBook("MS-0013");
        });

        // Kiểm tra sách đã bị đánh dấu xóa mềm
        BookSearchCriteria criteria = new BookSearchCriteria("Advanced Discrete Mathematics", null, BookSearchCriteria.AvailabilityFilter.ALL);
        PagedResult<BookSummaryDto> result = service.searchBooks(criteria, 1, 10);
        assertTrue(result.getItems().isEmpty(), "Sách đã xóa mềm không được xuất hiện trong kết quả tìm kiếm");
    }

    @Test
    @DisplayName("TC-07: Xóa hàng loạt sách - tự động bỏ qua sách đang mượn theo BR-09")
    void testBatchDeleteSkipsBooksInUse() {
        // MS-0011 (đang mượn: 2), MS-0013 (không mượn: 0), MS-0019 (không mượn: 0)
        List<String> ids = List.of("MS-0011", "MS-0013", "MS-0019");
        int deletedCount = service.deleteBooksBatch(ids);

        // Chỉ xóa được 2 cuốn không có người mượn
        assertEquals(2, deletedCount);

        // MS-0011 vẫn phải còn tồn tại
        BookSearchCriteria criteria = new BookSearchCriteria("Literature", null, BookSearchCriteria.AvailabilityFilter.ALL);
        PagedResult<BookSummaryDto> result = service.searchBooks(criteria, 1, 10);
        assertFalse(result.getItems().isEmpty());
    }

    @Test
    @DisplayName("TC-08: Lấy danh mục thể loại phục vụ bộ lọc")
    void testGetCategories() {
        Map<Integer, String> categories = service.getCategories();
        assertNotNull(categories);
        assertTrue(categories.size() >= 4);
        assertEquals("Công nghệ thông tin", categories.get(4));
    }

    /**
     * StubBookRepository: Lớp giả lập cho BookRepository dùng riêng trong Unit Test
     */
    private static class StubBookRepository implements BookRepository {
        private final List<BookSummaryDto> books = new ArrayList<>();
        private final Map<Integer, String> categories = new LinkedHashMap<>();

        public StubBookRepository() {
            categories.put(1, "Kỹ năng sống");
            categories.put(2, "Tâm lý học");
            categories.put(3, "Văn học cổ điển");
            categories.put(4, "Công nghệ thông tin");
            categories.put(5, "Toán học & Giải tích");

            books.add(new BookSummaryDto("MS-0011", "Literature: Tuyển tập Văn học thế giới", "Wade Warren", 3, "Văn học cổ điển", "NXB Văn Học", 6, 8, Color.BLUE, false, 2));
            books.add(new BookSummaryDto("MS-0021", "Mathematics: Đại số tuyến tính", "David Morgan", 5, "Toán học & Giải tích", "NXB Bách Khoa", 0, 5, Color.BLACK, false, 5));
            books.add(new BookSummaryDto("MS-0013", "Advanced Discrete Mathematics", "Savannah Nguyen", 5, "Toán học & Giải tích", "NXB Khoa Học", 7, 7, Color.ORANGE, false, 0));
            books.add(new BookSummaryDto("MS-0019", "Clean Architecture", "Robert C. Martin", 4, "Công nghệ thông tin", "NXB TT&TT", 5, 5, Color.GREEN, false, 0));
            books.add(new BookSummaryDto("MS-0025", "Atomic Habits: Thay đổi tí hon", "James Clear", 1, "Kỹ năng sống", "NXB Thế Giới", 12, 15, Color.YELLOW, false, 3));
            books.add(new BookSummaryDto("MS-0026", "Thinking, Fast and Slow", "Daniel Kahneman", 2, "Tâm lý học", "NXB Trẻ", 3, 8, Color.CYAN, false, 5));
            books.add(new BookSummaryDto("MS-0027", "The Great Gatsby", "F. Scott Fitzgerald", 3, "Văn học cổ điển", "NXB Hội Nhà Văn", 0, 4, Color.RED, false, 4));
            books.add(new BookSummaryDto("MS-0028", "Refactoring", "Martin Fowler", 4, "Công nghệ thông tin", "NXB TT&TT", 4, 4, Color.GRAY, false, 0));
            books.add(new BookSummaryDto("MS-0029", "Design Patterns", "Erich Gamma", 4, "Công nghệ thông tin", "NXB TT&TT", 2, 5, Color.MAGENTA, false, 0));
            books.add(new BookSummaryDto("MS-0030", "Deep Work", "Cal Newport", 1, "Kỹ năng sống", "NXB Trẻ", 6, 6, Color.BLUE, false, 0));
        }

        @Override
        public PagedResult<BookSummaryDto> searchBooks(BookSearchCriteria criteria, int page, int pageSize) {
            String kw = (criteria != null && criteria.getKeyword() != null) ? criteria.getKeyword().toLowerCase().trim() : "";
            Integer catId = (criteria != null) ? criteria.getCategoryId() : null;
            BookSearchCriteria.AvailabilityFilter avail = (criteria != null && criteria.getAvailability() != null)
                    ? criteria.getAvailability() : BookSearchCriteria.AvailabilityFilter.ALL;

            List<BookSummaryDto> filtered = books.stream()
                .filter(b -> !b.isDeleted())
                .filter(b -> {
                    if (kw.isEmpty()) return true;
                    return b.getTitle().toLowerCase().contains(kw) || b.getAuthor().toLowerCase().contains(kw);
                })
                .filter(b -> {
                    if (catId == null || catId <= 0) return true;
                    return b.getCategoryId() != null && b.getCategoryId().equals(catId);
                })
                .filter(b -> {
                    if (avail == BookSearchCriteria.AvailabilityFilter.AVAILABLE_ONLY) return b.isAvailable();
                    if (avail == BookSearchCriteria.AvailabilityFilter.OUT_OF_STOCK) return !b.isAvailable();
                    return true;
                })
                .collect(Collectors.toList());

            int total = filtered.size();
            int fromIdx = Math.min((page - 1) * pageSize, total);
            int toIdx = Math.min(fromIdx + pageSize, total);
            List<BookSummaryDto> pageItems = filtered.subList(fromIdx, toIdx);
            return new PagedResult<>(pageItems, total, page, pageSize);
        }

        @Override
        public Map<Integer, String> getCategories() {
            return new LinkedHashMap<>(categories);
        }

        @Override
        public int countActiveLoans(String bookId) {
            for (BookSummaryDto b : books) {
                if (b.getBookId().equalsIgnoreCase(bookId)) {
                    return b.getActiveLoansCount();
                }
            }
            return 0;
        }

        @Override
        public void softDelete(String bookId) {
            for (BookSummaryDto b : books) {
                if (b.getBookId().equalsIgnoreCase(bookId)) {
                    b.setDeleted(true);
                    break;
                }
            }
        }

        @Override
        public int softDeleteBatch(List<String> bookIds) {
            int count = 0;
            for (String id : bookIds) {
                softDelete(id);
                count++;
            }
            return count;
        }

        @Override
        public int countTotalBooks() {
            return (int) books.stream().filter(b -> !b.isDeleted()).count();
        }

        @Override
        public List<BookSummaryDto> getDeletedBooks() {
            return books.stream().filter(BookSummaryDto::isDeleted).collect(Collectors.toList());
        }

        @Override
        public void restoreBook(String bookId) {
            for (BookSummaryDto b : books) {
                if (b.getBookId().equalsIgnoreCase(bookId)) {
                    b.setDeleted(false);
                    break;
                }
            }
        }

        @Override
        public void addCategory(String name, String desc) {
            categories.put(categories.size() + 1, name);
        }

        @Override
        public void updateCategory(int id, String name, String desc) {
            categories.put(id, name);
        }

        @Override
        public void deleteCategory(int id) {
            categories.remove(id);
        }
    }
}
