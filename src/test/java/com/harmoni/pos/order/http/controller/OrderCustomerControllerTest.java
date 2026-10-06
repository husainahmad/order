package com.harmoni.pos.order.http.controller;

import com.harmoni.pos.order.model.OrderCustomer;
import com.harmoni.pos.order.service.OrderCustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderCustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderCustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderCustomerService orderCustomerService;

    @Test
    void getByOrderId_shouldReturnTheCustomerAsItWasAtTheTimeOfTheSale() throws Exception {
        when(orderCustomerService.getByOrderId(500)).thenReturn(new OrderCustomer()
                .setOrderId(500)
                .setCustomerId(77L)
                .setCustomerName("Ada")
                .setCustomerPhone("0400123456"));

        mockMvc.perform(get("/api/v1/order-customers/order/500")
                        .header("X-Username", "test-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.customerId").value(77))
                .andExpect(jsonPath("$.data.customerName").value("Ada"))
                .andExpect(jsonPath("$.data.customerPhone").value("0400123456"));
    }

    @Test
    void getByOrderId_shouldReturn200WithNoDataForAWalkInSale() throws Exception {
        when(orderCustomerService.getByOrderId(any())).thenReturn(null);

        mockMvc.perform(get("/api/v1/order-customers/order/500")
                        .header("X-Username", "test-user"))
                .andExpect(status().isOk());
    }

    @Test
    void getHistory_shouldReturnTheCustomersOrders() throws Exception {
        when(orderCustomerService.getHistory(eq(77L), any(), any())).thenReturn(List.of(
                new OrderCustomer().setOrderId(500).setCustomerId(77L).setCustomerName("Ada"),
                new OrderCustomer().setOrderId(400).setCustomerId(77L).setCustomerName("Ada")));

        mockMvc.perform(get("/api/v1/order-customers/customer/77/history")
                        .header("X-Username", "test-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].orderId").value(500));
    }

    @Test
    void getHistory_shouldAcceptADateRange() throws Exception {
        when(orderCustomerService.getHistory(any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/order-customers/customer/77/history")
                        .param("from", "2026-01-01T00:00:00")
                        .param("to", "2026-01-31T23:59:59")
                        .header("X-Username", "test-user"))
                .andExpect(status().isOk());

        verify(orderCustomerService).getHistory(eq(77L), any(Date.class), any(Date.class));
    }

    @Test
    void getHistory_shouldOmitTheRangeEntirelyWhenNoBoundsAreGiven() throws Exception {
        when(orderCustomerService.getHistory(eq(77L), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/order-customers/customer/77/history")
                        .header("X-Username", "test-user"))
                .andExpect(status().isOk());

        verify(orderCustomerService).getHistory(77L, null, null);
    }

    /**
     * Snapshots are taken during confirmation, so there is no way to post a customer in.
     * The status is 5xx rather than 404 because the application's catch-all exception
     * handler does not map an unmapped route; what matters is that nothing reaches the
     * service, so a client cannot attach a customer to an order that has none.
     */
    @Test
    void shouldExposeNoEndpointThatWritesACustomer() throws Exception {
        mockMvc.perform(post("/api/v1/order-customers/snapshot")
                        .header("X-Username", "test-user"))
                .andExpect(status().is5xxServerError());

        verifyNoInteractions(orderCustomerService);
    }
}
