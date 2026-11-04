package com.ximilib.domain.repository;

import com.ximilib.domain.model.LoanType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CirculationRepository {

    record UserCardInfo(String userId, String fullName, String cardNumber, boolean cardActive, boolean cardExpired) {}
    record BookCopyInfo(String copyId, String bookId, String bookTitle, BigDecimal coverPrice, String status, String shelfLocation) {}
    record BorrowDetailInfo(String detailId, String ticketId, String copyId, String bookTitle, String userId,
                            LocalDateTime dueDate, BigDecimal coverPrice) {}

    Optional<UserCardInfo> findCardByIdentifier(String identifier);

    Optional<BookCopyInfo> findCopyByBarcode(String barcode);

    List<BookCopyInfo> findAvailableCopies(String bookId);

    int countActiveHomeLoans(String userId);

    default boolean hasOverdueLoans(String userId) {
        return false;
    }

    boolean hasUnpaidFines(String userId);

    void createBorrowTicket(String ticketId, String userId, String cardId, LoanType loanType,
                            LocalDateTime borrowDate, LocalDateTime dueDate, List<String> copyIds, String notes);

    default void createBorrowTicket(TransactionContext tx, String ticketId, String userId, String cardId, LoanType loanType,
                            LocalDateTime borrowDate, LocalDateTime dueDate, List<String> copyIds, String notes) {
        createBorrowTicket(ticketId, userId, cardId, loanType, borrowDate, dueDate, copyIds, notes);
    }

    Optional<BorrowDetailInfo> findActiveLoanByCopy(String copyBarcode);

    void returnCopy(String detailId, String ticketId, String copyId, LocalDateTime returnDate, String condition, BigDecimal fine);

    default void returnCopy(TransactionContext tx, String detailId, String ticketId, String copyId, LocalDateTime returnDate, String condition, BigDecimal fine) {
        returnCopy(detailId, ticketId, copyId, returnDate, condition, fine);
    }

    void closeTicketIfAllReturned(String ticketId);

    default void closeTicketIfAllReturned(TransactionContext tx, String ticketId) {
        closeTicketIfAllReturned(ticketId);
    }

    void createFine(String fineId, String userId, String detailId, String reason, BigDecimal amount, String notes);

    default void createFine(TransactionContext tx, String fineId, String userId, String detailId, String reason, BigDecimal amount, String notes) {
        createFine(fineId, userId, detailId, reason, amount, notes);
    }

    void updateTicketStatus(String ticketId, int status);

    void markCopyLost(String copyId);

    void payFineAndCloseLoss(String fineId, String paymentMethod);

    default void payFineAndCloseLoss(TransactionContext tx, String fineId, String paymentMethod) {
        payFineAndCloseLoss(fineId, paymentMethod);
    }

    boolean isTicketRenewed(String ticketId);

    int countReservations(String bookId);

    void extendDueDate(String ticketId, LocalDateTime newDueDate, String reason);

    default void extendDueDate(TransactionContext tx, String ticketId, LocalDateTime newDueDate, String reason) {
        extendDueDate(ticketId, newDueDate, reason);
    }

    void createReservation(String resId, String userId, String bookId);

    String generateNextTicketId();

    default Optional<String> findUnpaidFineByDetailId(String detailId) {
        return Optional.empty();
    }

    default Optional<String> findUnpaidFineByTicketId(String ticketId) {
        return Optional.empty();
    }

    String generateNextFineId();
}
