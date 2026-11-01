package com.ximilib.domain.service;

import com.ximilib.domain.model.LibraryPolicy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Dịch vụ tính toán tiền phạt và bồi thường trong Domain (Domain Service).
 * Thực thi các quy tắc nghiệp vụ BR-06 (Phạt quá hạn) và BR-08 (Bồi thường mất/hỏng sách).
 * Tuân thủ R-01, R-02, R-04, R-09.
 */
public class FineCalculationService {

    /** Giá bìa mặc định khi đầu sách không có giá niêm yết: 50.000 VNĐ */
    private static final BigDecimal DEFAULT_COVER_PRICE = new BigDecimal("50000.00");

    /**
     * Tính số ngày trễ hạn giữa hạn trả và ngày thực trả theo BR-06.
     * Mốc tính theo số ngày dương lịch. Nếu trong cùng ngày nhưng sau giờ hẹn (ví dụ mượn tại chỗ quá 17:00),
     * làm tròn tối thiểu là 1 ngày trễ.
     *
     * @param dueDate   Hạn trả sách
     * @param actualDate Ngày giờ thực tế trả hoặc báo mất
     * @return Số ngày trễ (>= 0)
     */
    public int calculateOverdueDays(LocalDateTime dueDate, LocalDateTime actualDate) {
        if (dueDate == null || actualDate == null || !actualDate.isAfter(dueDate)) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(dueDate.toLocalDate(), actualDate.toLocalDate());
        return (int) Math.max(1, days);
    }

    /**
     * Tính tiền phạt quá hạn cho 1 cuốn sách theo BR-06.
     * Đơn vị: 2.000 VNĐ / ngày trễ / cuốn sách.
     *
     * @param overdueDays Số ngày trễ hạn
     * @return Tiền phạt trễ hạn (DECIMAL scale 2)
     */
    public BigDecimal calculateOverdueFine(int overdueDays) {
        return calculateOverdueFine(overdueDays, 1);
    }

    /**
     * Tính tiền phạt quá hạn cho nhiều cuốn sách theo BR-06.
     * Công thức: TienPhatQuaHan = Số ngày trễ * 2.000 VNĐ * Số cuốn quá hạn.
     *
     * @param overdueDays Số ngày trễ hạn
     * @param bookCount   Số cuốn sách quá hạn
     * @return Tổng tiền phạt quá hạn
     */
    public BigDecimal calculateOverdueFine(int overdueDays, int bookCount) {
        if (overdueDays <= 0 || bookCount <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return LibraryPolicy.FINE_PER_DAY
                .multiply(BigDecimal.valueOf((long) overdueDays * bookCount))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Tính tiền bồi thường giá trị sách bị mất (200% giá bìa theo BR-08).
     *
     * @param coverPrice Giá bìa cuốn sách
     * @return Số tiền đền bù mất sách (200% giá bìa)
     */
    public BigDecimal calculateBookLossAmount(BigDecimal coverPrice) {
        BigDecimal price = (coverPrice != null && coverPrice.compareTo(BigDecimal.ZERO) > 0)
                ? coverPrice : DEFAULT_COVER_PRICE;
        return price.multiply(LibraryPolicy.COMPENSATION_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Tính toán chi tiết nghĩa vụ tài chính khi hoàn trả hoặc báo mất/hỏng sách (BR-06 & BR-08).
     *
     * @param coverPrice Giá bìa cuốn sách
     * @param dueDate    Hạn trả của phiếu mượn
     * @param actualDate Thời điểm thực trả hoặc báo mất
     * @param isLost     Cờ đánh dấu mất sách (200% giá bìa + 20.000đ phí)
     * @param isDamaged  Cờ đánh dấu hỏng nhẹ / rách bìa (20.000đ phí)
     * @return Bảng chi tiết tính toán nghĩa vụ tài chính (CompensationBreakdown)
     */
    public CompensationBreakdown calculateCompensation(
            BigDecimal coverPrice,
            LocalDateTime dueDate,
            LocalDateTime actualDate,
            boolean isLost,
            boolean isDamaged
    ) {
        int overdueDays = calculateOverdueDays(dueDate, actualDate);
        BigDecimal overdueFine = calculateOverdueFine(overdueDays);

        BigDecimal effectiveCoverPrice = (coverPrice != null && coverPrice.compareTo(BigDecimal.ZERO) > 0)
                ? coverPrice.setScale(2, RoundingMode.HALF_UP) : DEFAULT_COVER_PRICE;

        BigDecimal bookLossAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal techFee = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        if (isLost) {
            bookLossAmount = calculateBookLossAmount(effectiveCoverPrice);
            techFee = LibraryPolicy.TECHNICAL_FEE.setScale(2, RoundingMode.HALF_UP);
        } else if (isDamaged) {
            techFee = LibraryPolicy.TECHNICAL_FEE.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal totalAmount = bookLossAmount.add(techFee).add(overdueFine).setScale(2, RoundingMode.HALF_UP);

        return new CompensationBreakdown(
                effectiveCoverPrice,
                bookLossAmount,
                techFee,
                overdueDays,
                overdueFine,
                totalAmount
        );
    }

    /**
     * Lớp dữ liệu chứa bảng phân rã chi tiết nghĩa vụ tài chính khi trả hoặc bồi hoàn sách.
     */
    public static class CompensationBreakdown {
        private final BigDecimal coverPrice;
        private final BigDecimal bookLossAmount;
        private final BigDecimal techFee;
        private final int overdueDays;
        private final BigDecimal overdueFine;
        private final BigDecimal totalAmount;

        public CompensationBreakdown(
                BigDecimal coverPrice,
                BigDecimal bookLossAmount,
                BigDecimal techFee,
                int overdueDays,
                BigDecimal overdueFine,
                BigDecimal totalAmount
        ) {
            this.coverPrice = coverPrice;
            this.bookLossAmount = bookLossAmount;
            this.techFee = techFee;
            this.overdueDays = overdueDays;
            this.overdueFine = overdueFine;
            this.totalAmount = totalAmount;
        }

        public BigDecimal getCoverPrice() {
            return coverPrice;
        }

        public BigDecimal getBookLossAmount() {
            return bookLossAmount;
        }

        public BigDecimal getTechFee() {
            return techFee;
        }

        public int getOverdueDays() {
            return overdueDays;
        }

        public BigDecimal getOverdueFine() {
            return overdueFine;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }
    }
}
