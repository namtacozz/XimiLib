package com.ximilib.application.service.impl;

import com.ximilib.application.dto.BorrowTicketDto;
import com.ximilib.application.dto.CompensationResultDto;
import com.ximilib.application.dto.ReturnResultDto;
import com.ximilib.application.port.CirculationQueryPort;
import com.ximilib.application.service.CirculationService;
import com.ximilib.domain.event.BookStockChangedEvent;
import com.ximilib.domain.event.DomainEventPublisher;
import com.ximilib.domain.model.LibraryPolicy;
import com.ximilib.domain.model.LoanTicketStatus;
import com.ximilib.domain.model.LoanType;
import com.ximilib.domain.repository.CirculationRepository;
import com.ximilib.domain.repository.CirculationRepository.*;
import com.ximilib.domain.service.FineCalculationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CirculationServiceImpl implements CirculationService {

    private final CirculationRepository circulationRepository;
    private final CirculationQueryPort circulationQueryPort;
    private final FineCalculationService fineCalculationService;
    private final com.ximilib.application.port.TransactionCoordinator transactionCoordinator;

    public CirculationServiceImpl(CirculationRepository repository) {
        this(repository, (repository instanceof CirculationQueryPort cqp) ? cqp : null, new FineCalculationService(), null);
    }

    public CirculationServiceImpl(CirculationRepository repository, CirculationQueryPort queryPort) {
        this(repository, queryPort, new FineCalculationService(), null);
    }

    public CirculationServiceImpl(CirculationRepository repository, FineCalculationService fineCalculationService) {
        this(repository, (repository instanceof CirculationQueryPort cqp) ? cqp : null, fineCalculationService, null);
    }

    public CirculationServiceImpl(CirculationRepository repository, CirculationQueryPort queryPort, FineCalculationService fineCalculationService) {
        this(repository, queryPort, fineCalculationService, null);
    }

    public CirculationServiceImpl(CirculationRepository repository, CirculationQueryPort queryPort,
                                  FineCalculationService fineCalculationService,
                                  com.ximilib.application.port.TransactionCoordinator transactionCoordinator) {
        this.circulationRepository = Objects.requireNonNull(repository, "circulationRepository không được null");
        this.circulationQueryPort = (queryPort != null) ? queryPort : ((repository instanceof CirculationQueryPort cqp) ? cqp : null);
        this.fineCalculationService = (fineCalculationService != null) ? fineCalculationService : new FineCalculationService();
        this.transactionCoordinator = transactionCoordinator;
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

        UserCardInfo card = repo.findCardByIdentifier(readerIdentifier)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin bạn đọc hoặc thẻ thư viện tương ứng."));

        if (!card.cardActive()) {
            throw new IllegalStateException("Thẻ thư viện của bạn đọc đang bị khóa. Vui lòng liên hệ thủ thư.");
        }
        if (card.cardExpired()) {
            throw new IllegalStateException("Thẻ thư viện của bạn đã hết hạn sử dụng. Vui lòng liên hệ thủ thư để gia hạn");
        }

        if (repo.hasOverdueLoans(card.userId())) {
            throw new IllegalStateException("Bạn có sách quá hạn chưa hoàn trả. Vui lòng trả sách trước khi mượn tiếp");
        }

        if (repo.hasUnpaidFines(card.userId())) {
            throw new IllegalStateException("Bạn đọc đang có khoản phạt chưa thanh toán. Vui lòng thanh toán trước khi mượn sách (Quy tắc BR-07).");
        }

        if (loanType == LoanType.VE_NHA) {
            int activeHomeLoans = repo.countActiveHomeLoans(card.userId());
            if (activeHomeLoans + copyBarcodes.size() > LibraryPolicy.MAX_BOOKS_BORROW) {
                throw new IllegalStateException("Bạn đã đạt giới hạn mượn tối đa 3 cuốn sách mang về");
            }
        }

        LocalDateTime borrowDate = LocalDateTime.now();
        LocalDateTime dueDate;
        if (loanType == LoanType.VE_NHA) {
            dueDate = borrowDate.plusDays(LibraryPolicy.LOAN_DAYS_HOME).with(LocalTime.of(23, 59, 59));
        } else {
            dueDate = borrowDate.with(LocalTime.of(17, 0, 0));
        }

        String ticketId = repo.generateNextTicketId();
        if (transactionCoordinator != null) {
            transactionCoordinator.executeVoid(tx -> {
                repo.createBorrowTicket(tx, ticketId, card.userId(), card.cardNumber(), loanType, borrowDate, dueDate, copyBarcodes, notes);
            });
        } else {
            repo.createBorrowTicket(ticketId, card.userId(), card.cardNumber(), loanType, borrowDate, dueDate, copyBarcodes, notes);
        }

        DomainEventPublisher.publish(new BookStockChangedEvent("LOAN", "LOAN_CREATED"));

        boolean isOnlineOrder = (notes != null && (notes.toLowerCase().contains("online") || notes.toLowerCase().contains("app") || notes.toLowerCase().contains("đặt mượn")));
        int initialStatus = isOnlineOrder ? 0 : 1;

        return new BorrowTicketDto(
            ticketId, card.userId(), card.fullName(), card.cardNumber(), loanType,
            borrowDate, dueDate, null, initialStatus, "Mượn " + copyBarcodes.size() + " cuốn",
            String.join("; ", copyBarcodes), notes
        );
    }

    @Override
    public ReturnResultDto returnBook(String copyBarcode, String returnCondition) {
        if (copyBarcode == null || copyBarcode.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng quét hoặc nhập mã vạch cuốn sách cần trả.");
        }

        CirculationRepository repo = getRepo();
        BorrowDetailInfo detail = repo.findActiveLoanByCopy(copyBarcode.trim())
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu mượn đang hoạt động cho cuốn sách: " + copyBarcode));

        LocalDateTime now = LocalDateTime.now();
        boolean isDamaged = returnCondition != null && (returnCondition.toLowerCase().contains("hỏng") || returnCondition.toLowerCase().contains("rách"));

        FineCalculationService.CompensationBreakdown breakdown = fineCalculationService.calculateCompensation(
            detail.coverPrice(), detail.dueDate(), now, false, isDamaged
        );

        int overdueDays = breakdown.getOverdueDays();
        BigDecimal overdueFine = breakdown.getOverdueFine();
        BigDecimal damageFine = breakdown.getTechFee();
        BigDecimal totalFine = breakdown.getTotalAmount();

        if (totalFine.compareTo(BigDecimal.ZERO) > 0) {
            String fineId = repo.generateNextFineId();
            String reason = overdueDays > 0 ? ("Trễ hạn " + overdueDays + " ngày") : "";
            if (damageFine.compareTo(BigDecimal.ZERO) > 0) {
                reason += (reason.isEmpty() ? "" : ", ") + "Hư hại nhẹ (" + returnCondition + ")";
            }
            repo.createFine(fineId, detail.userId(), detail.detailId(), reason, totalFine, "Phạt tại quầy khi trả sách");
        }

        if (transactionCoordinator != null) {
            transactionCoordinator.executeVoid(tx -> {
                repo.returnCopy(tx, detail.detailId(), detail.ticketId(), detail.copyId(), now, returnCondition, totalFine);
                repo.closeTicketIfAllReturned(tx, detail.ticketId());
            });
        } else {
            repo.returnCopy(detail.detailId(), detail.ticketId(), detail.copyId(), now, returnCondition, totalFine);
            repo.closeTicketIfAllReturned(detail.ticketId());
        }

        DomainEventPublisher.publish(new BookStockChangedEvent("RETURN", "BOOK_RETURNED"));

        return new ReturnResultDto(detail.ticketId(), copyBarcode, overdueDays, overdueFine, damageFine, totalFine, true);
    }

    @Override
    public CompensationResultDto reportLoss(String copyBarcode) {
        if (copyBarcode == null || copyBarcode.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng quét mã cuốn sách bị báo mất.");
        }

        CirculationRepository repo = getRepo();
        BorrowDetailInfo detail = repo.findActiveLoanByCopy(copyBarcode.trim())
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin mượn của cuốn sách: " + copyBarcode));

        LocalDateTime now = LocalDateTime.now();
        FineCalculationService.CompensationBreakdown breakdown = fineCalculationService.calculateCompensation(
            detail.coverPrice(), detail.dueDate(), now, true, false
        );

        String existingFineId = repo.findUnpaidFineByDetailId(detail.detailId()).orElse(null);
        String fineId = (existingFineId != null) ? existingFineId : repo.generateNextFineId();
        if (existingFineId == null) {
            if (transactionCoordinator != null) {
                transactionCoordinator.executeVoid(tx -> {
                    repo.updateTicketStatus(detail.ticketId(), LoanTicketStatus.CHO_XU_LY_BOI_THUONG.getCode());
                    repo.markCopyLost(detail.copyId());
                    repo.createFine(tx, fineId, detail.userId(), detail.detailId(), "Bồi thường mất sách: " + detail.bookTitle(),
                        breakdown.getTotalAmount(), "200% giá bìa + 20.000đ phí + phạt quá hạn");
                });
            } else {
                repo.updateTicketStatus(detail.ticketId(), LoanTicketStatus.CHO_XU_LY_BOI_THUONG.getCode());
                repo.markCopyLost(detail.copyId());
                repo.createFine(fineId, detail.userId(), detail.detailId(), "Bồi thường mất sách: " + detail.bookTitle(),
                    breakdown.getTotalAmount(), "200% giá bìa + 20.000đ phí + phạt quá hạn");
            }
        } else {
            repo.updateTicketStatus(detail.ticketId(), LoanTicketStatus.CHO_XU_LY_BOI_THUONG.getCode());
            repo.markCopyLost(detail.copyId());
        }

        DomainEventPublisher.publish(new BookStockChangedEvent("LOSS", "BOOK_LOST"));

        return new CompensationResultDto(
            fineId, detail.ticketId(), detail.bookTitle(), breakdown.getCoverPrice(),
            breakdown.getBookLossAmount(), breakdown.getTechFee(), breakdown.getOverdueDays(),
            breakdown.getOverdueFine(), breakdown.getTotalAmount()
        );
    }

    @Override
    public CompensationResultDto calculateCompensation(String copyBarcode, boolean isLost, boolean isDamaged) {
        if (copyBarcode == null || copyBarcode.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng quét hoặc nhập mã vạch cuốn sách.");
        }

        CirculationRepository repo = getRepo();
        BorrowDetailInfo detail = repo.findActiveLoanByCopy(copyBarcode.trim())
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu mượn đang hoạt động cho cuốn sách: " + copyBarcode));

        FineCalculationService.CompensationBreakdown breakdown = fineCalculationService.calculateCompensation(
            detail.coverPrice(), detail.dueDate(), LocalDateTime.now(), isLost, isDamaged
        );

        String fineTicketId = repo.findUnpaidFineByDetailId(detail.detailId()).orElse(null);

        return new CompensationResultDto(
            fineTicketId,
            detail.ticketId(),
            detail.bookTitle(),
            breakdown.getCoverPrice(),
            breakdown.getBookLossAmount(),
            breakdown.getTechFee(),
            breakdown.getOverdueDays(),
            breakdown.getOverdueFine(),
            breakdown.getTotalAmount()
        );
    }

    @Override
    public String findUnpaidFineByTicketId(String ticketId) {
        if (ticketId == null || ticketId.isBlank()) return null;
        return getRepo().findUnpaidFineByTicketId(ticketId.trim()).orElse(null);
    }

    @Override
    public void payFine(String fineTicketId, String paymentMethod) {
        if (fineTicketId == null || fineTicketId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã phiếu phạt không hợp lệ.");
        }

        if (transactionCoordinator != null) {
            transactionCoordinator.executeVoid(tx -> {
                getRepo().payFineAndCloseLoss(tx, fineTicketId.trim(), paymentMethod);
            });
        } else {
            getRepo().payFineAndCloseLoss(fineTicketId.trim(), paymentMethod);
        }
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

        List<BorrowTicketDto> tickets = (circulationQueryPort != null)
            ? circulationQueryPort.findTickets(borrowTicketId.trim(), null)
            : Collections.emptyList();
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

        LocalDateTime newDueDate = ticket.getDueDate().plusDays(LibraryPolicy.RENEW_DAYS);
        if (transactionCoordinator != null) {
            transactionCoordinator.executeVoid(tx -> {
                repo.extendDueDate(tx, borrowTicketId.trim(), newDueDate, reason);
            });
        } else {
            repo.extendDueDate(borrowTicketId.trim(), newDueDate, reason);
        }
    }

    @Override
    public void reserveBook(String userId, String bookId) {
        String resId = "DC-" + System.currentTimeMillis() % 1000000;
        getRepo().createReservation(resId, userId, bookId);
    }

    @Override
    public List<BorrowTicketDto> getReaderLoans(String userId) {
        if (circulationQueryPort != null) {
            return circulationQueryPort.findTicketsByReader(userId);
        }
        return Collections.emptyList();
    }

    @Override
    public List<BorrowTicketDto> getAllLoans(String keyword, Integer status) {
        if (circulationQueryPort != null) {
            return circulationQueryPort.findTickets(keyword, status);
        }
        return Collections.emptyList();
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
