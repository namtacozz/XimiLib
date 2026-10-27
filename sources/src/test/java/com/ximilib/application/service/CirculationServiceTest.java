package com.ximilib.application.service;

import com.ximilib.application.dto.BorrowTicketDto;
import com.ximilib.application.dto.CompensationResultDto;
import com.ximilib.application.dto.ReturnResultDto;
import com.ximilib.application.service.impl.CirculationServiceImpl;
import com.ximilib.domain.model.LoanTicketStatus;
import com.ximilib.domain.model.LoanType;
import com.ximilib.domain.repository.CirculationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Nghiệp vụ Lưu Hành & Bồi Thường (CirculationService - SRS TC-1..TC-4, BR-02..BR-08)")
class CirculationServiceTest {

    private CirculationService circulationService;
    private StubCirculationRepository repository;

    @BeforeEach
    void setUp() {
        repository = new StubCirculationRepository();
        circulationService = new CirculationServiceImpl(repository);
    }

    @Test
    @DisplayName("SRS TC-1: Mượn sách về nhà đúng hạn 14 ngày")
    void testCreateHomeLoanDue14Days() {
        BorrowTicketDto ticket = circulationService.createLoan(
            "LIB-2026-00001",
            List.of("BK001-C01", "BK001-C02"),
            LoanType.VE_NHA,
            "Mượn học tập"
        );

        assertNotNull(ticket);
        assertEquals(LoanType.VE_NHA, ticket.getLoanType());
        assertEquals(LoanTicketStatus.DANG_MUON.getCode(), ticket.getStatus());
        assertEquals(14, java.time.temporal.ChronoUnit.DAYS.between(ticket.getBorrowDate().toLocalDate(), ticket.getDueDate().toLocalDate()));
    }

    @Test
    @DisplayName("SRS TC-2: Trả sách trễ 5 ngày phạt 10.000 VNĐ (BR-06)")
    void testReturnOverdueFiveDays() {
        // Cuốn sách mượn có hạn trả là 5 ngày trước
        repository.addActiveLoan("BK001-C01", LocalDateTime.now().minusDays(5), new BigDecimal("80000.00"));

        ReturnResultDto result = circulationService.returnBook("BK001-C01", "Bình thường");

        assertEquals(5, result.getOverdueDays());
        assertEquals(new BigDecimal("10000.00"), result.getOverdueFine());
        assertEquals(new BigDecimal("10000.00"), result.getTotalFine());
    }

    @Test
    @DisplayName("SRS TC-3: Bạn đọc nợ phạt bị chặn mượn sách mới (BR-07)")
    void testBorrowBlockedWhenHasUnpaidFine() {
        repository.setHasUnpaidFine(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            circulationService.createLoan("LIB-2026-00001", List.of("BK001-C01"), LoanType.VE_NHA, "");
        });

