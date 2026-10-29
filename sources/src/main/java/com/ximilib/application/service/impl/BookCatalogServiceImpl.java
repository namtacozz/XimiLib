package com.ximilib.application.service.impl;

import com.ximilib.application.dto.BookSearchCriteria;
import com.ximilib.application.dto.BookSummaryDto;
import com.ximilib.application.dto.PagedResult;
import com.ximilib.application.port.BookQueryPort;
import com.ximilib.application.service.BookCatalogService;
import com.ximilib.domain.event.BookStockChangedEvent;
import com.ximilib.domain.event.DomainEventPublisher;
import com.ximilib.domain.exception.BookInUseException;
import com.ximilib.domain.repository.BookRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * BookCatalogServiceImpl: Triển khai thực tế nghiệp vụ quản lý kho sách từ MySQL CSDL.
 * - Tuân thủ R-01 (Presentation -> Application -> Domain <- Infrastructure).
 * - Tuân thủ BR-09 (kiểm tra sách đang mượn trước khi xóa mềm).
 * - Dữ liệu lấy 100% từ MySQL qua BookRepository, không lưu cứng dữ liệu sách trong Java.
 */
public class BookCatalogServiceImpl implements BookCatalogService {

    private final BookRepository bookRepository;
    private final BookQueryPort bookQueryPort;

    public BookCatalogServiceImpl(BookRepository bookRepository) {
        this(bookRepository, (bookRepository instanceof BookQueryPort bqp) ? bqp : null);
    }

    public BookCatalogServiceImpl(BookRepository bookRepository, BookQueryPort bookQueryPort) {
        this.bookRepository = Objects.requireNonNull(bookRepository, "bookRepository không được null");
        this.bookQueryPort = (bookQueryPort != null) ? bookQueryPort : ((bookRepository instanceof BookQueryPort bqp) ? bqp : null);
    }

    @Override
    public PagedResult<BookSummaryDto> searchBooks(BookSearchCriteria criteria, int page, int pageSize) {
        if (bookQueryPort != null) {
            return bookQueryPort.searchBooks(criteria, page, pageSize);
        }
        return new PagedResult<>(Collections.emptyList(), 0, page, pageSize);
    }

    @Override
    public Map<Integer, String> getCategories() {
        return bookRepository.getCategories();
    }

    @Override
    public void deleteBook(String bookId) throws BookInUseException {
        if (bookId == null || bookId.trim().isEmpty()) return;
        int activeLoans = bookRepository.countActiveLoans(bookId);
        if (activeLoans > 0) {
            throw new BookInUseException(bookId, activeLoans);
        }
        bookRepository.softDelete(bookId);
        DomainEventPublisher.publish(new BookStockChangedEvent(bookId, "DELETED"));
    }

    @Override
    public int deleteBooksBatch(List<String> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) return 0;
        List<String> eligibleIds = new ArrayList<>();
        for (String id : bookIds) {
            if (bookRepository.countActiveLoans(id) == 0) {
                eligibleIds.add(id);
            }
        }
        int count = bookRepository.softDeleteBatch(eligibleIds);
        if (count > 0) {
            DomainEventPublisher.publish(new BookStockChangedEvent("BATCH", "DELETED"));
        }
        return count;
    }

    @Override
    public List<BookSummaryDto> getDeletedBooks() {
        if (bookQueryPort != null) {
            return bookQueryPort.getDeletedBooks();
        }
        return Collections.emptyList();
    }

    @Override
    public void restoreBook(String bookId) {
        if (bookId != null && !bookId.trim().isEmpty()) {
            bookRepository.restoreBook(bookId.trim());
            DomainEventPublisher.publish(new BookStockChangedEvent(bookId.trim(), "RESTORED"));
        }
    }

    @Override
    public void addCategory(String name, String desc) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên thể loại không được để trống.");
        }
        bookRepository.addCategory(name.trim(), desc);
    }

    @Override
    public void updateCategory(int id, String name, String desc) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên thể loại không được để trống.");
        }
        bookRepository.updateCategory(id, name.trim(), desc);
    }

    @Override
    public void deleteCategory(int id) {
        bookRepository.deleteCategory(id);
    }
}
