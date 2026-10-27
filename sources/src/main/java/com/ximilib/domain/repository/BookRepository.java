package com.ximilib.domain.repository;

import com.ximilib.application.dto.BookSearchCriteria;
import com.ximilib.application.dto.BookSummaryDto;
import com.ximilib.application.dto.PagedResult;
import java.util.List;
import java.util.Map;

/**
 * BookRepository: Hợp đồng lưu trữ và truy vấn dữ liệu đầu sách từ CSDL MySQL (sach, theloai, tacgia, nhaxuatban).
 * Tuân thủ Clean Architecture (Domain interface, không import SQL/JDBC/Swing - R-01).
 */
public interface BookRepository {

    PagedResult<BookSummaryDto> searchBooks(BookSearchCriteria criteria, int page, int pageSize);

    Map<Integer, String> getCategories();

    int countActiveLoans(String bookId);

    void softDelete(String bookId);

    int softDeleteBatch(List<String> bookIds);

    int countTotalBooks();

    List<BookSummaryDto> getDeletedBooks();

    void restoreBook(String bookId);

    void addCategory(String name, String desc);

    void updateCategory(int id, String name, String desc);

    void deleteCategory(int id);
}
