package com.harmoni.pos.order.service;

import com.harmoni.pos.order.mapper.ReportMapper;
import com.harmoni.pos.order.model.PaymentSummary;
import com.harmoni.pos.order.model.SettlementReport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ReportMapper reportMapper;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Test
    void generateSettlementReport_whenReportExists_shouldReturnWithPaymentBreakdown() {
        var start = LocalDateTime.of(2025, 1, 1, 0, 0);
        var end = LocalDateTime.of(2025, 1, 2, 0, 0);
        var report = new SettlementReport()
                .setTotalOrders(10)
                .setTotalSales(BigDecimal.valueOf(1000));
        var payments = List.of(
                new PaymentSummary().setPaymentType("CASH").setTotalAmount(BigDecimal.valueOf(600)),
                new PaymentSummary().setPaymentType("CARD").setTotalAmount(BigDecimal.valueOf(400))
        );
        when(reportMapper.getSettlementReport(start, end)).thenReturn(report);
        when(reportMapper.getPaymentBreakdown(start, end)).thenReturn(payments);

        SettlementReport result = reportService.generateSettlementReport(start, end);

        assertEquals(10, result.getTotalOrders());
        assertEquals(BigDecimal.valueOf(1000), result.getTotalSales());
        assertEquals(Map.of("CASH", BigDecimal.valueOf(600), "CARD", BigDecimal.valueOf(400)),
                result.getPaymentBreakdown());
    }

    @Test
    void generateSettlementReport_whenReportIsNull_shouldCreateNewReport() {
        var start = LocalDateTime.of(2025, 1, 1, 0, 0);
        var end = LocalDateTime.of(2025, 1, 2, 0, 0);
        when(reportMapper.getSettlementReport(start, end)).thenReturn(null);
        when(reportMapper.getPaymentBreakdown(start, end)).thenReturn(List.of());

        SettlementReport result = reportService.generateSettlementReport(start, end);

        assertNotNull(result);
        assertEquals(0, result.getTotalOrders());
        assertNotNull(result.getPaymentBreakdown());
        assertTrue(result.getPaymentBreakdown().isEmpty());
    }

    @Test
    void generateSettlementReport_whenPaymentsNull_shouldReturnEmptyBreakdown() {
        var start = LocalDateTime.of(2025, 1, 1, 0, 0);
        var end = LocalDateTime.of(2025, 1, 2, 0, 0);
        when(reportMapper.getSettlementReport(start, end)).thenReturn(new SettlementReport());
        when(reportMapper.getPaymentBreakdown(start, end)).thenReturn(null);

        SettlementReport result = reportService.generateSettlementReport(start, end);

        assertNotNull(result.getPaymentBreakdown());
        assertTrue(result.getPaymentBreakdown().isEmpty());
    }

    @Test
    void generateSettlementReport_whenStartAfterEnd_shouldThrowIllegalArgumentException() {
        var start = LocalDateTime.of(2025, 1, 2, 0, 0);
        var end = LocalDateTime.of(2025, 1, 1, 0, 0);

        assertThrows(IllegalArgumentException.class,
                () -> reportService.generateSettlementReport(start, end));
    }
}
