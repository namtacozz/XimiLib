package com.ximilib.domain.repository;

import java.util.List;
import java.util.Map;

/**
 * BookRepository: Hợp đồng lưu trữ và truy vấn dữ liệu đầu sách từ CSDL MySQL (sach, theloai, tacgia, nhaxuatban).
 * Tuân thủ Clean Architecture (Domain interface, không import DTO/SQL/JDBC/Swing - R-01).
 */
public interface BookRepository {

    Map<Integer, String> getCategories();

    int countActiveLoans(String bookId);

    void softDelete(String bookId);

    int softDeleteBatch(List<String> bookIds);

    default int softDeleteBatch(TransactionContext tx, List<String> bookIds) {
        return softDeleteBatch(bookIds);
    }

    int countTotalBooks();

    default void saveNewBook(String title, String author, String categoryName, String publisherName, String shelfLocation, String isbn, double price, int initialCopies) {}

    default void saveNewBook(String title, String author, String categoryName, String isbn, double price, int initialCopies) {
        saveNewBook(title, author, categoryName, "NXB Bách Khoa", "Kệ IT-01 (Khu A)", isbn, price, initialCopies);
    }

    void restoreBook(String bookId);

    void addCategory(String name, String desc);

    void updateCategory(int id, String name, String desc);

    void deleteCategory(int id);
}