        assertTrue(ex.getMessage().contains("khoản phạt chưa thanh toán"));
    }

    @Test
    @DisplayName("SRS TC-4 & R-10: Báo mất sách tính bồi thường 200% + 20k phí, chuyển CHO_XU_LY_BOI_THUONG rồi nộp phạt chuyển DA_MAT")
    void testReportLossAndPayCompensation() {
        // Sách giá bìa 100.000đ, không trễ hạn
        repository.addActiveLoan("BK002-C01", LocalDateTime.now().plusDays(5), new BigDecimal("100000.00"));

        CompensationResultDto loss = circulationService.reportLoss("BK002-C01");

        // 200% của 100.000 = 200.000 + 20.000 phí kỹ thuật = 220.000 VNĐ
        assertEquals(new BigDecimal("200000.00"), loss.getBookLossAmount());
        assertEquals(new BigDecimal("20000.00"), loss.getTechFee());
        assertEquals(new BigDecimal("220000.00"), loss.getTotalAmount());
        assertEquals(LoanTicketStatus.CHO_XU_LY_BOI_THUONG.getCode(), repository.getTicketStatus(loss.getBorrowTicketId()));

        // Nộp phạt hoàn tất: phiếu chuyển sang DA_MAT (5)
        circulationService.payFine(loss.getFineTicketId(), "TIEN_MAT");
        assertEquals(LoanTicketStatus.DA_MAT.getCode(), repository.getTicketStatus(loss.getBorrowTicketId()));
    }

    @Test
    @DisplayName("BR-02: Chặn mượn quá 3 cuốn mang về nhà")
    void testBlockExceedingThreeHomeLoans() {
        repository.setActiveHomeLoansCount(2);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            circulationService.createLoan("LIB-2026-00001", List.of("BK001-C01", "BK001-C02"), LoanType.VE_NHA, "");
        });

        assertTrue(ex.getMessage().contains("tối đa 3 cuốn sách mang về nhà"));
    }

    @Test
    @DisplayName("BR-03: Gia hạn thêm 7 ngày và chặn gia hạn lần 2")
    void testRenewLoanSevenDaysAndBlockSecondRenewal() {
        String ticketId = "PM-TEST-01";
        repository.addTicket(ticketId, LoanTicketStatus.DANG_MUON.getCode(), LocalDateTime.now().plusDays(2));

        assertDoesNotThrow(() -> circulationService.renewLoan(ticketId, "Cần thêm thời gian đọc"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            circulationService.renewLoan(ticketId, "Gia hạn thêm lần nữa");
        });
        assertTrue(ex.getMessage().contains("tối đa 1 lần"));
    }

    private static class StubCirculationRepository implements CirculationRepository {
        private boolean hasUnpaidFine = false;
        private int activeHomeLoansCount = 0;
        private final Map<String, Integer> ticketStatuses = new HashMap<>();
        private final Map<String, LocalDateTime> ticketDueDates = new HashMap<>();
        private final Set<String> renewedTickets = new HashSet<>();
        private final Map<String, BorrowDetailInfo> activeCopies = new HashMap<>();

        public void setHasUnpaidFine(boolean v) { this.hasUnpaidFine = v; }
        public void setActiveHomeLoansCount(int c) { this.activeHomeLoansCount = c; }
        public int getTicketStatus(String tid) { return ticketStatuses.getOrDefault(tid, -1); }

        public void addActiveLoan(String copyBarcode, LocalDateTime dueDate, BigDecimal coverPrice) {
            String ticketId = "PM-STUB-" + copyBarcode;
            BorrowDetailInfo detail = new BorrowDetailInfo(
                "D-" + copyBarcode, ticketId, copyBarcode, "Sách mẫu", "ND004", dueDate, coverPrice
            );
            activeCopies.put(copyBarcode, detail);
            ticketStatuses.put(ticketId, LoanTicketStatus.DANG_MUON.getCode());
            ticketDueDates.put(ticketId, dueDate);
        }

        public void addTicket(String tid, int status, LocalDateTime dueDate) {
            ticketStatuses.put(tid, status);
            ticketDueDates.put(tid, dueDate);
        }

        @Override
        public Optional<UserCardInfo> findCardByIdentifier(String identifier) {
            return Optional.of(new UserCardInfo("ND004", "Trần Văn An", "LIB-2026-00001", true, false));
        }

        @Override
        public Optional<BookCopyInfo> findCopyByBarcode(String barcode) {
            return Optional.of(new BookCopyInfo(barcode, "BK001", "Sách mẫu", new BigDecimal("50000"), "KHA_DUNG", "Khu A - Kệ 1"));
        }

        @Override
        public List<BookCopyInfo> findAvailableCopies(String bookId) {
            return List.of(new BookCopyInfo(bookId + "-C01", bookId, "Sách mẫu", new BigDecimal("50000"), "KHA_DUNG", "Khu A - Kệ 1"));
        }

        @Override
        public int countActiveHomeLoans(String userId) {
            return activeHomeLoansCount;
        }

        @Override
        public boolean hasUnpaidFines(String userId) {
            return hasUnpaidFine;
        }

        @Override
        public void createBorrowTicket(String ticketId, String userId, String cardId, LoanType loanType,
                                       LocalDateTime borrowDate, LocalDateTime dueDate, List<String> copyIds, String notes) {
            ticketStatuses.put(ticketId, 1);
            ticketDueDates.put(ticketId, dueDate);
        }

        @Override
        public List<BorrowTicketDto> findTickets(String keyword, Integer statusFilter) {
            List<BorrowTicketDto> list = new ArrayList<>();
            ticketStatuses.forEach((k, v) -> {
                list.add(new BorrowTicketDto(k, "ND004", "Trần Văn An", "LIB-2026-00001", LoanType.VE_NHA,
                    LocalDateTime.now(), ticketDueDates.get(k), null, v, "Sách", "C01", ""));
            });
            return list;
        }

        @Override
        public List<BorrowTicketDto> findTicketsByReader(String userId) {
            return findTickets(null, null);
        }

        @Override
        public Optional<BorrowDetailInfo> findActiveLoanByCopy(String copyBarcode) {
            return Optional.ofNullable(activeCopies.get(copyBarcode));
        }

        @Override
        public void returnCopy(String detailId, String ticketId, String copyId, LocalDateTime returnDate, String condition, BigDecimal fine) {
            activeCopies.remove(copyId);
        }

        @Override
        public void closeTicketIfAllReturned(String ticketId) {
            ticketStatuses.put(ticketId, LoanTicketStatus.DA_TRA.getCode());
        }

        @Override
        public void createFine(String fineId, String userId, String detailId, String reason, BigDecimal amount, String notes) {}

        @Override
        public void updateTicketStatus(String ticketId, int status) {
            ticketStatuses.put(ticketId, status);
        }

        @Override
        public void markCopyLost(String copyId) {}

        @Override
        public void payFineAndCloseLoss(String fineId, String paymentMethod) {
            ticketStatuses.forEach((k, v) -> {
                if (v == LoanTicketStatus.CHO_XU_LY_BOI_THUONG.getCode()) {
                    ticketStatuses.put(k, LoanTicketStatus.DA_MAT.getCode());
                }
            });
        }

        @Override
        public boolean isTicketRenewed(String ticketId) {
            return renewedTickets.contains(ticketId);
        }

        @Override
        public int countReservations(String bookId) {
            return 0;
        }

        @Override
        public void extendDueDate(String ticketId, LocalDateTime newDueDate, String reason) {
            renewedTickets.add(ticketId);
            ticketDueDates.put(ticketId, newDueDate);
        }

        @Override
        public void createReservation(String resId, String userId, String bookId) {}

        @Override
        public String generateNextTicketId() {
            return "PM-" + System.currentTimeMillis() % 100000;
        }

        @Override
        public String generateNextFineId() {
            return "PP-" + System.currentTimeMillis() % 100000;
        }
    }
}
