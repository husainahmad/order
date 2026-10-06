package com.harmoni.pos.order.service;

import com.harmoni.pos.order.exception.BusinessBadRequestException;
import com.harmoni.pos.order.mapper.OrderItemDiscountMapper;
import com.harmoni.pos.order.model.DiscountType;
import com.harmoni.pos.order.model.OrderItemDiscount;
import com.harmoni.pos.order.model.dto.pricing.OrderItemDiscountSnapshotDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderItemDiscountServiceImplTest {

    @Mock
    private OrderItemDiscountMapper orderItemDiscountMapper;

    private OrderItemDiscountServiceImpl orderItemDiscountService;

    @BeforeEach
    void setUp() {
        orderItemDiscountService = new OrderItemDiscountServiceImpl(orderItemDiscountMapper);
    }

    private static OrderItemDiscountSnapshotDto snapshot(long orderItemId, String type, String amount) {
        OrderItemDiscountSnapshotDto dto = new OrderItemDiscountSnapshotDto();
        dto.setOrderItemId(orderItemId);
        dto.setPromotionId(99L);
        dto.setPromotionCode("HAPPY10");
        dto.setPromotionName("Happy Hour");
        dto.setDiscountType(type);
        dto.setDiscountValue(new BigDecimal("10.00"));
        dto.setDiscountAmount(new BigDecimal(amount));
        return dto;
    }

    @Test
    void recordSnapshot_shouldStoreTheEngineAnswerVerbatim() {
        List<OrderItemDiscount> recorded = orderItemDiscountService.recordSnapshot(
                List.of(snapshot(9001L, "PERCENTAGE", "2.00")));

        assertEquals(1, recorded.size());
        OrderItemDiscount row = recorded.get(0);
        assertEquals(9001L, row.getOrderItemId());
        assertEquals(99L, row.getPromotionId());
        assertEquals("HAPPY10", row.getPromotionCode());
        assertEquals("Happy Hour", row.getPromotionName());
        assertEquals(DiscountType.PERCENTAGE, row.getDiscountType());
        assertEquals(new BigDecimal("10.00"), row.getDiscountValue());
        assertEquals(new BigDecimal("2.00"), row.getDiscountAmount());
    }

    @Test
    void recordSnapshot_shouldStampEveryRowInTheBatchWithTheSameTime() {
        List<OrderItemDiscount> recorded = orderItemDiscountService.recordSnapshot(
                List.of(snapshot(9001L, "PERCENTAGE", "2.00"), snapshot(9002L, "PERCENTAGE", "3.00")));

        assertNotNull(recorded.get(0).getCreatedAt());
        assertEquals(recorded.get(0).getCreatedAt(), recorded.get(1).getCreatedAt());
    }

    @Test
    void recordSnapshot_shouldWriteThemInOneBatch() {
        orderItemDiscountService.recordSnapshot(
                List.of(snapshot(9001L, "PERCENTAGE", "2.00"), snapshot(9002L, "FIXED_AMOUNT", "5.00")));

        verify(orderItemDiscountMapper).insertBatch(anyList());
    }

    @Test
    void recordSnapshot_shouldReadBackThePromotionalSpecialPriceType() {
        List<OrderItemDiscount> recorded = orderItemDiscountService.recordSnapshot(
                List.of(snapshot(9001L, "SPECIAL_PRICE", "1.00")));

        assertEquals(DiscountType.SPECIAL_PRICE, recorded.get(0).getDiscountType());
    }

    @Test
    void recordSnapshot_shouldRejectADiscountTypeItCannotStore() {
        assertThrows(BusinessBadRequestException.class,
                () -> orderItemDiscountService.recordSnapshot(List.of(snapshot(9001L, "BUY_X_GET_Y", "2.00"))));
    }

    @Test
    void recordSnapshot_shouldLeaveTheTimestampToTheServiceRatherThanTheCaller() {
        orderItemDiscountService.recordSnapshot(List.of(snapshot(9001L, "PERCENTAGE", "2.00")));

        ArgumentCaptor<List<OrderItemDiscount>> captor = ArgumentCaptor.forClass(List.class);
        verify(orderItemDiscountMapper).insertBatch(captor.capture());
        assertNotNull(captor.getValue().get(0).getCreatedAt());
    }

    @Test
    void recordSnapshot_whenNothingWasSubmitted_shouldNotTouchTheDatabase() {
        List<OrderItemDiscount> recorded = orderItemDiscountService.recordSnapshot(List.of());

        assertTrue(recorded.isEmpty());
        verify(orderItemDiscountMapper, org.mockito.Mockito.never()).insertBatch(anyList());
    }

    @Test
    void recordSnapshot_whenSubmittedNull_shouldNotTouchTheDatabase() {
        assertTrue(orderItemDiscountService.recordSnapshot(null).isEmpty());
        verify(orderItemDiscountMapper, org.mockito.Mockito.never()).insertBatch(anyList());
    }

    @Test
    void recordSnapshot_shouldAcceptAManualDiscountWithNoPromotionBehindIt() {
        OrderItemDiscountSnapshotDto dto = snapshot(9001L, "MANUAL", "1.00");
        dto.setPromotionId(null);
        dto.setPromotionCode(null);
        dto.setPromotionName(null);

        List<OrderItemDiscount> recorded = orderItemDiscountService.recordSnapshot(List.of(dto));

        assertNull(recorded.get(0).getPromotionId());
        assertNull(recorded.get(0).getPromotionCode());
        assertEquals(DiscountType.MANUAL, recorded.get(0).getDiscountType());
    }
}
