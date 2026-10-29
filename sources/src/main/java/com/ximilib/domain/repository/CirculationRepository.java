package com.ximilib.domain.repository;

import com.ximilib.domain.model.LoanType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * CirculationRepository: Hợp đồng lưu trữ và truy vấn nghiệp vụ Lưu hành, Mượn/Trả, Bồi hoàn.
 * Tuân thủ Clean Architecture (Domain interface, không import SQL/JDBC/Swing - R-01).
 */
public interface CirculationRepository {

    record UserCardInfo(String userId, String fullName, String cardNumber, boolean cardActive, boolean cardExpired) {}
    record BookCopyInfo(String copyId, String bookId, String bookTitle, BigDecimal coverPrice, String status, String shelfLocation) {}
    record BorrowDetailInfo(String detailId, String ticketId, String copyId, String bookTitle, String userId,
                            LocalDateTime dueDate, BigDecimal coverPrice) {}

    Optional<UserCardInfo> findCardByIdentifier(String identifier);

    Optional<BookCopyInfo> findCopyByBarcode(String barcode);

    List<BookCopyInfo> findAvailableCopies(String bookId);

    int countActiveHomeLoans(String userId);

    boolean hasUnpaidFines(String userId);

    void createBorrowTicket(String ticketId, String userId, String cardId, LoanType loanType,
                            LocalDateTime borrowDate, LocalDateTime dueDate, List<String> copyIds, String notes);

    Optional<BorrowDetailInfo> findActiveLoanByCopy(String copyBarcode);

    void returnCopy(String detailId, String ticketId, String copyId, LocalDateTime returnDate, String condition, BigDecimal fine);

    void closeTicketIfAllReturned(String ticketId);

    void createFine(String fineId, String userId, String detailId, String reason, BigDecimal amount, String notes);

    void updateTicketStatus(String ticketId, int status);

    void markCopyLost(String copyId);

    void payFineAndCloseLoss(String fineId, String paymentMethod);

    boolean isTicketRenewed(String ticketId);

    int countReservations(String bookId);

    void extendDueDate(String ticketId, LocalDateTime newDueDate, String reason);

    void createReservation(String resId, String userId, String bookId);

    String generateNextTicketId();

    String generateNextFineId();
}
