package com.ximilib.application.service;

import com.ximilib.application.dto.BookSearchCriteria;
import com.ximilib.application.dto.BookSummaryDto;
import com.ximilib.application.dto.PagedResult;
import com.ximilib.domain.exception.BookInUseException;
import java.util.List;
import java.util.Map;

public interface BookCatalogService {

    PagedResult<BookSummaryDto> searchBooks(BookSearchCriteria criteria, int page, int pageSize);

    Map<Integer, String> getCategories();

    void saveNewBook(String title, String author, String categoryName, String publisherName, String shelfLocation, String isbn, double price, int initialCopies);

    void saveNewBook(String title, String author, String categoryName, String isbn, double price, int initialCopies);

    void deleteBook(String bookId) throws BookInUseException;

    int deleteBooksBatch(List<String> bookIds);

    List<BookSummaryDto> getDeletedBooks();

    void restoreBook(String bookId);

    void addCategory(String name, String desc);

    void updateCategory(int id, String name, String desc);

    void deleteCategory(int id);
}
