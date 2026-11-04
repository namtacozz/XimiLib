package com.ximilib.domain.exception;

public class BookInUseException extends BusinessRuleException {

    public BookInUseException(String bookTitle, int activeLoansCount) {
        super("BR_09_BOOK_IN_USE",
            "Không thể xóa đầu sách đang có độc giả mượn (Sách \"" + bookTitle + "\": Hiện vẫn còn " + activeLoansCount +
            " cuốn sách đang được độc giả mượn. Vui lòng thu hồi trước khi xóa).");
    }
}
