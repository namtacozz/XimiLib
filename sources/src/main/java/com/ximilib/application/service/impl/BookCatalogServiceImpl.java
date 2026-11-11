package com.ximilib.application.service.impl;

import com.ximilib.application.dto.BookSearchCriteria;
import com.ximilib.application.dto.BookSummaryDto;
import com.ximilib.application.dto.PagedResult;
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

public class BookCatalogServiceImpl implements BookCatalogService {

    private final BookRepository bookRepository;

    public BookCatalogServiceImpl(BookRepository bookRepository) {
        this.bookRepository = Objects.requireNonNull(bookRepository, "bookRepository không được null");
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
    public void saveNewBook(String title, String author, String categoryName, String publisherName, String shelfLocation, String isbn, double price, int initialCopies) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Thông tin không hợp lệ: Tên sách không được để trống.");
        }
        if (initialCopies < 1) {
            throw new IllegalArgumentException("Thông tin không hợp lệ: Số lượng sách nhập kho ban đầu phải từ 1 cuốn trở lên (Quy tắc BR-12).");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Thông tin không hợp lệ: Giá bìa sách không thể âm.");
        }
        bookRepository.saveNewBook(title.trim(), author, categoryName, publisherName, shelfLocation, isbn, price, initialCopies);
        DomainEventPublisher.publish(new BookStockChangedEvent("NEW_BOOK", "CREATED"));
    }

    @Override
    public void saveNewBook(String title, String author, String categoryName, String isbn, double price, int initialCopies) {
        saveNewBook(title, author, categoryName, "NXB Bách Khoa", "Kệ IT-01 (Khu A)", isbn, price, initialCopies);
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
        return bookRepository.getDeletedBooks();
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
