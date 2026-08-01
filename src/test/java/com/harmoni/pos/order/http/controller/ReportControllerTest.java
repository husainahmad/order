package com.harmoni.pos.order.http.controller;

import com.harmoni.pos.order.business.service.ReportBusinessService;
import com.harmoni.pos.order.business.service.menu.UserService;
import com.harmoni.pos.order.component.JwtUtil;
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

    @MockBean
    private JwtUtil jwtUtil;

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
}
