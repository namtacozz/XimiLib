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

    /**
     * Tính toán trước nghĩa vụ tài chính (tiền phạt trễ hạn, bồi thường mất sách hoặc phí kỹ thuật)
     * cho cuốn sách đang mượn theo quy định BR-06 và BR-08.
     *
     * @param copyBarcode Mã vạch cuốn sách
     * @param isLost      Cờ đánh dấu báo mất sách
     * @param isDamaged   Cờ đánh dấu sách hỏng nhẹ / rách bìa
     * @return Kết quả tính toán nghĩa vụ tài chính (CompensationResultDto)
     */
    CompensationResultDto calculateCompensation(String copyBarcode, boolean isLost, boolean isDamaged);

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
