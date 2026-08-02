package com.harmoni.pos.order.business.service;

import com.harmoni.pos.order.business.service.menu.UserService;
import com.harmoni.pos.order.model.Order;
import com.harmoni.pos.order.model.OrderDetail;
import com.harmoni.pos.order.model.OrderStatus;
import com.harmoni.pos.order.model.dto.OrderDto;
import com.harmoni.pos.order.model.dto.OrderDetailDto;
import com.harmoni.pos.order.model.dto.StoreDto;
import com.harmoni.pos.order.model.dto.UserDto;
import com.harmoni.pos.order.service.OrderDetailService;
import com.harmoni.pos.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderBusinessServiceImplTest {

    @Mock
    private OrderService orderService;

    @Mock
    private OrderDetailService orderDetailService;

    @Mock
    private UserService userService;

    @InjectMocks
    private OrderBusinessServiceImpl orderBusinessService;

    @Test
    void calculateSubtotal_shouldSumOrderDetailAmounts() {
        var order = new Order()
                .setOrderDetails(List.of(
                        new OrderDetail().setAmount(BigDecimal.valueOf(100)),
                        new OrderDetail().setAmount(BigDecimal.valueOf(50))
                ));

        orderBusinessService.calculateSubtotal(order);

        assertEquals(BigDecimal.valueOf(150), order.getSubTotal());
    }

    @Test
    void calculateSubtotal_whenAmountNull_shouldTreatAsZero() {
        var order = new Order()
                .setOrderDetails(List.of(
                        new OrderDetail().setAmount(null),
                        new OrderDetail().setAmount(BigDecimal.valueOf(50))
                ));

        orderBusinessService.calculateSubtotal(order);

        assertEquals(BigDecimal.valueOf(50), order.getSubTotal());
    }

    @Test
    void calculateServiceCharge_shouldSetZero() {
        var order = new Order();

        orderBusinessService.calculateServiceCharge(order);

        assertEquals(BigDecimal.ZERO, order.getServiceChargeTotal());
    }

    @Test
    void calculateDiscount_shouldSetDiscountAndRecalculateGrandTotal() {
        var order = new Order().setSubTotal(BigDecimal.valueOf(200));
        var orderDto = new OrderDto();
        orderDto.setDiscount(BigDecimal.valueOf(30));

        orderBusinessService.calculateDiscount(order, orderDto);

        assertEquals(BigDecimal.valueOf(30), order.getDiscountTotal());
        assertEquals(BigDecimal.valueOf(170), order.getGrandTotal());
    }

    @Test
    void calculateDiscount_whenNull_shouldSetZero() {
        var order = new Order().setSubTotal(BigDecimal.valueOf(200));
        var orderDto = new OrderDto();

        orderBusinessService.calculateDiscount(order, orderDto);

        assertEquals(BigDecimal.ZERO, order.getDiscountTotal());
    }

    @Test
    void calculateTax_shouldSetZero() {
        var order = new Order();
        var orderDto = new OrderDto();

        orderBusinessService.calculateTax(order, orderDto);

        assertEquals(BigDecimal.ZERO, order.getTaxTotal());
    }

    @Test
    void calculateGrandTotal_shouldComputeCorrectly() {
        var order = new Order()
                .setSubTotal(BigDecimal.valueOf(200))
                .setDiscountTotal(BigDecimal.valueOf(20))
                .setTaxTotal(BigDecimal.valueOf(10))
                .setServiceChargeTotal(BigDecimal.valueOf(5));

        orderBusinessService.calculateGrandTotal(order);

        assertEquals(BigDecimal.valueOf(195), order.getGrandTotal());
    }

    @Test
    void calculateGrandTotal_whenValuesNull_shouldTreatAsZero() {
        var order = new Order()
                .setSubTotal(BigDecimal.valueOf(100))
                .setDiscountTotal(null)
                .setTaxTotal(null)
                .setServiceChargeTotal(null);

        orderBusinessService.calculateGrandTotal(order);

        assertEquals(BigDecimal.valueOf(100), order.getGrandTotal());
    }

    @Test
    void calculate_shouldPopulateAndCalculateOrder() {
        var username = "test-user";
        var orderDto = new OrderDto();
        orderDto.setCustomerId(1);
        orderDto.setCustomerName("John");
        orderDto.setDiscount(BigDecimal.valueOf(10));
        orderDto.setOrderDetailDtos(List.of(new OrderDetailDto()));
        orderDto.setRemark("test");
        var userDto = new UserDto()
                .setStore(new StoreDto().setId(5));
        var orderDetails = List.of(
                new OrderDetail().setAmount(BigDecimal.valueOf(100))
        );

        when(userService.getUserDetail(username)).thenReturn(userDto);
        when(orderService.generateOrderNo(any())).thenReturn("ORD5-20250101000000-ABC123");
        when(orderDetailService.populateOrderDetail(any(), anyString())).thenReturn(orderDetails);

        Order result = orderBusinessService.calculate(orderDto, username);

        assertNotNull(result);
        assertEquals(-1, result.getCustomerId().intValue());
        assertEquals("John", result.getCustomerName());
        assertEquals(5, result.getStore().getId().intValue());
        assertNotNull(result.getOrderNo());
        assertEquals(BigDecimal.valueOf(100), result.getSubTotal());
        assertEquals(BigDecimal.valueOf(10), result.getDiscountTotal());
        assertEquals(BigDecimal.valueOf(90), result.getGrandTotal());
        assertEquals(BigDecimal.ZERO, result.getTaxTotal());
        assertEquals(BigDecimal.ZERO, result.getServiceChargeTotal());
    }

    @Test
    void confirm_shouldCalculatePersistAndReturnConfirmedOrder() {
        var username = "test-user";
        var orderDto = new OrderDto();
        orderDto.setCustomerId(1);
        orderDto.setOrderDetailDtos(List.of(new OrderDetailDto()));
        var userDto = new UserDto()
                .setStore(new StoreDto().setId(5));

        when(userService.getUserDetail(username)).thenReturn(userDto);
        when(orderService.generateOrderNo(any())).thenReturn("ORD5-20250101000000-ABC123");
        when(orderDetailService.populateOrderDetail(any(), anyString()))
                .thenReturn(List.of(new OrderDetail().setAmount(BigDecimal.valueOf(50))));
        when(orderService.insert(any())).thenReturn(1);

        Order result = orderBusinessService.confirm(orderDto, username);

        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        verify(orderService).insert(result);
    }

    @Test
    void print_shouldCalculateWithoutPersisting() {
        var username = "test-user";
        var orderDto = new OrderDto();
        orderDto.setCustomerId(1);
        orderDto.setOrderDetailDtos(List.of(new OrderDetailDto()));
        var userDto = new UserDto()
                .setStore(new StoreDto().setId(5));

        when(userService.getUserDetail(username)).thenReturn(userDto);
        when(orderService.generateOrderNo(any())).thenReturn("ORD5-20250101000000-ABC123");
        when(orderDetailService.populateOrderDetail(any(), anyString()))
                .thenReturn(List.of(new OrderDetail().setAmount(BigDecimal.valueOf(50))));

        Order result = orderBusinessService.print(orderDto, username);

        assertNotNull(result);
        verify(orderService, never()).insert(any());
    }

    @Test
    void voidOrder_shouldSetStatusAndDelegateVoid() {
        var existingOrder = new Order().setId(1).setStatus(OrderStatus.CONFIRMED);
        when(orderService.selectByPrimaryKey(1)).thenReturn(existingOrder);
        when(orderService.voidOrderById(existingOrder)).thenReturn(1);

        Order result = orderBusinessService.voidOrder(1, "test-user");

        assertEquals(OrderStatus.VOID, result.getStatus());
        assertNotNull(result.getUpdatedAt());
        verify(orderService).voidOrderById(existingOrder);
    }

    @Test
    void getDailyListByStore_shouldRetrieveOrdersForStoreTimeZone() {
        var username = "test-user";
        var userDto = new UserDto()
                .setStore(new StoreDto().setId(1).setTimeZone("Asia/Kuala_Lumpur"));
        var expectedOrders = List.of(new Order().setId(1));

        when(userService.getUserDetail(username)).thenReturn(userDto);
        when(orderService.selectByStoreId(any(), any(Timestamp.class), any(Timestamp.class)))
                .thenReturn(expectedOrders);

        List<Order> result = orderBusinessService.getDailyListByStore(username);

        assertEquals(1, result.size());
    }
}
