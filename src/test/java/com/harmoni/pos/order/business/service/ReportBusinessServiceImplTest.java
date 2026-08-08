package com.harmoni.pos.order.business.service;

import com.harmoni.pos.order.mapper.ReportMapper;
import com.harmoni.pos.order.model.DailyReport;
import com.harmoni.pos.order.model.DailyProductReport;
import com.harmoni.pos.order.model.OrderVolumeReport;
import com.harmoni.pos.order.model.PaymentSummary;
import com.harmoni.pos.order.model.ProductSalesReport;
import com.harmoni.pos.order.model.SettlementReport;
import com.harmoni.pos.order.model.TopProductReport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @Test
    void getDailyReport_shouldReturnListGroupedByDay() {
        var storeId = 1;
        var start = LocalDateTime.of(2025, 1, 1, 0, 0);
        var end = LocalDateTime.of(2025, 1, 3, 0, 0);
        var dailyReport = new DailyReport();
        dailyReport.setDate(LocalDate.of(2025, 1, 1));
        dailyReport.setProducts(List.of(
                new DailyProductReport()
                        .setProductId(1)
                        .setProductName("Coffee")
                        .setQuantity(10)
                        .setNetSales(BigDecimal.valueOf(150))
                        .setDiscount(BigDecimal.valueOf(10))
        ));
        when(reportMapper.getDailyReport(storeId, start, end)).thenReturn(List.of(dailyReport));

        List<DailyReport> result = reportBusinessService.getDailyReport(storeId, start, end);

        assertEquals(1, result.size());
        assertEquals(LocalDate.of(2025, 1, 1), result.get(0).getDate());
        assertEquals(1, result.get(0).getProducts().get(0).getProductId().intValue());
        assertEquals("Coffee", result.get(0).getProducts().get(0).getProductName());
        assertEquals(10, result.get(0).getProducts().get(0).getQuantity().intValue());
        assertEquals(BigDecimal.valueOf(150), result.get(0).getProducts().get(0).getNetSales());
        assertEquals(BigDecimal.valueOf(10), result.get(0).getProducts().get(0).getDiscount());
    }

    @Test
    void getSalesReport_shouldReturnListGroupedByProductAndCategory() {
        var storeId = 1;
        var start = LocalDateTime.of(2025, 1, 1, 0, 0);
        var end = LocalDateTime.of(2025, 1, 2, 0, 0);
        var salesReport = new ProductSalesReport()
                .setCategoryId(1)
                .setCategoryName("Beverages")
                .setProductId(1)
                .setProductName("Coffee")
                .setQuantity(10)
                .setGrossSales(BigDecimal.valueOf(150))
                .setDiscount(BigDecimal.valueOf(10))
                .setNetSales(BigDecimal.valueOf(140));
        when(reportMapper.getSalesReport(storeId, start, end)).thenReturn(List.of(salesReport));

        List<ProductSalesReport> result = reportBusinessService.getSalesReport(storeId, start, end);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getCategoryId().intValue());
        assertEquals("Beverages", result.get(0).getCategoryName());
        assertEquals(1, result.get(0).getProductId().intValue());
        assertEquals("Coffee", result.get(0).getProductName());
        assertEquals(10, result.get(0).getQuantity().intValue());
        assertEquals(BigDecimal.valueOf(150), result.get(0).getGrossSales());
        assertEquals(BigDecimal.valueOf(10), result.get(0).getDiscount());
        assertEquals(BigDecimal.valueOf(140), result.get(0).getNetSales());
    }

    @Test
    void getOrderVolumeReport_shouldReturnReportWithNonPeakComputed() {
        var storeId = 1;
        var start = LocalDateTime.of(2025, 1, 1, 0, 0);
        var end = LocalDateTime.of(2025, 1, 2, 0, 0);
        var report = new OrderVolumeReport()
                .setTotalOrders(100)
                .setPeakTimeOrders(45);
        when(reportMapper.getOrderVolumeReport(storeId, start, end)).thenReturn(report);

        OrderVolumeReport result = reportBusinessService.getOrderVolumeReport(storeId, start, end);

        assertEquals(100, result.getTotalOrders());
        assertEquals(45, result.getPeakTimeOrders());
        assertEquals(55, result.getNonPeakTimeOrders());
    }

    @Test
    void getOrderVolumeReport_whenMapperReturnsNull_shouldReturnZeroedReport() {
        var storeId = 1;
        var start = LocalDateTime.of(2025, 1, 1, 0, 0);
        var end = LocalDateTime.of(2025, 1, 2, 0, 0);
        when(reportMapper.getOrderVolumeReport(storeId, start, end)).thenReturn(null);

        OrderVolumeReport result = reportBusinessService.getOrderVolumeReport(storeId, start, end);

        assertNotNull(result);
        assertEquals(0, result.getTotalOrders());
        assertEquals(0, result.getPeakTimeOrders());
        assertEquals(0, result.getNonPeakTimeOrders());
    }
}
