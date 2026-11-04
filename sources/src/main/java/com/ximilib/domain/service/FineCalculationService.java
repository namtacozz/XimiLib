package com.ximilib.domain.service;

import com.ximilib.domain.model.LibraryPolicy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class FineCalculationService {

    private static final BigDecimal DEFAULT_COVER_PRICE = new BigDecimal("50000.00");

    public int calculateOverdueDays(LocalDateTime dueDate, LocalDateTime actualDate) {
        if (dueDate == null || actualDate == null || !actualDate.isAfter(dueDate)) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(dueDate.toLocalDate(), actualDate.toLocalDate());
        return (int) Math.max(1, days);
    }

    public BigDecimal calculateOverdueFine(int overdueDays) {
        return calculateOverdueFine(overdueDays, 1);
    }

    public BigDecimal calculateOverdueFine(int overdueDays, int bookCount) {
        if (overdueDays <= 0 || bookCount <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return LibraryPolicy.FINE_PER_DAY
                .multiply(BigDecimal.valueOf((long) overdueDays * bookCount))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateBookLossAmount(BigDecimal coverPrice) {
        BigDecimal price = (coverPrice != null && coverPrice.compareTo(BigDecimal.ZERO) > 0)
                ? coverPrice : DEFAULT_COVER_PRICE;
        return price.multiply(LibraryPolicy.COMPENSATION_RATE).setScale(2, RoundingMode.HALF_UP);
    }

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
