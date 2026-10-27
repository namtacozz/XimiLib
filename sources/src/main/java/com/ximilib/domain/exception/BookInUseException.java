package com.ximilib.domain.exception;

/**
 * BookInUseException: Ngoại lệ vi phạm quy tắc BR-09 khi xóa đầu sách đang có người mượn.
 */
public class BookInUseException extends BusinessRuleException {

    public BookInUseException(String bookTitle, int activeLoansCount) {
        super("BR_09_BOOK_IN_USE",
            "Không thể xóa đầu sách \"" + bookTitle + "\": Hiện vẫn còn " + activeLoansCount + 
            " cuốn sách đang được độc giả mượn (Quy tắc BR-09).");
    }
}
