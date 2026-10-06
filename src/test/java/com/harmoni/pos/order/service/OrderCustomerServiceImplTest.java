package com.harmoni.pos.order.service;

import com.harmoni.pos.order.mapper.OrderCustomerMapper;
import com.harmoni.pos.order.model.Order;
import com.harmoni.pos.order.model.OrderCustomer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderCustomerServiceImplTest {

    @Mock
    private OrderCustomerMapper orderCustomerMapper;

    private OrderCustomerServiceImpl orderCustomerService;

    @BeforeEach
    void setUp() {
        orderCustomerService = new OrderCustomerServiceImpl(orderCustomerMapper);
    }

    private static Order order(int id, Integer customerId, String name, String phone) {
        return new Order()
                .setId(id)
                .setCustomerId(customerId)
                .setCustomerName(name)
                .setCustomerPhone(phone);
    }

    @Test
    void record_shouldCopyTheCustomerAsTheyWereAtTheTimeOfTheSale() {
        OrderCustomer recorded = orderCustomerService.record(order(500, 77, "Ada", "0400123456"));

        assertNotNull(recorded);
        assertEquals(500, recorded.getOrderId().intValue());
        assertEquals(77L, recorded.getCustomerId().longValue());
        assertEquals("Ada", recorded.getCustomerName());
        assertEquals("0400123456", recorded.getCustomerPhone());
        assertNotNull(recorded.getCreatedAt());
    }

    @Test
    void record_shouldHangTheSnapshotOffThePersistedOrderNotTheRequest() {
        orderCustomerService.record(order(500, 77, "Ada", "0400123456"));

        ArgumentCaptor<OrderCustomer> captor = ArgumentCaptor.forClass(OrderCustomer.class);
        verify(orderCustomerMapper).insert(captor.capture());
        assertEquals(500, captor.getValue().getOrderId().intValue());
    }

    @Test
    void record_shouldNotWriteARowForAWalkInSale() {
        assertNull(orderCustomerService.record(order(500, -1, null, null)));

        verify(orderCustomerMapper, never()).insert(any());
    }

    @Test
    void record_shouldNotWriteARowWhenNoCustomerWasGivenAtAll() {
        assertNull(orderCustomerService.record(order(500, null, null, null)));

        verify(orderCustomerMapper, never()).insert(any());
    }

    @Test
    void record_shouldNotWriteARowForANullOrder() {
        assertNull(orderCustomerService.record(null));

        verify(orderCustomerMapper, never()).insert(any());
    }

    @Test
    void record_shouldKeepTheNameEvenWhenNoPhoneWasGiven() {
        OrderCustomer recorded = orderCustomerService.record(order(500, 77, "Ada", null));

        assertEquals("Ada", recorded.getCustomerName());
        assertNull(recorded.getCustomerPhone());
    }

    @Test
    void getByOrderId_shouldDelegate() {
        when(orderCustomerMapper.selectByOrderId(500)).thenReturn(new OrderCustomer().setOrderId(500));

        assertEquals(500, orderCustomerService.getByOrderId(500).getOrderId().intValue());
        verify(orderCustomerMapper).selectByOrderId(500);
    }

    @Test
    void getByOrderId_shouldReturnNullWhenTheOrderHadNoCustomer() {
        when(orderCustomerMapper.selectByOrderId(anyInt())).thenReturn(null);

        assertNull(orderCustomerService.getByOrderId(500));
    }

    @Test
    void getHistory_shouldDelegate() {
        when(orderCustomerMapper.selectHistory(eq(77L), any(), any())).thenReturn(List.of(new OrderCustomer()));

        assertEquals(1, orderCustomerService.getHistory(77L, new Date(), new Date()).size());
        verify(orderCustomerMapper).selectHistory(eq(77L), any(), any());
    }

    @Test
    void getHistory_shouldPassNullBoundsThroughSoTheRangeIsOmittedEntirely() {
        when(orderCustomerMapper.selectHistory(eq(77L), any(), any())).thenReturn(List.of());

        assertTrue(orderCustomerService.getHistory(77L, null, null).isEmpty());
        verify(orderCustomerMapper).selectHistory(77L, null, null);
    }

    @Test
    void deleteByOrderId_shouldDelegate() {
        when(orderCustomerMapper.deleteByOrderId(500)).thenReturn(1);

        assertEquals(1, orderCustomerService.deleteByOrderId(500));
        verify(orderCustomerMapper).deleteByOrderId(500);
    }
}
