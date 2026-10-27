package com.ximilib.application.service.impl;

import com.ximilib.application.dto.BookSearchCriteria;
import com.ximilib.application.dto.BookSummaryDto;
import com.ximilib.application.dto.PagedResult;
import com.ximilib.application.service.BookCatalogService;
import com.ximilib.domain.exception.BookInUseException;
import com.ximilib.domain.repository.BookRepository;
import com.ximilib.infrastructure.persistence.impl.BookRepositoryImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * BookCatalogServiceImpl: Triển khai thực tế nghiệp vụ quản lý kho sách từ MySQL CSDL.
 * - Tuân thủ R-01 (Presentation -> Application -> Domain <- Infrastructure).
 * - Tuân thủ BR-09 (kiểm tra sách đang mượn trước khi xóa mềm).
 * - Dữ liệu lấy 100% từ MySQL qua BookRepository, không lưu cứng dữ liệu sách trong Java.
 */
public class BookCatalogServiceImpl implements BookCatalogService {

    private final BookRepository bookRepository;

    public BookCatalogServiceImpl() {
        this(new BookRepositoryImpl(null));
    }

    public BookCatalogServiceImpl(BookRepository bookRepository) {
        if (bookRepository == null) {
            this.bookRepository = new BookRepositoryImpl(null);
        } else {
            this.bookRepository = bookRepository;
        }
    }

    @Override
    public PagedResult<BookSummaryDto> searchBooks(BookSearchCriteria criteria, int page, int pageSize) {
        return bookRepository.searchBooks(criteria, page, pageSize);
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
        return bookRepository.softDeleteBatch(eligibleIds);
    }

    @Override
    public List<BookSummaryDto> getDeletedBooks() {
        return bookRepository.getDeletedBooks();
    }

    @Override
    public void restoreBook(String bookId) {
        if (bookId != null && !bookId.trim().isEmpty()) {
            bookRepository.restoreBook(bookId.trim());
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
