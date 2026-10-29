package com.ximilib.application.service.impl;

import com.ximilib.application.dto.BorrowTicketDto;
import com.ximilib.application.dto.CompensationResultDto;
import com.ximilib.application.dto.ReturnResultDto;
import com.ximilib.application.service.CirculationService;
import com.ximilib.domain.event.BookStockChangedEvent;
import com.ximilib.domain.event.DomainEventPublisher;
import com.ximilib.domain.model.LibraryPolicy;
import com.ximilib.domain.model.LoanTicketStatus;
import com.ximilib.domain.model.LoanType;
import com.ximilib.domain.repository.CirculationRepository;
import com.ximilib.domain.repository.CirculationRepository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * CirculationServiceImpl: Triển khai các quy tắc nghiệp vụ Lưu hành, Mượn trả và Bồi thường.
 * Áp dụng đầy đủ: BR-02, BR-03, BR-04, BR-06, BR-07, BR-08, R-01, R-10, UC-5, UC-6, UC-9.
 */
public class CirculationServiceImpl implements CirculationService {

    private final CirculationRepository circulationRepository;

    public CirculationServiceImpl(CirculationRepository repository) {
        this.circulationRepository = Objects.requireNonNull(repository, "circulationRepository không được null");
    }

    private CirculationRepository getRepo() {
        return circulationRepository;
    }

    @Override
    public BorrowTicketDto createLoan(String readerIdentifier, List<String> copyBarcodes, LoanType loanType, String notes) {
        if (readerIdentifier == null || readerIdentifier.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập tên đăng nhập, email hoặc quẹt thẻ bạn đọc.");
        }
        if (copyBarcodes == null || copyBarcodes.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn hoặc quét mã vạch ít nhất một cuốn sách để mượn.");
        }

        CirculationRepository repo = getRepo();

        // 1. Kiểm tra bạn đọc & thẻ thư viện
        UserCardInfo card = repo.findCardByIdentifier(readerIdentifier)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin bạn đọc hoặc thẻ thư viện tương ứng."));

        if (!card.cardActive()) {
            throw new IllegalStateException("Thẻ thư viện của bạn đọc đang bị khóa. Vui lòng liên hệ thủ thư.");
        }
        if (card.cardExpired()) {
            throw new IllegalStateException("Thẻ thư viện đã hết hạn. Vui lòng gia hạn thẻ trước khi mượn sách.");
        }

        // 2. Kiểm tra nợ phạt (BR-07)
        if (repo.hasUnpaidFines(card.userId())) {
            throw new IllegalStateException("Bạn đọc đang có khoản phạt chưa thanh toán. Vui lòng thanh toán trước khi mượn sách (Quy tắc BR-07).");
        }

        // 3. Kiểm tra hạn mức mượn sách mang về nhà (BR-02: Tối đa 3 cuốn; Mượn tại chỗ không giới hạn)
        if (loanType == LoanType.VE_NHA) {
            int activeHomeLoans = repo.countActiveHomeLoans(card.userId());
            if (activeHomeLoans + copyBarcodes.size() > LibraryPolicy.MAX_BOOKS_BORROW) {
                throw new IllegalStateException("Mỗi bạn đọc chỉ được mượn tối đa 3 cuốn sách mang về nhà (Quy tắc BR-02). Hiện đang mượn: "
                    + activeHomeLoans + " cuốn.");
            }
        }

        // 4. Tính toán thời hạn trả
        LocalDateTime borrowDate = LocalDateTime.now();
        LocalDateTime dueDate;
        if (loanType == LoanType.VE_NHA) {
            dueDate = borrowDate.plusDays(LibraryPolicy.LOAN_DAYS_HOME).with(LocalTime.of(23, 59, 59));
        } else {
            dueDate = borrowDate.with(LocalTime.of(17, 0, 0)); // BR-04: Đọc tại chỗ trả trước 17:00
        }

        String ticketId = repo.generateNextTicketId();
        repo.createBorrowTicket(ticketId, card.userId(), card.cardNumber(), loanType, borrowDate, dueDate, copyBarcodes, notes);

        // Thông báo đồng bộ giao diện
        DomainEventPublisher.publish(new BookStockChangedEvent("LOAN", "LOAN_CREATED"));

        return new BorrowTicketDto(
            ticketId, card.userId(), card.fullName(), card.cardNumber(), loanType,
            borrowDate, dueDate, null, 1, "Mượn " + copyBarcodes.size() + " cuốn",
            String.join("; ", copyBarcodes), notes
        );
    }

