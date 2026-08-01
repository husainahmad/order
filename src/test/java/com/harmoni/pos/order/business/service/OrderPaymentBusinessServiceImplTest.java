package com.harmoni.pos.order.business.service;

import com.harmoni.pos.order.business.service.menu.UserService;
import com.harmoni.pos.order.exception.BusinessUnAuthorizedRequestException;
import com.harmoni.pos.order.model.Order;
import com.harmoni.pos.order.model.OrderPayment;
import com.harmoni.pos.order.model.OrderStatus;
import com.harmoni.pos.order.model.dto.OrderPaymentDto;
import com.harmoni.pos.order.model.dto.StoreDto;
import com.harmoni.pos.order.model.dto.UserDto;
import com.harmoni.pos.order.service.OrderPaymentService;
import com.harmoni.pos.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderPaymentBusinessServiceImplTest {

    @Mock
    private OrderService orderService;

    @Mock
    private OrderPaymentService orderPaymentService;

    @Mock
    private UserService userService;

    @InjectMocks
    private OrderPaymentBusinessServiceImpl orderPaymentBusinessService;

    @Test
    void processPayment_whenStoreMatches_shouldUpdateOrderAndCreatePayment() {
        var username = "test-user";
        var paymentDto = new OrderPaymentDto();
        paymentDto.setOrderId(1);
        paymentDto.setPaymentId(2);

        var order = new Order()
                .setId(1)
                .setStoreId(5)
                .setGrandTotal(BigDecimal.valueOf(200))
                .setStatus(OrderStatus.CONFIRMED);
        var userDto = new UserDto()
                .setStore(new StoreDto().setId(5));

        when(orderService.selectByPrimaryKey(1)).thenReturn(order);
        when(userService.getUserDetail(username)).thenReturn(userDto);
        when(orderService.updateByPrimaryKey(order)).thenReturn(1);
        when(orderPaymentService.insert(any(OrderPayment.class))).thenReturn(1);

        Order result = orderPaymentBusinessService.processPayment(paymentDto, username);

        assertEquals(OrderStatus.PAID, result.getStatus());
        assertNotNull(result.getUpdatedAt());
        assertNotNull(result.getOrderPayment());
        assertEquals(BigDecimal.valueOf(200), result.getOrderPayment().getAmount());
        assertEquals(1, result.getOrderPayment().getOrderId().intValue());
        assertEquals(2, result.getOrderPayment().getPaymentId().intValue());
        verify(orderService).updateByPrimaryKey(order);
        verify(orderPaymentService).insert(any(OrderPayment.class));
    }

    @Test
    void processPayment_whenStoreMismatch_shouldThrowUnauthorized() {
        var username = "test-user";
        var paymentDto = new OrderPaymentDto();
        paymentDto.setOrderId(1);

        var order = new Order()
                .setId(1)
                .setStoreId(5);
        var userDto = new UserDto()
                .setStore(new StoreDto().setId(99));

        when(orderService.selectByPrimaryKey(1)).thenReturn(order);
        when(userService.getUserDetail(username)).thenReturn(userDto);

        assertThrows(BusinessUnAuthorizedRequestException.class,
                () -> orderPaymentBusinessService.processPayment(paymentDto, username));

        verify(orderService, never()).updateByPrimaryKey(any());
        verify(orderPaymentService, never()).insert(any());
    }
}
