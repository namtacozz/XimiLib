package com.ximilib.domain.service;

import com.ximilib.domain.model.LibraryPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Dịch vụ Tính phạt & Bồi thường (FineCalculationService - BR-06, BR-08)")
class FineCalculationServiceTest {

    private FineCalculationService fineCalculationService;

    @BeforeEach
    void setUp() {
        fineCalculationService = new FineCalculationService();
    }

    @Test
    @DisplayName("Ví dụ 1 (BR-08): Mất sách trong hạn (Giá bìa 80.000đ, trễ 0 ngày -> Bồi thường 180.000đ)")
    void testExample1_LostBookWithinDueDate() {
        BigDecimal coverPrice = new BigDecimal("80000.00");
        LocalDateTime dueDate = LocalDateTime.now().plusDays(3);
        LocalDateTime actualDate = LocalDateTime.now();

        FineCalculationService.CompensationBreakdown result = fineCalculationService.calculateCompensation(
                coverPrice, dueDate, actualDate, true, false
        );

        assertEquals(0, result.getOverdueDays());
        assertEquals(new BigDecimal("0.00"), result.getOverdueFine());
        assertEquals(new BigDecimal("160000.00"), result.getBookLossAmount());
        assertEquals(new BigDecimal("20000.00"), result.getTechFee());
        assertEquals(new BigDecimal("180000.00"), result.getTotalAmount());
    }

    @Test
    @DisplayName("Ví dụ 2 (BR-08): Mất sách khi trễ 5 ngày (Giá bìa 100.000đ -> Bồi thường 230.000đ)")
    void testExample2_LostBookOverdueFiveDays() {
        BigDecimal coverPrice = new BigDecimal("100000.00");
        LocalDateTime dueDate = LocalDateTime.now().minusDays(5);
        LocalDateTime actualDate = LocalDateTime.now();

        FineCalculationService.CompensationBreakdown result = fineCalculationService.calculateCompensation(
                coverPrice, dueDate, actualDate, true, false
        );

        assertEquals(5, result.getOverdueDays());
        assertEquals(new BigDecimal("10000.00"), result.getOverdueFine());
        assertEquals(new BigDecimal("200000.00"), result.getBookLossAmount());
        assertEquals(new BigDecimal("20000.00"), result.getTechFee());
        assertEquals(new BigDecimal("230000.00"), result.getTotalAmount());
    }

    @Test
    @DisplayName("Ví dụ 3 (BR-08): Sách hỏng bìa/rách nhẹ phục hồi được (Phí kỹ thuật 20.000đ)")
    void testExample3_DamagedBookRestorable() {
        BigDecimal coverPrice = new BigDecimal("120000.00");
        LocalDateTime dueDate = LocalDateTime.now().plusDays(2);
        LocalDateTime actualDate = LocalDateTime.now();

        FineCalculationService.CompensationBreakdown result = fineCalculationService.calculateCompensation(
                coverPrice, dueDate, actualDate, false, true
        );

        assertEquals(0, result.getOverdueDays());
        assertEquals(new BigDecimal("0.00"), result.getOverdueFine());
        assertEquals(new BigDecimal("0.00"), result.getBookLossAmount());
        assertEquals(new BigDecimal("20000.00"), result.getTechFee());
        assertEquals(new BigDecimal("20000.00"), result.getTotalAmount());
    }

    @Test
    @DisplayName("Trả sách đúng hạn, không hư hỏng: Tiền phạt = 0 VNĐ")
    void testReturnOnTimeNormalCondition() {
        BigDecimal coverPrice = new BigDecimal("90000.00");
        LocalDateTime dueDate = LocalDateTime.now().plusDays(7);
        LocalDateTime actualDate = LocalDateTime.now();

        FineCalculationService.CompensationBreakdown result = fineCalculationService.calculateCompensation(
                coverPrice, dueDate, actualDate, false, false
        );

        assertEquals(0, result.getOverdueDays());
        assertEquals(new BigDecimal("0.00"), result.getOverdueFine());
        assertEquals(new BigDecimal("0.00"), result.getBookLossAmount());
        assertEquals(new BigDecimal("0.00"), result.getTechFee());
        assertEquals(new BigDecimal("0.00"), result.getTotalAmount());
    }

