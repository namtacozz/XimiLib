package com.ximilib.application.service;

import com.ximilib.application.dto.BorrowTicketDto;
import com.ximilib.application.dto.CompensationResultDto;
import com.ximilib.application.dto.ReturnResultDto;
import com.ximilib.domain.model.LoanType;
import com.ximilib.domain.repository.CirculationRepository;
import java.util.List;

/**
 * CirculationService: Hợp đồng nghiệp vụ quản lý Lưu hành sách tại quầy, Trả sách, Báo mất và Gia hạn.
 * Tuân thủ Clean Architecture, BR-02..BR-08 và SRS UC-5, UC-6, UC-9.
 */
public interface CirculationService {

    BorrowTicketDto createLoan(String readerIdentifier, List<String> copyBarcodes, LoanType loanType, String notes);

    ReturnResultDto returnBook(String copyBarcode, String returnCondition);

    CompensationResultDto reportLoss(String copyBarcode);

    void payFine(String fineTicketId, String paymentMethod);

    void renewLoan(String borrowTicketId, String reason);

    void reserveBook(String userId, String bookId);

    List<BorrowTicketDto> getReaderLoans(String userId);

    List<BorrowTicketDto> getAllLoans(String keyword, Integer status);

    CirculationRepository.UserCardInfo getCardInfo(String identifier);

    CirculationRepository.BookCopyInfo getCopyInfo(String barcode);

    List<CirculationRepository.BookCopyInfo> getAvailableCopies(String bookId);

    CirculationRepository.BorrowDetailInfo findActiveLoanByCopy(String copyBarcode);
}
