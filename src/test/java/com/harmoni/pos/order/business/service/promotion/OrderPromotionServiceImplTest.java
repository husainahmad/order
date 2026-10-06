package com.harmoni.pos.order.business.service.promotion;

import com.harmoni.pos.order.exception.BusinessBadRequestException;
import com.harmoni.pos.order.business.service.menu.PromotionMenuService;
import com.harmoni.pos.order.model.CalculatedOrder;
import com.harmoni.pos.order.model.Order;
import com.harmoni.pos.order.model.OrderDetail;
import com.harmoni.pos.order.model.OrderDetailSku;
import com.harmoni.pos.order.model.dto.StoreDto;
import com.harmoni.pos.order.model.dto.UserDto;
import com.harmoni.pos.order.model.dto.pricing.AppliedDiscountDto;
import com.harmoni.pos.order.model.dto.pricing.CartLineDto;
import com.harmoni.pos.order.model.dto.pricing.OrderItemDiscountSnapshotDto;
import com.harmoni.pos.order.model.dto.pricing.PricedLineDto;
import com.harmoni.pos.order.model.dto.pricing.PromotionPriceRequestDto;
import com.harmoni.pos.order.model.dto.pricing.PromotionPriceResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderPromotionServiceImplTest {

    private static final String USERNAME = "test-user";

    @Mock
    private PromotionMenuService promotionMenuService;

    private OrderPromotionServiceImpl orderPromotionService;

    @BeforeEach
    void setUp() {
        orderPromotionService = new OrderPromotionServiceImpl(promotionMenuService);
    }

    private static OrderDetailSku sku(Integer id, Integer skuId, String price, int quantity) {
        return new OrderDetailSku()
                .setId(id)
                .setSkuId(skuId)
                .setPrice(new BigDecimal(price))
                .setQuantity(quantity)
                .setAmount(new BigDecimal(price).multiply(BigDecimal.valueOf(quantity)));
    }

    private static OrderDetail detail(Integer productId, Integer categoryId, OrderDetailSku... skus) {
        return new OrderDetail()
                .setId(1)
                .setProductId(productId)
                .setCategoryId(categoryId)
                .setOrderDetailSkus(List.of(skus));
    }

    private static Order order(OrderDetail... details) {
        return new Order().setOrderDetails(List.of(details));
    }

    private static UserDto userDto() {
        return new UserDto().setStore(new StoreDto().setId(5).setTimeZone("Australia/Sydney"));
    }

    private static PricedLineDto priced(int index, String gross, String discount, AppliedDiscountDto... discounts) {
        PricedLineDto dto = new PricedLineDto();
        dto.setLineIndex(index);
        dto.setGrossAmount(new BigDecimal(gross));
        dto.setDiscountAmount(new BigDecimal(discount));
        dto.setNetAmount(new BigDecimal(gross).subtract(new BigDecimal(discount)));
        dto.setDiscounted(discounts.length > 0);
        dto.setDiscounts(List.of(discounts));
        return dto;
    }

    private static AppliedDiscountDto applied(String amount) {
        AppliedDiscountDto dto = new AppliedDiscountDto();
        dto.setPromotionId(99L);
        dto.setPromotionCode("HAPPY10");
        dto.setPromotionName("Happy Hour");
        dto.setDiscountType("PERCENTAGE");
        dto.setDiscountValue(new BigDecimal("10.00"));
        dto.setDiscountAmount(new BigDecimal(amount));
        return dto;
    }

    private void menuPrices(PricedLineDto... lines) {
        PromotionPriceResponseDto response = new PromotionPriceResponseDto();
        response.setLines(List.of(lines));
        when(promotionMenuService.price(any(), eq(USERNAME))).thenReturn(response);
    }

    @Test
    void applyPromotions_shouldSendOneCartLinePerSkuWithItsProductAndCategory() {
        menuPrices(priced(0, "20.00", "0.00"));

        orderPromotionService.applyPromotions(
                order(detail(10, 30, sku(9001, 20, "20.00", 1))), userDto(), USERNAME);

        ArgumentCaptor<PromotionPriceRequestDto> captor = ArgumentCaptor.forClass(PromotionPriceRequestDto.class);
        verify(promotionMenuService).price(captor.capture(), eq(USERNAME));

        List<CartLineDto> lines = captor.getValue().getLines();
        assertEquals(1, lines.size());
        assertEquals(0, lines.get(0).getLineIndex());
        assertEquals(10, lines.get(0).getProductId());
        assertEquals(20, lines.get(0).getSkuId());
        assertEquals(30, lines.get(0).getCategoryId());
        assertEquals(new BigDecimal("20.00"), lines.get(0).getUnitPrice());
        assertEquals(1, lines.get(0).getQuantity());
    }

    @Test
    void applyPromotions_shouldSendTheStoreAndItsOwnTimeZone() {
        menuPrices(priced(0, "20.00", "0.00"));

        orderPromotionService.applyPromotions(
                order(detail(10, 30, sku(9001, 20, "20.00", 1))), userDto(), USERNAME);

        ArgumentCaptor<PromotionPriceRequestDto> captor = ArgumentCaptor.forClass(PromotionPriceRequestDto.class);
        verify(promotionMenuService).price(captor.capture(), eq(USERNAME));

        assertEquals(5L, captor.getValue().getContext().getStoreId());
        assertEquals("Australia/Sydney", captor.getValue().getContext().getZone());
    }

    @Test
    void applyPromotions_shouldNumberTheLinesAcrossProductsInOrder() {
        menuPrices(priced(0, "20.00", "0.00"), priced(1, "30.00", "0.00"), priced(2, "40.00", "0.00"));

        orderPromotionService.applyPromotions(order(
                detail(10, 30, sku(9001, 20, "20.00", 1)),
                detail(11, 30, sku(9002, 21, "30.00", 1), sku(9003, 22, "20.00", 2))), userDto(), USERNAME);

        ArgumentCaptor<PromotionPriceRequestDto> captor = ArgumentCaptor.forClass(PromotionPriceRequestDto.class);
        verify(promotionMenuService).price(captor.capture(), eq(USERNAME));

        List<CartLineDto> lines = captor.getValue().getLines();
        assertEquals(3, lines.size());
        assertEquals(0, lines.get(0).getLineIndex());
        assertEquals(1, lines.get(1).getLineIndex());
        assertEquals(2, lines.get(2).getLineIndex());
        assertEquals(11, lines.get(1).getProductId());
        assertEquals(22, lines.get(2).getSkuId());
    }

    @Test
    void applyPromotions_shouldWriteTheDiscountOntoTheSkuLine() {
        menuPrices(priced(0, "20.00", "2.00", applied("2.00")));

        Order order = order(detail(10, 30, sku(9001, 20, "20.00", 1)));
        orderPromotionService.applyPromotions(order, userDto(), USERNAME);

        assertEquals(new BigDecimal("2.00"), order.getOrderDetails().get(0).getOrderDetailSkus().get(0).getDiscountAmount());
    }

    @Test
    void applyPromotions_shouldSumTheSkuDiscountsOntoTheProductLine() {
        menuPrices(priced(0, "20.00", "2.00", applied("2.00")), priced(1, "40.00", "4.00", applied("4.00")));

        Order order = order(detail(10, 30, sku(9001, 20, "20.00", 1), sku(9002, 21, "20.00", 2)));
        orderPromotionService.applyPromotions(order, userDto(), USERNAME);

        assertEquals(new BigDecimal("6.00"), order.getOrderDetails().get(0).getDiscountAmount());
    }

    @Test
    void applyPromotions_shouldTotalEveryLineDiscountOntoTheOrder() {
        menuPrices(priced(0, "20.00", "2.00", applied("2.00")), priced(1, "50.00", "5.00", applied("5.00")));

        Order order = order(
                detail(10, 30, sku(9001, 20, "20.00", 1)),
                detail(11, 31, sku(9002, 22, "25.00", 2)));
        orderPromotionService.applyPromotions(order, userDto(), USERNAME);

        assertEquals(new BigDecimal("7.00"), order.getDiscountTotal());
    }

    @Test
    void applyPromotions_shouldLeaveTheGrossLineAmountAloneSoTheSubtotalStaysTheBasketValue() {
        menuPrices(priced(0, "20.00", "2.00", applied("2.00")));

        Order order = order(detail(10, 30, sku(9001, 20, "20.00", 1)));
        orderPromotionService.applyPromotions(order, userDto(), USERNAME);

        assertEquals(new BigDecimal("20.00"), order.getOrderDetails().get(0).getOrderDetailSkus().get(0).getAmount());
    }

    @Test
    void applyPromotions_shouldRecordZeroWhenNoPromotionApplies() {
        menuPrices(priced(0, "20.00", "0.00"));

        Order order = order(detail(10, 30, sku(9001, 20, "20.00", 1)));
        CalculatedOrder result = orderPromotionService.applyPromotions(order, userDto(), USERNAME);

        assertEquals(0, result.totalDiscount().compareTo(BigDecimal.ZERO));
        assertEquals(0, order.getOrderDetails().get(0).getDiscountAmount().compareTo(BigDecimal.ZERO));
        assertTrue(result.lineDiscounts().get(0).discounts().isEmpty());
    }

    @Test
    void applyPromotions_whenTheOrderHasNoLines_shouldNotCallTheMenuService() {
        Order order = new Order().setOrderDetails(List.of());

        CalculatedOrder result = orderPromotionService.applyPromotions(order, userDto(), USERNAME);

        assertEquals(0, result.totalDiscount().compareTo(BigDecimal.ZERO));
        assertEquals(0, order.getDiscountTotal().compareTo(BigDecimal.ZERO));
        verify(promotionMenuService, org.mockito.Mockito.never()).price(any(), anyString());
    }

    @Test
    void applyPromotions_whenTheMenuPricesTheWrongNumberOfLines_shouldRefuseToGuess() {
        menuPrices(priced(0, "20.00", "2.00", applied("2.00")));

        Order order = order(detail(10, 30, sku(9001, 20, "20.00", 1), sku(9002, 21, "30.00", 1)));

        assertThrows(BusinessBadRequestException.class,
                () -> orderPromotionService.applyPromotions(order, userDto(), USERNAME));
    }

    @Test
    void applyPromotions_whenTheMenuReturnsNoLines_shouldRefuseToGuess() {
        PromotionPriceResponseDto response = new PromotionPriceResponseDto();
        when(promotionMenuService.price(any(), eq(USERNAME))).thenReturn(response);

        Order order = order(detail(10, 30, sku(9001, 20, "20.00", 1)));

        assertThrows(BusinessBadRequestException.class,
                () -> orderPromotionService.applyPromotions(order, userDto(), USERNAME));
    }

    @Test
    void toSnapshots_shouldPointEachAuditRowAtThePersistedOrderLine() {
        menuPrices(priced(0, "20.00", "2.00", applied("2.00")), priced(1, "30.00", "3.00", applied("3.00")));
        Order order = order(detail(10, 30, sku(9001, 20, "20.00", 1), sku(9002, 21, "30.00", 1)));
        CalculatedOrder calculated = orderPromotionService.applyPromotions(order, userDto(), USERNAME);

        List<OrderItemDiscountSnapshotDto> snapshots = orderPromotionService.toSnapshots(calculated);

        assertEquals(2, snapshots.size());
        assertEquals(9001L, snapshots.get(0).getOrderItemId());
        assertEquals(9002L, snapshots.get(1).getOrderItemId());
    }

    @Test
    void toSnapshots_shouldPassThePromotionThroughUntouched() {
        menuPrices(priced(0, "20.00", "2.00", applied("2.00")));
        Order order = order(detail(10, 30, sku(9001, 20, "20.00", 1)));
        CalculatedOrder calculated = orderPromotionService.applyPromotions(order, userDto(), USERNAME);

        OrderItemDiscountSnapshotDto snapshot = orderPromotionService.toSnapshots(calculated).get(0);

        assertEquals(99L, snapshot.getPromotionId());
        assertEquals("HAPPY10", snapshot.getPromotionCode());
        assertEquals("Happy Hour", snapshot.getPromotionName());
        assertEquals("PERCENTAGE", snapshot.getDiscountType());
        assertEquals(new BigDecimal("10.00"), snapshot.getDiscountValue());
        assertEquals(new BigDecimal("2.00"), snapshot.getDiscountAmount());
    }

    @Test
    void toSnapshots_shouldWriteOneAuditRowPerPromotionOnALine() {
        menuPrices(priced(0, "20.00", "7.00", applied("5.00"), applied("2.00")));
        Order order = order(detail(10, 30, sku(9001, 20, "20.00", 1)));
        CalculatedOrder calculated = orderPromotionService.applyPromotions(order, userDto(), USERNAME);

        assertEquals(2, orderPromotionService.toSnapshots(calculated).size());
    }

    @Test
    void toSnapshots_shouldReturnNothingWhenNoPromotionApplied() {
        menuPrices(priced(0, "20.00", "0.00"));
        Order order = order(detail(10, 30, sku(9001, 20, "20.00", 1)));
        CalculatedOrder calculated = orderPromotionService.applyPromotions(order, userDto(), USERNAME);

        assertTrue(orderPromotionService.toSnapshots(calculated).isEmpty());
    }

    @Test
    void toSnapshots_whenALineHasNoIdAfterInsert_shouldRefuseToRecordItAgainstTheWrongLine() {
        menuPrices(priced(0, "20.00", "2.00", applied("2.00")));
        Order order = order(detail(10, 30, sku(null, 20, "20.00", 1)));
        CalculatedOrder calculated = orderPromotionService.applyPromotions(order, userDto(), USERNAME);

        assertThrows(BusinessBadRequestException.class, () -> orderPromotionService.toSnapshots(calculated));
    }
}
