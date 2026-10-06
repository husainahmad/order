package com.harmoni.pos.order.service;

import com.harmoni.pos.order.exception.BusinessNoContentRequestException;
import com.harmoni.pos.order.mapper.OrderMapper;
import com.harmoni.pos.order.model.Order;
import com.harmoni.pos.order.model.OrderDetail;
import com.harmoni.pos.order.model.OrderDetailCustomization;
import com.harmoni.pos.order.model.OrderDetailCustomizationOption;
import com.harmoni.pos.order.model.OrderDetailSku;
import com.harmoni.pos.order.model.dto.StoreDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.math.BigDecimal;
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

    @Mock
    private OrderDetailCustomizationService orderDetailCustomizationService;

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

    private static Order orderWithDetails(int orderId, Integer... detailIds) {
        var details = java.util.Arrays.stream(detailIds)
                .map(detailId -> new OrderDetail().setId(detailId))
                .toList();
        return new Order()
                .setId(orderId)
                .setOrderDetails(details);
    }

    private static OrderDetailCustomization customizationOn(Integer detailId, Integer optionId) {
        var option = new OrderDetailCustomizationOption()
                .setCustomizationOptionsId(optionId)
                .setCustomizationOptionName("Large")
                .setPrice(new BigDecimal("1.50"))
                .setAmount(new BigDecimal("3.00"));
        return new OrderDetailCustomization()
                .setCustomizationId(1)
                .setCustomizationName("Size")
                .setOrderDetailId(detailId)
                .setOptions(List.of(option));
    }

    /**
     * An order read back has to say what the customer chose. Without this the detail comes back
     * bare and a receipt printed from it is not the sale that was taken.
     */
    @Test
    void selectByPrimaryKey_shouldPutEachDetailsChoicesBackOnThatDetail() {
        var order = orderWithDetails(7, 900, 901);
        when(orderMapper.selectByPrimaryKey(7)).thenReturn(order);
        when(orderDetailCustomizationService.getByOrderDetailIds(List.of(900, 901)))
                .thenReturn(List.of(customizationOn(900, 101), customizationOn(901, 102)));

        Order result = orderService.selectByPrimaryKey(7);

        List<OrderDetail> details = result.getOrderDetails();
        assertEquals(1, details.get(0).getCustomizations().size());
        assertEquals(101, details.get(0).getCustomizations().get(0).getOptions().get(0).getCustomizationOptionsId().intValue());
        assertEquals(1, details.get(1).getCustomizations().size());
        assertEquals(102, details.get(1).getCustomizations().get(0).getOptions().get(0).getCustomizationOptionsId().intValue());
    }

    @Test
    void selectByStoreId_shouldPutEachDetailsChoicesBackOnThatDetail() {
        var order = orderWithDetails(7, 900);
        when(orderMapper.selectByStoreId(eq(1), any(), any())).thenReturn(List.of(order));
        when(orderDetailCustomizationService.getByOrderDetailIds(List.of(900)))
                .thenReturn(List.of(customizationOn(900, 101)));

        List<Order> result = orderService.selectByStoreId(1, Timestamp.valueOf("2026-01-01 00:00:00"),
                Timestamp.valueOf("2026-01-02 00:00:00"));

        assertEquals(1, result.get(0).getOrderDetails().get(0).getCustomizations().size());
    }

    /**
     * The field is NON_NULL, so a plain detail has to come back with nothing on it rather than
     * an empty list. An empty list would claim the detail was checked and had no choices, which
     * is not the same as saying nothing about it.
     */
    @Test
    void aDetailWithNoChoicesShouldBeLeftUnsetRatherThanGivenAnEmptyList() {
        var order = orderWithDetails(7, 900, 901);
        when(orderMapper.selectByPrimaryKey(7)).thenReturn(order);
        when(orderDetailCustomizationService.getByOrderDetailIds(List.of(900, 901)))
                .thenReturn(List.of(customizationOn(900, 101)));

        Order result = orderService.selectByPrimaryKey(7);

        List<OrderDetail> details = result.getOrderDetails();
        assertNull(details.get(1).getCustomizations());
    }

    /**
     * The order mapper joins to details but not to the choices, because that join multiplies
     * rows by the number of choices on a detail. They are fetched per order rather than per
     * detail so a large order is one extra query instead of one per customised detail.
     */
    @Test
    void everyDetailsChoicesShouldBeFetchedInOneCall() {
        var order = orderWithDetails(7, 900, 901, 902);
        when(orderMapper.selectByPrimaryKey(7)).thenReturn(order);
        when(orderDetailCustomizationService.getByOrderDetailIds(any())).thenReturn(List.of());

        orderService.selectByPrimaryKey(7);

        ArgumentCaptor<List<Integer>> captor = ArgumentCaptor.forClass(List.class);
        verify(orderDetailCustomizationService).getByOrderDetailIds(captor.capture());
        assertEquals(List.of(900, 901, 902), captor.getValue());
    }

    @Test
    void anOrderWithNoDetailsShouldNotQueryForChoicesAtAll() {
        var order = new Order().setId(7);
        when(orderMapper.selectByPrimaryKey(7)).thenReturn(order);

        orderService.selectByPrimaryKey(7);

        verify(orderDetailCustomizationService, never()).getByOrderDetailIds(any());
    }

    @Test
    void findByIdempotencyKey_shouldPutTheDetailsChoicesBackToo() {
        var order = orderWithDetails(7, 900);
        when(orderMapper.selectByIdempotencyKey("idem-1")).thenReturn(order);
        when(orderDetailCustomizationService.getByOrderDetailIds(List.of(900)))
                .thenReturn(List.of(customizationOn(900, 101)));

        Order result = orderService.findByIdempotencyKey("idem-1");

        assertEquals(101, result.getOrderDetails().get(0).getCustomizations().get(0).getOptions().get(0).getCustomizationOptionsId().intValue());
    }

    /**
     * A replayed confirmation hands back the order that was already sold, so that one has to
     * carry its choices for the same reason a freshly read one does. Going back to the mapper
     * directly here would quietly return a bare order.
     */
    @Test
    void insertWithIdempotency_whenReplayed_shouldReturnTheExistingOrderWithItsChoices() {
        var existing = orderWithDetails(99, 900);
        var newOrder = new Order().setOrderDetails(List.of(new OrderDetail().setId(1)));
        when(orderMapper.selectByIdempotencyKey("idem-1")).thenReturn(existing);
        when(orderDetailCustomizationService.getByOrderDetailIds(List.of(900)))
                .thenReturn(List.of(customizationOn(900, 101)));

        Order result = orderService.insertWithIdempotency(newOrder, "idem-1");

        assertEquals(99, result.getId().intValue());
        assertEquals(1, result.getOrderDetails().get(0).getCustomizations().size());
    }
}
