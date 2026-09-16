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
import org.springframework.dao.DuplicateKeyException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @Test
    void insertWithIdempotency_whenKeyNull_shouldInsertNormally() {
        var order = new Order()
                .setId(1)
                .setOrderDetails(List.of(new OrderDetail().setId(1)));
        when(orderMapper.insert(order)).thenReturn(1);

        Order result = orderService.insertWithIdempotency(order, null);

        assertEquals(1, result.getId().intValue());
        assertNull(result.getIdempotencyKey());
        verify(orderMapper).insert(order);
        verify(orderDetailService).insertBulk(any());
    }

    @Test
    void insertWithIdempotency_whenKeyPresentAndNoExisting_shouldInsertWithKey() {
        var order = new Order()
                .setId(1)
                .setOrderDetails(List.of(new OrderDetail().setId(1)));
        when(orderMapper.selectByIdempotencyKey("idem-1")).thenReturn(null);
        when(orderMapper.insert(order)).thenReturn(1);

        Order result = orderService.insertWithIdempotency(order, "idem-1");

        assertEquals("idem-1", result.getIdempotencyKey());
        verify(orderMapper).insert(order);
    }

    @Test
    void insertWithIdempotency_whenKeyPresentAndExisting_shouldReturnExisting() {
        var existing = new Order().setId(99).setOrderNo("EXISTING");
        var newOrder = new Order()
                .setOrderDetails(List.of(new OrderDetail().setId(1)));
        when(orderMapper.selectByIdempotencyKey("idem-1")).thenReturn(existing);

        Order result = orderService.insertWithIdempotency(newOrder, "idem-1");

        assertEquals(99, result.getId().intValue());
        assertEquals("EXISTING", result.getOrderNo());
        verify(orderMapper, never()).insert(any());
    }

    @Test
    void insertWithIdempotency_whenDuplicateKeyOnInsert_shouldReturnExistingOrder() {
        var existing = new Order().setId(99).setOrderNo("EXISTING");
        var newOrder = new Order()
                .setOrderDetails(List.of(new OrderDetail().setId(1)));
        when(orderMapper.selectByIdempotencyKey("idem-1")).thenReturn(null, existing);
        when(orderMapper.insert(newOrder)).thenThrow(new DuplicateKeyException("Duplicate"));

        Order result = orderService.insertWithIdempotency(newOrder, "idem-1");

        assertEquals(99, result.getId().intValue());
        assertEquals("EXISTING", result.getOrderNo());
    }

    @Test
    void insertWithIdempotency_whenBlankKey_shouldTreatAsNull() {
        var order = new Order()
                .setId(1)
                .setOrderDetails(List.of(new OrderDetail().setId(1)));
        when(orderMapper.insert(order)).thenReturn(1);

        Order result = orderService.insertWithIdempotency(order, "   ");

        assertNull(result.getIdempotencyKey());
        verify(orderMapper, never()).selectByIdempotencyKey(any());
        verify(orderMapper).insert(order);
    }
}
