package com.harmoni.pos.order.service;

import com.harmoni.pos.order.exception.BusinessNoContentRequestException;
import com.harmoni.pos.order.mapper.OrderMapper;
import com.harmoni.pos.order.model.Order;
import com.harmoni.pos.order.model.OrderDetail;
import com.harmoni.pos.order.model.dto.StoreDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderDetailService orderDetailService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void generateOrderNo_shouldReturnFormattedOrderNumber() {
        var store = new StoreDto().setId(1);
        var order = new Order().setStore(store);

        String orderNo = orderService.generateOrderNo(order);

        assertNotNull(orderNo);
        assertTrue(orderNo.startsWith("ORD1-"));
    }

    @Test
    void deleteByPrimaryKey_shouldReturnAffectedRows() {
        when(orderMapper.deleteByPrimaryKey(1)).thenReturn(1);

        int result = orderService.deleteByPrimaryKey(1);

        assertEquals(1, result);
        verify(orderMapper).deleteByPrimaryKey(1);
    }

    @Test
    void insert_shouldSetTimestampsAndDelegateToDetailService() {
        var orderDetail = new OrderDetail()
                .setId(1);
        var order = new Order()
                .setId(1)
                .setOrderDetails(List.of(orderDetail));
        when(orderMapper.insert(order)).thenReturn(1);

        int result = orderService.insert(order);

        assertEquals(1, result);
        assertNotNull(order.getCreatedAt());
        assertNotNull(orderDetail.getCreatedAt());
        assertEquals(order.getId(), orderDetail.getOrderId());
        verify(orderDetailService).insertBulk(order.getOrderDetails());
    }

    @Test
    void selectByPrimaryKey_whenFound_shouldReturnOrder() {
        var expected = new Order().setId(1);
        when(orderMapper.selectByPrimaryKey(1)).thenReturn(expected);

        Order actual = orderService.selectByPrimaryKey(1);

        assertEquals(expected, actual);
    }

    @Test
    void selectByPrimaryKey_whenNotFound_shouldThrowException() {
        when(orderMapper.selectByPrimaryKey(1)).thenReturn(null);

        assertThrows(BusinessNoContentRequestException.class,
                () -> orderService.selectByPrimaryKey(1));
    }

    @Test
    void selectByStoreId_shouldReturnOrders() {
        var from = Timestamp.valueOf(LocalDateTime.now().minusDays(1));
        var until = Timestamp.valueOf(LocalDateTime.now());
        var expected = List.of(new Order().setId(1));
        when(orderMapper.selectByStoreId(1, from, until)).thenReturn(expected);

        List<Order> actual = orderService.selectByStoreId(1, from, until);

        assertEquals(expected, actual);
    }

    @Test
    void updateByPrimaryKey_shouldReturnAffectedRows() {
        var order = new Order().setId(1);
        when(orderMapper.updateByPrimaryKey(order)).thenReturn(1);

        int result = orderService.updateByPrimaryKey(order);

        assertEquals(1, result);
    }

    @Test
    void voidOrderById_shouldReturnAffectedRows() {
        var order = new Order().setId(1);
        when(orderMapper.voidById(order)).thenReturn(1);

        int result = orderService.voidOrderById(order);

        assertEquals(1, result);
    }
}
