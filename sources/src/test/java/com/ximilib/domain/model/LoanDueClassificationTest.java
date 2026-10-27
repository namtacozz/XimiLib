package com.ximilib.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Test: LoanDueClassification")
class LoanDueClassificationTest {

    private final LocalDate today = LocalDate.of(2026, 10, 27);

    @Test
    @DisplayName("Hạn trả trước hôm nay -> OVERDUE với số ngày trễ chính xác")
    void testOverdue() {
        LocalDate due = today.minusDays(5);
        LoanDueClassification c = LoanDueClassification.classify(due, today);

        assertTrue(c.isOverdue());
        assertFalse(c.isDueToday());
        assertEquals(-5, c.daysRemaining());
        assertEquals("Quá hạn 5 ngày", c.displayText());
    }

    @Test
    @DisplayName("Hạn trả hôm nay -> DUE_TODAY")
    void testDueToday() {
        LoanDueClassification c = LoanDueClassification.classify(today, today);

        assertFalse(c.isOverdue());
        assertTrue(c.isDueToday());
        assertEquals(0, c.daysRemaining());
        assertEquals("Hạn hôm nay", c.displayText());
    }

    @Test
    @DisplayName("Hạn trả trong 1 ngày tới -> DUE_SOON")
    void testDueSoonTomorrow() {
        LocalDate due = today.plusDays(1);
        LoanDueClassification c = LoanDueClassification.classify(due, today);

        assertFalse(c.isOverdue());
        assertFalse(c.isDueToday());
        assertEquals(1, c.daysRemaining());
        assertEquals("Còn 1 ngày", c.displayText());
    }

    @Test
    @DisplayName("Hạn trả trong 2 ngày tới -> DUE_SOON")
    void testDueSoonTwoDays() {
        LocalDate due = today.plusDays(2);
        LoanDueClassification c = LoanDueClassification.classify(due, today);

        assertFalse(c.isOverdue());
        assertEquals(2, c.daysRemaining());
        assertEquals("Còn 2 ngày", c.displayText());
    }

    @Test
    @DisplayName("Hạn trả trên 2 ngày -> NORMAL")
    void testNormal() {
        LocalDate due = today.plusDays(10);
        LoanDueClassification c = LoanDueClassification.classify(due, today);

        assertFalse(c.isOverdue());
        assertEquals(10, c.daysRemaining());
        assertEquals("Còn 10 ngày", c.displayText());
    }

    @Test
    @DisplayName("Hạn trả null -> an toàn trả về DUE_TODAY không quăng NullPointerException")
    void testNullDueDate() {
        LoanDueClassification c = LoanDueClassification.classify(null, today);
        assertNotNull(c);
        assertTrue(c.isDueToday());
    }
}
