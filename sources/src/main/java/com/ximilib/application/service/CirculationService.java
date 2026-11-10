package com.ximilib.application.service;

import com.ximilib.application.dto.BorrowTicketDto;
import com.ximilib.application.dto.CompensationResultDto;
import com.ximilib.application.dto.ReturnResultDto;
import com.ximilib.domain.model.LoanType;
import com.ximilib.domain.repository.CirculationRepository;
import java.util.List;

public interface CirculationService {

    BorrowTicketDto createLoan(String readerIdentifier, List<String> copyBarcodes, LoanType loanType, String notes);

    ReturnResultDto returnBook(String copyBarcode, String returnCondition);

    CompensationResultDto reportLoss(String copyBarcode);

    CompensationResultDto calculateCompensation(String copyBarcode, boolean isLost, boolean isDamaged);

    void payFine(String fineTicketId, String paymentMethod);

    String findUnpaidFineByTicketId(String ticketId);

    void renewLoan(String borrowTicketId, String reason);

    void reserveBook(String userId, String bookId);

    BorrowTicketDto deliverPendingLoan(String ticketId);

    List<BorrowTicketDto> getReaderLoans(String userId);

    List<BorrowTicketDto> getAllLoans(String keyword, Integer status);

    CirculationRepository.UserCardInfo getCardInfo(String identifier);

    CirculationRepository.BookCopyInfo getCopyInfo(String barcode);

    List<CirculationRepository.BookCopyInfo> getAvailableCopies(String bookId);

    CirculationRepository.BorrowDetailInfo findActiveLoanByCopy(String copyBarcode);
}
