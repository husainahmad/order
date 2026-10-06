package com.harmoni.pos.order.http.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmoni.pos.order.business.service.OrderPaymentBusinessService;
import com.harmoni.pos.order.model.Order;
import com.harmoni.pos.order.model.OrderPayment;
import com.harmoni.pos.order.model.OrderStatus;
import com.harmoni.pos.order.model.dto.OrderPaymentDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderPaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderPaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderPaymentBusinessService orderPaymentBusinessService;

    @Test
    void payOrder_shouldReturn201() throws Exception {
        var paymentDto = new OrderPaymentDto();
        paymentDto.setOrderId(1);
        paymentDto.setPaymentId(2);

        var orderPayment = new OrderPayment()
                .setId(1)
                .setOrderId(1)
                .setPaymentId(2)
                .setAmount(BigDecimal.valueOf(200));

        var order = new Order()
                .setId(1)
                .setStatus(OrderStatus.PAID)
                .setGrandTotal(BigDecimal.valueOf(200))
                .setOrderPayment(orderPayment);

        when(orderPaymentBusinessService.processPayment(any(OrderPaymentDto.class), anyString()))
                .thenReturn(order);

        mockMvc.perform(put("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Username", "test-user")
                        .content(objectMapper.writeValueAsString(paymentDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("PAID"))
                .andExpect(jsonPath("$.data.orderPayment.id").value(1))
                .andExpect(jsonPath("$.data.orderPayment.amount").value(200));
    }

    @Test
    void payOrder_whenMalformedJson_shouldReturn400() throws Exception {
        mockMvc.perform(put("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Username", "test-user")
                        .content("not a valid json"))
                .andExpect(status().isBadRequest());
    }
}