    @Override
    public ReturnResultDto returnBook(String copyBarcode, String returnCondition) {
        if (copyBarcode == null || copyBarcode.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng quét hoặc nhập mã vạch cuốn sách cần trả.");
        }

        CirculationRepository repo = getRepo();
        BorrowDetailInfo detail = repo.findActiveLoanByCopy(copyBarcode)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu mượn đang hoạt động cho cuốn sách: " + copyBarcode));

        LocalDateTime now = LocalDateTime.now();
        int overdueDays = 0;
        BigDecimal overdueFine = BigDecimal.ZERO;

        if (detail.dueDate() != null && now.isAfter(detail.dueDate())) {
            overdueDays = (int) Math.max(1, ChronoUnit.DAYS.between(detail.dueDate().toLocalDate(), now.toLocalDate()));
            overdueFine = LibraryPolicy.FINE_PER_DAY.multiply(BigDecimal.valueOf(overdueDays));
        }

        BigDecimal damageFine = BigDecimal.ZERO;
        if (returnCondition != null && (returnCondition.toLowerCase().contains("hỏng") || returnCondition.toLowerCase().contains("rách"))) {
            damageFine = LibraryPolicy.TECHNICAL_FEE; // 20.000 VNĐ xử lý kỹ thuật (BR-08)
        }

        BigDecimal totalFine = overdueFine.add(damageFine);

        if (totalFine.compareTo(BigDecimal.ZERO) > 0) {
            String fineId = repo.generateNextFineId();
            String reason = overdueDays > 0 ? ("Trễ hạn " + overdueDays + " ngày") : "";
            if (damageFine.compareTo(BigDecimal.ZERO) > 0) {
                reason += (reason.isEmpty() ? "" : ", ") + "Hư hại nhẹ (" + returnCondition + ")";
            }
            repo.createFine(fineId, detail.userId(), detail.detailId(), reason, totalFine, "Phạt tại quầy khi trả sách");
        }

        repo.returnCopy(detail.detailId(), detail.ticketId(), detail.copyId(), now, returnCondition, totalFine);
        repo.closeTicketIfAllReturned(detail.ticketId());

        DomainEventPublisher.publish(new BookStockChangedEvent("RETURN", "BOOK_RETURNED"));

        return new ReturnResultDto(detail.ticketId(), copyBarcode, overdueDays, overdueFine, damageFine, totalFine, true);
    }

    @Override
    public CompensationResultDto reportLoss(String copyBarcode) {
        if (copyBarcode == null || copyBarcode.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng quét mã cuốn sách bị báo mất.");
        }

        CirculationRepository repo = getRepo();
        BorrowDetailInfo detail = repo.findActiveLoanByCopy(copyBarcode)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin mượn của cuốn sách: " + copyBarcode));

        LocalDateTime now = LocalDateTime.now();
        int overdueDays = 0;
        BigDecimal overdueFine = BigDecimal.ZERO;
        if (detail.dueDate() != null && now.isAfter(detail.dueDate())) {
            overdueDays = (int) Math.max(1, ChronoUnit.DAYS.between(detail.dueDate().toLocalDate(), now.toLocalDate()));
            overdueFine = LibraryPolicy.FINE_PER_DAY.multiply(BigDecimal.valueOf(overdueDays));
        }

        // BR-08: 200% Giá bìa + 20.000đ phí xử lý kỹ thuật + phạt quá hạn
        BigDecimal coverPrice = detail.coverPrice() != null ? detail.coverPrice() : new BigDecimal("50000.00");
        BigDecimal bookLossAmount = coverPrice.multiply(LibraryPolicy.COMPENSATION_RATE).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal techFee = LibraryPolicy.TECHNICAL_FEE;
        BigDecimal totalAmount = bookLossAmount.add(techFee).add(overdueFine).setScale(2, java.math.RoundingMode.HALF_UP);

        // Chuyển trạng thái phiếu mượn sang CHO_XU_LY_BOI_THUONG (4) theo SRS UC-9 & R-10
        repo.updateTicketStatus(detail.ticketId(), LoanTicketStatus.CHO_XU_LY_BOI_THUONG.getCode());
        repo.markCopyLost(detail.copyId());

        String fineId = repo.generateNextFineId();
        repo.createFine(fineId, detail.userId(), detail.detailId(), "Bồi thường mất sách: " + detail.bookTitle(),
            totalAmount, "200% giá bìa + 20.000đ phí + phạt quá hạn");

        DomainEventPublisher.publish(new BookStockChangedEvent("LOSS", "BOOK_LOST"));

        return new CompensationResultDto(
            fineId, detail.ticketId(), detail.bookTitle(), coverPrice, bookLossAmount,
            techFee, overdueDays, overdueFine, totalAmount
        );
    }

