package com.harmoni.pos.order.http.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmoni.pos.order.business.service.OrderBusinessService;
import com.harmoni.pos.order.component.CallerScope;
import com.harmoni.pos.order.component.CallerScopeContext;
import com.harmoni.pos.order.exception.BusinessBadRequestException;
import com.harmoni.pos.order.exception.ScopeAccessDeniedException;
import com.harmoni.pos.order.model.Order;
import com.harmoni.pos.order.model.OrderDetail;
import com.harmoni.pos.order.model.OrderPayment;
import com.harmoni.pos.order.model.OrderStatus;
import com.harmoni.pos.order.model.dto.OrderDto;
import com.harmoni.pos.order.model.dto.OrderDetailDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderBusinessService orderBusinessService;

    @Test
    void createOrder_shouldReturn201() throws Exception {
        var orderDetailDto = new OrderDetailDto();
        orderDetailDto.setProductId(1);
        orderDetailDto.setOrderDetailSkus(List.of());

        var orderDto = new OrderDto();
        orderDto.setCustomerId(1);
        orderDto.setOrderDetailDtos(List.of(orderDetailDto));

        var order = new Order()
                .setId(1)
                .setOrderNo("ORD1-20250101000000-ABC123")
                .setStatus(OrderStatus.CONFIRMED);

        when(orderBusinessService.confirm(any(OrderDto.class), anyString(), any())).thenReturn(order);

        mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Username", "test-user")
                        .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.orderNo").value("ORD1-20250101000000-ABC123"))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }

    @Test
    void createOrder_whenIdempotencyKeyHeaderPresent_shouldForwardKey() throws Exception {
        var orderDetailDto = new OrderDetailDto();
        orderDetailDto.setProductId(1);
        orderDetailDto.setOrderDetailSkus(List.of());

        var orderDto = new OrderDto();
        orderDto.setCustomerId(1);
        orderDto.setOrderDetailDtos(List.of(orderDetailDto));

        var order = new Order()
                .setId(1)
                .setOrderNo("ORD1-20250101000000-ABC123")
                .setStatus(OrderStatus.CONFIRMED);

        when(orderBusinessService.confirm(any(OrderDto.class), anyString(), any())).thenReturn(order);

        mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Username", "test-user")
                        .header("Idempotency-Key", "abc-123")
                        .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1));

        org.mockito.Mockito.verify(orderBusinessService)
                .confirm(any(OrderDto.class), anyString(), org.mockito.ArgumentMatchers.eq("abc-123"));
    }

    @Test
    void printOrder_shouldReturn200() throws Exception {
        var orderDetailDto = new OrderDetailDto();
        orderDetailDto.setProductId(1);
        orderDetailDto.setOrderDetailSkus(List.of());

        var orderDto = new OrderDto();
        orderDto.setCustomerId(1);
        orderDto.setOrderDetailDtos(List.of(orderDetailDto));

        var order = new Order()
                .setId(1)
                .setOrderNo("ORD1-20250101000000-ABC123");

        when(orderBusinessService.print(any(OrderDto.class), anyString())).thenReturn(order);

        mockMvc.perform(post("/api/v1/order/print")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Username", "test-user")
                        .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void getListOrder_shouldReturn201() throws Exception {
        var orders = List.of(
                new Order().setId(1).setOrderNo("ORD1-001"),
                new Order().setId(2).setOrderNo("ORD1-002")
        );

        when(orderBusinessService.getDailyListByStore(anyString())).thenReturn(orders);

        mockMvc.perform(get("/api/v1/order")
                        .header("X-Username", "test-user"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[1].id").value(2));
    }

    @Test
    void voidOrder_shouldReturn200() throws Exception {
        var voidedOrder = new Order()
                .setId(1)
                .setStatus(OrderStatus.VOID);

        when(orderBusinessService.voidOrder(any(), anyString())).thenReturn(voidedOrder);

        mockMvc.perform(put("/api/v1/order/1/void")
                        .header("X-Username", "test-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("VOID"));
    }

    @Test
    void createOrder_whenMalformedJson_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Username", "test-user")
                        .content("not a valid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOrderById_shouldReturnFullOrder() throws Exception {
        CallerScopeContext.set(CallerScope.of(Set.of(5)));
        try {
            Order order = new Order()
                    .setId(7)
                    .setStoreId(5)
                    .setOrderNo("ORD5-001")
                    .setStatus(OrderStatus.CONFIRMED)
                    .setOrderDetails(List.of(new OrderDetail().setId(11).setProductId(9)
                            .setProductName("Burger").setAmount(new BigDecimal("5.00"))))
                    .setOrderPayment(new OrderPayment().setId(3).setPaymentId(2)
                            .setAmount(new BigDecimal("5.00")));
            when(orderBusinessService.getOrderById(eq(7), any())).thenReturn(order);

            mockMvc.perform(get("/api/v1/order/7"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(7))
                    .andExpect(jsonPath("$.data.orderDetails[0].productName").value("Burger"))
                    .andExpect(jsonPath("$.data.orderPayment.amount").value(5.00));
            verify(orderBusinessService).getOrderById(eq(7),
                    argThat(scope -> !scope.isGlobal() && scope.getStoreIds().equals(Set.of(5))));
        } finally {
            CallerScopeContext.clear();
        }
    }

    @Test
    void getOrderById_shouldReturnBadRequestForNonPositiveId() throws Exception {
        when(orderBusinessService.getOrderById(eq(0), any()))
                .thenThrow(new BusinessBadRequestException("exception.order.id.badRequest.invalid", null));

        mockMvc.perform(get("/api/v1/order/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request, order id must be positive"));
    }

    @Test
    void getOrderById_shouldReturnForbiddenWhenOutsideScope() throws Exception {
        when(orderBusinessService.getOrderById(eq(8), any()))
                .thenThrow(new ScopeAccessDeniedException("Order is outside the caller's scope"));

        mockMvc.perform(get("/api/v1/order/8"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Order is outside the caller's scope"));
    }
}
