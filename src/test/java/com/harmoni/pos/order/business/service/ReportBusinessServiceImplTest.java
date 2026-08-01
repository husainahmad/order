package com.harmoni.pos.order.business.service;

import com.harmoni.pos.order.mapper.ReportMapper;
import com.harmoni.pos.order.model.PaymentSummary;
import com.harmoni.pos.order.model.SettlementReport;
import com.harmoni.pos.order.model.TopProductReport;
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
class ReportBusinessServiceImplTest {

    @Mock
    private ReportMapper reportMapper;

    @InjectMocks
    private ReportBusinessServiceImpl reportBusinessService;

    @Test
    void generateSettlementReport_shouldReturnReportWithPaymentBreakdown() {
        var storeId = 1;
        var start = LocalDateTime.of(2025, 1, 1, 0, 0);
        var end = LocalDateTime.of(2025, 1, 2, 0, 0);
        var report = new SettlementReport()
                .setTotalOrders(5)
                .setTotalSales(BigDecimal.valueOf(1000));
        var payments = List.of(
                new PaymentSummary().setPaymentType("CASH").setTotalAmount(BigDecimal.valueOf(700)),
                new PaymentSummary().setPaymentType("CARD").setTotalAmount(BigDecimal.valueOf(300))
        );

        when(reportMapper.getSettlementReport(start, end)).thenReturn(report);
        when(reportMapper.getPaymentBreakdown(start, end)).thenReturn(payments);

        SettlementReport result = reportBusinessService.generateSettlementReport(storeId, start, end);

        assertEquals(5, result.getTotalOrders());
        assertEquals(BigDecimal.valueOf(1000), result.getTotalSales());
        assertEquals(Map.of("CASH", BigDecimal.valueOf(700), "CARD", BigDecimal.valueOf(300)),
                result.getPaymentBreakdown());
    }

    @Test
    void getTopProducts_shouldReturnOrderedList() {
        var storeId = 1;
        var start = LocalDateTime.of(2025, 1, 1, 0, 0);
        var end = LocalDateTime.of(2025, 1, 2, 0, 0);
        var limit = 10;
        var p1 = new TopProductReport();
        p1.setProductId(1);
        p1.setQuantitySold(50);
        var p2 = new TopProductReport();
        p2.setProductId(2);
        p2.setQuantitySold(30);
        var topProducts = List.of(p1, p2);

        when(reportMapper.getTopProducts(storeId, start, end, limit)).thenReturn(topProducts);

        List<TopProductReport> result = reportBusinessService.getTopProducts(storeId, start, end, limit);

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getProductId().intValue());
        assertEquals(50, result.get(0).getQuantitySold().intValue());
    }
}
