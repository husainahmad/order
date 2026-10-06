package com.harmoni.pos.order.http.controller;

import com.harmoni.pos.order.business.service.ReportBusinessService;
import com.harmoni.pos.order.business.service.menu.UserService;
import com.harmoni.pos.order.model.DailyReport;
import com.harmoni.pos.order.model.DailyProductReport;
import com.harmoni.pos.order.model.OrderVolumeReport;
import com.harmoni.pos.order.model.ProductSalesReport;
import com.harmoni.pos.order.model.SettlementReport;
import com.harmoni.pos.order.model.TopProductReport;
import com.harmoni.pos.order.model.dto.StoreDto;
import com.harmoni.pos.order.model.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private ReportBusinessService reportBusinessService;

    @Test
    void getSettlementReport_shouldReturn200() throws Exception {
        var storeDto = new StoreDto().setId(1);
        var userDto = new UserDto().setStore(storeDto).setStoreId(1);

        when(userService.getUserDetail("test-user")).thenReturn(userDto);

        var report = new SettlementReport()
                .setTotalOrders(10)
                .setTotalSales(BigDecimal.valueOf(1000));
        when(reportBusinessService.generateSettlementReport(anyInt(), any(), any()))
                .thenReturn(report);

        mockMvc.perform(get("/api/v1/reports/settlement")
                        .param("start", "2025-01-01T00:00:00")
                        .param("end", "2025-01-02T00:00:00")
                        .header("X-Username", "test-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalOrders").value(10))
                .andExpect(jsonPath("$.data.totalSales").value(1000));
    }

    @Test
    void getTopProducts_shouldReturn200() throws Exception {
        var storeDto = new StoreDto().setId(1);
        var userDto = new UserDto().setStore(storeDto).setStoreId(1);

        when(userService.getUserDetail("test-user")).thenReturn(userDto);

        var topProducts = List.of(
                new TopProductReport() {{ setProductId(1); setQuantitySold(50); }},
                new TopProductReport() {{ setProductId(2); setQuantitySold(30); }}
        );
        when(reportBusinessService.getTopProducts(anyInt(), any(), any(), anyInt()))
                .thenReturn(topProducts);

        mockMvc.perform(get("/api/v1/reports/top-products")
                        .param("start", "2025-01-01T00:00:00")
                        .param("end", "2025-01-02T00:00:00")
                        .param("limit", "10")
                        .header("X-Username", "test-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].productId").value(1))
                .andExpect(jsonPath("$.data[1].productId").value(2));
    }

    @Test
    void getDailyReport_shouldReturn200() throws Exception {
        var storeDto = new StoreDto().setId(1);
        var userDto = new UserDto().setStore(storeDto).setStoreId(1);

        when(userService.getUserDetail("test-user")).thenReturn(userDto);

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
        when(reportBusinessService.getDailyReport(anyInt(), any(), any()))
                .thenReturn(List.of(dailyReport));

        mockMvc.perform(get("/api/v1/reports/daily")
                        .param("start", "2025-01-01T00:00:00")
                        .param("end", "2025-01-02T00:00:00")
                        .header("X-Username", "test-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].date").value("2025-01-01"))
                .andExpect(jsonPath("$.data[0].products[0].productName").value("Coffee"))
                .andExpect(jsonPath("$.data[0].products[0].quantity").value(10))
                .andExpect(jsonPath("$.data[0].products[0].netSales").value(150))
                .andExpect(jsonPath("$.data[0].products[0].discount").value(10));
    }

    @Test
    void getSalesReport_shouldReturn200() throws Exception {
        var storeDto = new StoreDto().setId(1);
        var userDto = new UserDto().setStore(storeDto).setStoreId(1);

        when(userService.getUserDetail("test-user")).thenReturn(userDto);

        var salesReport = List.of(
                new ProductSalesReport()
                        .setCategoryId(1)
                        .setCategoryName("Beverages")
                        .setProductId(1)
                        .setProductName("Coffee")
                        .setQuantity(10)
                        .setGrossSales(BigDecimal.valueOf(150))
                        .setDiscount(BigDecimal.valueOf(10))
                        .setNetSales(BigDecimal.valueOf(140))
        );
        when(reportBusinessService.getSalesReport(anyInt(), any(), any()))
                .thenReturn(salesReport);

        mockMvc.perform(get("/api/v1/reports/sales")
                        .param("start", "2025-01-01T00:00:00")
                        .param("end", "2025-01-02T00:00:00")
                        .header("X-Username", "test-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].categoryId").value(1))
                .andExpect(jsonPath("$.data[0].categoryName").value("Beverages"))
                .andExpect(jsonPath("$.data[0].productId").value(1))
                .andExpect(jsonPath("$.data[0].productName").value("Coffee"))
                .andExpect(jsonPath("$.data[0].quantity").value(10))
                .andExpect(jsonPath("$.data[0].grossSales").value(150))
                .andExpect(jsonPath("$.data[0].netSales").value(140));
    }

    @Test
    void getOrderVolumeReport_shouldReturn200() throws Exception {
        var storeDto = new StoreDto().setId(1);
        var userDto = new UserDto().setStore(storeDto).setStoreId(1);

        when(userService.getUserDetail("test-user")).thenReturn(userDto);

        var orderVolumeReport = new OrderVolumeReport()
                .setTotalOrders(100)
                .setPeakTimeOrders(45)
                .setNonPeakTimeOrders(55);
        when(reportBusinessService.getOrderVolumeReport(anyInt(), any(), any()))
                .thenReturn(orderVolumeReport);

        mockMvc.perform(get("/api/v1/reports/order-volume")
                        .param("start", "2025-01-01T00:00:00")
                        .param("end", "2025-01-02T00:00:00")
                        .header("X-Username", "test-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalOrders").value(100))
                .andExpect(jsonPath("$.data.peakTimeOrders").value(45))
                .andExpect(jsonPath("$.data.nonPeakTimeOrders").value(55));
    }
}