    @Override
    public void payFine(String fineTicketId, String paymentMethod) {
        if (fineTicketId == null || fineTicketId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã phiếu phạt không hợp lệ.");
        }
        // Đóng bồi hoàn và chính thức chuyển trạng thái phiếu mượn sang DA_MAT (5) (SRS UC-9, R-10)
        getRepo().payFineAndCloseLoss(fineTicketId.trim(), paymentMethod);
        DomainEventPublisher.publish(new BookStockChangedEvent("FINE", "FINE_PAID"));
    }

    @Override
    public void renewLoan(String borrowTicketId, String reason) {
        if (borrowTicketId == null || borrowTicketId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã phiếu mượn không hợp lệ.");
        }

        CirculationRepository repo = getRepo();

        if (repo.isTicketRenewed(borrowTicketId.trim())) {
            throw new IllegalStateException("Mỗi phiếu mượn chỉ được gia hạn tối đa 1 lần (Quy tắc BR-03).");
        }

        List<BorrowTicketDto> tickets = repo.findTickets(borrowTicketId.trim(), null);
        if (tickets.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy phiếu mượn: " + borrowTicketId);
        }

        BorrowTicketDto ticket = tickets.get(0);
        if (ticket.getStatus() != LoanTicketStatus.DANG_MUON.getCode()) {
            throw new IllegalStateException("Phiếu mượn không ở trạng thái đang mượn hợp lệ để gia hạn.");
        }

        if (ticket.getDueDate() != null && LocalDateTime.now().isAfter(ticket.getDueDate())) {
            throw new IllegalStateException("Phiếu mượn đã quá hạn, không thể gia hạn trực tuyến. Vui lòng mang sách đến quầy thủ thư.");
        }

        // BR-03: Gia hạn thêm 7 ngày
        LocalDateTime newDueDate = ticket.getDueDate().plusDays(LibraryPolicy.RENEW_DAYS);
        repo.extendDueDate(borrowTicketId.trim(), newDueDate, reason);
    }

    @Override
    public void reserveBook(String userId, String bookId) {
        String resId = "DC-" + System.currentTimeMillis() % 1000000;
        getRepo().createReservation(resId, userId, bookId);
    }

    @Override
    public List<BorrowTicketDto> getReaderLoans(String userId) {
        return getRepo().findTicketsByReader(userId);
    }

    @Override
    public List<BorrowTicketDto> getAllLoans(String keyword, Integer status) {
        return getRepo().findTickets(keyword, status);
    }

    @Override
    public UserCardInfo getCardInfo(String identifier) {
        return getRepo().findCardByIdentifier(identifier).orElse(null);
    }

    @Override
    public BookCopyInfo getCopyInfo(String barcode) {
        return getRepo().findCopyByBarcode(barcode).orElse(null);
    }

    @Override
    public List<BookCopyInfo> getAvailableCopies(String bookId) {
        return getRepo().findAvailableCopies(bookId);
    }

    @Override
    public CirculationRepository.BorrowDetailInfo findActiveLoanByCopy(String copyBarcode) {
        return getRepo().findActiveLoanByCopy(copyBarcode).orElse(null);
    }
}