    @Test
    @DisplayName("Trả sách trễ 1 ngày: Phạt đúng 2.000 VNĐ (BR-06)")
    void testReturnOverdueOneDay() {
        BigDecimal coverPrice = new BigDecimal("90000.00");
        LocalDateTime dueDate = LocalDateTime.now().minusDays(1);
        LocalDateTime actualDate = LocalDateTime.now();

        FineCalculationService.CompensationBreakdown result = fineCalculationService.calculateCompensation(
                coverPrice, dueDate, actualDate, false, false
        );

        assertEquals(1, result.getOverdueDays());
        assertEquals(new BigDecimal("2000.00"), result.getOverdueFine());
        assertEquals(new BigDecimal("0.00"), result.getBookLossAmount());
        assertEquals(new BigDecimal("0.00"), result.getTechFee());
        assertEquals(new BigDecimal("2000.00"), result.getTotalAmount());
    }

    @Test
    @DisplayName("BR-06: Phạt nhiều cuốn sách trễ hạn (3 cuốn trễ 3 ngày = 18.000 VNĐ, 2 cuốn trễ 5 ngày = 20.000 VNĐ)")
    void testMultipleBooksOverdueFine() {

        BigDecimal fine1 = fineCalculationService.calculateOverdueFine(3, 3);
        assertEquals(new BigDecimal("18000.00"), fine1);

        BigDecimal fine2 = fineCalculationService.calculateOverdueFine(5, 2);
        assertEquals(new BigDecimal("20000.00"), fine2);

        assertEquals(new BigDecimal("0.00"), fineCalculationService.calculateOverdueFine(0, 3));
        assertEquals(new BigDecimal("0.00"), fineCalculationService.calculateOverdueFine(3, 0));
    }

    @Test
    @DisplayName("Ca biên: Ngày trễ = 0 khi trả trong ngày trước giờ hẹn")
    void testOverdueZeroSameDayBeforeDue() {
        LocalDateTime dueDate = LocalDateTime.of(2026, 11, 1, 17, 0, 0);
        LocalDateTime actualDate = LocalDateTime.of(2026, 11, 1, 16, 30, 0);

        int days = fineCalculationService.calculateOverdueDays(dueDate, actualDate);
        assertEquals(0, days);
        assertEquals(new BigDecimal("0.00"), fineCalculationService.calculateOverdueFine(days));
    }

    @Test
    @DisplayName("Ca biên: Trễ qua nửa đêm tính theo mốc 00:00 (từ 23:59:59 sang 00:00:01 tính đúng 1 ngày trễ)")
    void testOverdueCrossingMidnightBoundary() {
        LocalDateTime dueDate = LocalDateTime.of(2026, 11, 1, 23, 59, 59);
        LocalDateTime actualDate = LocalDateTime.of(2026, 11, 2, 0, 0, 1);

        int days = fineCalculationService.calculateOverdueDays(dueDate, actualDate);
        assertEquals(1, days, "Qua nửa đêm sang ngày hôm sau phải tính là 1 ngày trễ hạn");
        assertEquals(new BigDecimal("2000.00"), fineCalculationService.calculateOverdueFine(days));
    }

    @Test
    @DisplayName("Ca biên: Giá bìa lẻ và làm tròn HALF_UP khi tính đền bù 200%")
    void testOddCoverPriceAndRoundingHalfUp() {

        BigDecimal oddPrice1 = new BigDecimal("75333.33");
        BigDecimal lossAmount1 = fineCalculationService.calculateBookLossAmount(oddPrice1);
        assertEquals(new BigDecimal("150666.66"), lossAmount1);

        BigDecimal oddPrice2 = new BigDecimal("85555.555");
        FineCalculationService.CompensationBreakdown breakdown = fineCalculationService.calculateCompensation(
                oddPrice2, LocalDateTime.now().plusDays(1), LocalDateTime.now(), true, false
        );
        assertEquals(new BigDecimal("85555.56"), breakdown.getCoverPrice());
        assertEquals(new BigDecimal("171111.12"), breakdown.getBookLossAmount());
        assertEquals(new BigDecimal("20000.00"), breakdown.getTechFee());
        assertEquals(new BigDecimal("191111.12"), breakdown.getTotalAmount());
    }
}
