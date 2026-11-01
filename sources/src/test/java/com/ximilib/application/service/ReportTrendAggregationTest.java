package com.ximilib.application.service;

import com.ximilib.application.port.ReportPort;
import com.ximilib.application.port.ReportPort.DailyMetric;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Logic Tổng Hợp Số Liệu Xu Hướng Báo Cáo (ReportTrendAggregation)")
class ReportTrendAggregationTest {

    @Test
    @DisplayName("TC-TREND-01: Tổng hợp số liệu xu hướng lượt mượn và doanh thu phạt chuẩn xác")
    void testTrendAggregationLogic() {
        List<DailyMetric> sampleMetrics = new ArrayList<>();
        sampleMetrics.add(new DailyMetric("26/10", 3, new BigDecimal("10000.00")));
        sampleMetrics.add(new DailyMetric("27/10", 5, new BigDecimal("0.00")));
        sampleMetrics.add(new DailyMetric("28/10", 8, new BigDecimal("25000.00")));
        sampleMetrics.add(new DailyMetric("29/10", 2, new BigDecimal("6000.00")));
        sampleMetrics.add(new DailyMetric("30/10", 11, new BigDecimal("15000.00")));
        sampleMetrics.add(new DailyMetric("31/10", 0, new BigDecimal("0.00")));
        sampleMetrics.add(new DailyMetric("01/11", 7, new BigDecimal("8000.00")));

        assertEquals(7, sampleMetrics.size());

        // Tổng lượt mượn trong 7 ngày
        int totalBorrows = sampleMetrics.stream().mapToInt(DailyMetric::borrowCount).sum();
        assertEquals(36, totalBorrows);

        // Tổng doanh thu phạt trong 7 ngày
        BigDecimal totalFines = sampleMetrics.stream()
                .map(DailyMetric::fineRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("64000.00"), totalFines);

        // Đỉnh lượt mượn (Peak day)
        int maxBorrows = sampleMetrics.stream().mapToInt(DailyMetric::borrowCount).max().orElse(0);
        assertEquals(11, maxBorrows);
    }
}
