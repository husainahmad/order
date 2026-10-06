package com.harmoni.pos.order.business.service;

import com.harmoni.pos.order.business.service.menu.UserService;
import com.harmoni.pos.order.business.service.promotion.OrderPromotionService;
import com.harmoni.pos.order.model.CalculatedOrder;
import com.harmoni.pos.order.model.Order;
import com.harmoni.pos.order.model.OrderDetail;
import com.harmoni.pos.order.model.OrderDetailCustomization;
import com.harmoni.pos.order.model.OrderDetailCustomizationOption;
import com.harmoni.pos.order.model.OrderDetailSku;
import com.harmoni.pos.order.model.OrderItemDiscount;
import com.harmoni.pos.order.model.OrderStatus;
import com.harmoni.pos.order.model.dto.OrderDto;
import com.harmoni.pos.order.model.dto.OrderDetailDto;
import com.harmoni.pos.order.model.dto.StoreDto;
import com.harmoni.pos.order.model.dto.UserDto;
import com.harmoni.pos.order.model.dto.pricing.OrderItemDiscountSnapshotDto;
import com.harmoni.pos.order.service.OrderCustomerService;
import com.harmoni.pos.order.service.OrderDetailService;
import com.harmoni.pos.order.service.OrderItemDiscountService;
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
import static org.mockito.ArgumentMatchers.anyList;
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

    @Mock
    private OrderPromotionService orderPromotionService;

    @Mock
    private OrderItemDiscountService orderItemDiscountService;

    @Mock
    private OrderCustomerService orderCustomerService;

    @InjectMocks
    private OrderBusinessServiceImpl orderBusinessService;

    /**
     * Stands in for the pricing call, which is exercised in
     * {@code OrderPromotionServiceImplTest}. Returns the calculated order unchanged so the
     * tests here can concentrate on how the order is assembled from it.
     */
    private void promotionsGrantNothing() {
        when(orderPromotionService.applyPromotions(any(), any(), anyString()))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    order.setDiscountTotal(BigDecimal.ZERO);
                    return new CalculatedOrder(order, List.of(), BigDecimal.ZERO);
                });
    }

    private static UserDto userDto() {
        return new UserDto().setStore(new StoreDto().setId(5).setTimeZone("Australia/Sydney"));
    }

    @Test
    void calculate_shouldKeepTheCustomerTheClientNamed() {
        var username = "test-user";
        var orderDto = new OrderDto();
        orderDto.setCustomerId(77);
        orderDto.setCustomerName("Ada");
        orderDto.setCustomerPhone("0400123456");
        orderDto.setOrderDetailDtos(List.of(new OrderDetailDto()));

        when(userService.getUserDetail(username)).thenReturn(userDto());
        when(orderService.generateOrderNo(any())).thenReturn("ORD5-20250101000000-ABC123");
        when(orderDetailService.populateOrderDetail(any(), anyInt(), anyString()))
                .thenReturn(List.of(new OrderDetail().setAmount(new BigDecimal("100.00"))));
        promotionsGrantNothing();

        Order result = orderBusinessService.calculate(orderDto, username);

        assertEquals(77, result.getCustomerId().intValue());
        assertEquals("Ada", result.getCustomerName());
        assertEquals("0400123456", result.getCustomerPhone());
    }

    @Test
    void calculate_shouldTreatAnAbsentCustomerAsAWalkIn() {
        var username = "test-user";
        var orderDto = new OrderDto();
        orderDto.setOrderDetailDtos(List.of(new OrderDetailDto()));

        when(userService.getUserDetail(username)).thenReturn(userDto());
        when(orderService.generateOrderNo(any())).thenReturn("ORD5-20250101000000-ABC123");
        when(orderDetailService.populateOrderDetail(any(), anyInt(), anyString()))
                .thenReturn(List.of(new OrderDetail().setAmount(new BigDecimal("100.00"))));
        promotionsGrantNothing();

        Order result = orderBusinessService.calculate(orderDto, username);

        assertEquals(-1, result.getCustomerId().intValue());
    }

    @Test
    void calculate_shouldNotLetAClientClaimTheWalkInSentinelAsACustomer() {
        var username = "test-user";
        var orderDto = new OrderDto();
        orderDto.setCustomerId(-1);
        orderDto.setOrderDetailDtos(List.of(new OrderDetailDto()));

        when(userService.getUserDetail(username)).thenReturn(userDto());
        when(orderService.generateOrderNo(any())).thenReturn("ORD5-20250101000000-ABC123");
        when(orderDetailService.populateOrderDetail(any(), anyInt(), anyString()))
                .thenReturn(List.of(new OrderDetail().setAmount(new BigDecimal("100.00"))));
        promotionsGrantNothing();

        Order result = orderBusinessService.calculate(orderDto, username);

        assertEquals(-1, result.getCustomerId().intValue());
    }

    @Test
    void confirm_shouldSnapshotTheCustomerAgainstThePersistedOrder() {
        var username = "test-user";
        var orderDto = new OrderDto();
        orderDto.setCustomerId(77);
        orderDto.setCustomerName("Ada");
        orderDto.setCustomerPhone("0400123456");
        orderDto.setOrderDetailDtos(List.of(new OrderDetailDto()));

        when(userService.getUserDetail(username)).thenReturn(userDto());
        when(orderService.generateOrderNo(any())).thenReturn("ORD5-20250101000000-ABC123");
        when(orderDetailService.populateOrderDetail(any(), anyInt(), anyString()))
                .thenReturn(List.of(new OrderDetail().setAmount(new BigDecimal("100.00"))));
        promotionsGrantNothing();
        when(orderService.insertWithIdempotency(any(), any())).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(500);
            return order;
        });

        orderBusinessService.confirm(orderDto, username);

        verify(orderCustomerService).record(argThat(order ->
                Integer.valueOf(500).equals(order.getId()) && Integer.valueOf(77).equals(order.getCustomerId())));
    }

    @Test
    void confirm_shouldStillCallRecordForAWalkInAndLetTheServiceDecide() {
        var username = "test-user";
        var orderDto = new OrderDto();
        orderDto.setOrderDetailDtos(List.of(new OrderDetailDto()));

        when(userService.getUserDetail(username)).thenReturn(userDto());
        when(orderService.generateOrderNo(any())).thenReturn("ORD5-20250101000000-ABC123");
        when(orderDetailService.populateOrderDetail(any(), anyInt(), anyString()))
                .thenReturn(List.of(new OrderDetail().setAmount(new BigDecimal("100.00"))));
        promotionsGrantNothing();
        when(orderService.insertWithIdempotency(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));

        orderBusinessService.confirm(orderDto, username);

        verify(orderCustomerService).record(any());
    }

    @Test
    void confirm_shouldNotSnapshotTheCustomerOnAnIdempotentReplay() {
        var username = "test-user";
        var orderDto = new OrderDto();
        orderDto.setCustomerId(77);
        orderDto.setOrderDetailDtos(List.of(new OrderDetailDto()));
        var existingOrder = new Order().setId(42).setOrderNo("ORD5-EXISTING");

        when(userService.getUserDetail(username)).thenReturn(userDto());
        when(orderService.generateOrderNo(any())).thenReturn("ORD5-20250101000000-ABC123");
        when(orderDetailService.populateOrderDetail(any(), anyInt(), anyString()))
                .thenReturn(List.of(new OrderDetail().setAmount(new BigDecimal("100.00"))));
        promotionsGrantNothing();
        when(orderService.insertWithIdempotency(any(), eq("idem-key-1"))).thenReturn(existingOrder);

        orderBusinessService.confirm(orderDto, username, "idem-key-1");

        verify(orderCustomerService, never()).record(any());
    }

    @Test
    void print_shouldNotSnapshotTheCustomer() {
        var username = "test-user";
        var orderDto = new OrderDto();
        orderDto.setCustomerId(77);
        orderDto.setOrderDetailDtos(List.of(new OrderDetailDto()));

        when(userService.getUserDetail(username)).thenReturn(userDto());
        when(orderService.generateOrderNo(any())).thenReturn("ORD5-20250101000000-ABC123");
        when(orderDetailService.populateOrderDetail(any(), anyInt(), anyString()))
                .thenReturn(List.of(new OrderDetail().setAmount(new BigDecimal("100.00"))));
        promotionsGrantNothing();

        orderBusinessService.print(orderDto, username);

        verify(orderCustomerService, never()).record(any());
    }

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
    void calculateDiscount_shouldTakeTheGrantFromThePromotionServiceAndRecalculateGrandTotal() {
        var order = new Order().setSubTotal(BigDecimal.valueOf(200));
        when(orderPromotionService.applyPromotions(eq(order), any(), anyString()))
                .thenAnswer(invocation -> {
                    order.setDiscountTotal(BigDecimal.valueOf(30));
                    return new CalculatedOrder(order, List.of(), BigDecimal.valueOf(30));
                });

        orderBusinessService.calculateDiscount(order, userDto(), "test-user");

        assertEquals(BigDecimal.valueOf(30), order.getDiscountTotal());
        assertEquals(BigDecimal.valueOf(170), order.getGrandTotal());
    }

    @Test
    void calculateDiscount_whenNoPromotionApplies_shouldSetZero() {
        var order = new Order().setSubTotal(BigDecimal.valueOf(200));
        promotionsGrantNothing();

        orderBusinessService.calculateDiscount(order, userDto(), "test-user");

        assertEquals(BigDecimal.ZERO, order.getDiscountTotal());
        assertEquals(BigDecimal.valueOf(200), order.getGrandTotal());
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
        orderDto.setOrderDetailDtos(List.of(new OrderDetailDto()));
        orderDto.setRemark("test");
        var orderDetails = List.of(
                new OrderDetail().setAmount(BigDecimal.valueOf(100))
        );

        when(userService.getUserDetail(username)).thenReturn(userDto());
        when(orderService.generateOrderNo(any())).thenReturn("ORD5-20250101000000-ABC123");
        when(orderDetailService.populateOrderDetail(any(), anyInt(), anyString())).thenReturn(orderDetails);
        promotionsGrantNothing();

        Order result = orderBusinessService.calculate(orderDto, username);

        assertNotNull(result);
        assertEquals(1, result.getCustomerId().intValue());
        assertEquals("John", result.getCustomerName());
        assertEquals(5, result.getStore().getId().intValue());
        assertNotNull(result.getOrderNo());
        assertEquals(BigDecimal.valueOf(100), result.getSubTotal());
        assertEquals(BigDecimal.ZERO, result.getDiscountTotal());
        assertEquals(BigDecimal.valueOf(100), result.getGrandTotal());
        assertEquals(BigDecimal.ZERO, result.getTaxTotal());
        assertEquals(BigDecimal.ZERO, result.getServiceChargeTotal());
    }

    @Test
    void calculate_shouldIgnoreTheDiscountTheClientSent() {
        var username = "test-user";
        var orderDto = new OrderDto();
        orderDto.setDiscount(new BigDecimal("99999"));
        orderDto.setOrderDetailDtos(List.of(new OrderDetailDto()));

        when(userService.getUserDetail(username)).thenReturn(userDto());
        when(orderService.generateOrderNo(any())).thenReturn("ORD5-20250101000000-ABC123");
        when(orderDetailService.populateOrderDetail(any(), anyInt(), anyString()))
                .thenReturn(List.of(new OrderDetail().setAmount(BigDecimal.valueOf(100))));
        when(orderPromotionService.applyPromotions(any(), any(), anyString()))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    order.setDiscountTotal(new BigDecimal("10.00"));
                    return new CalculatedOrder(order, List.of(), new BigDecimal("10.00"));
                });

        Order result = orderBusinessService.calculate(orderDto, username);

        assertEquals(new BigDecimal("10.00"), result.getDiscountTotal());
        assertEquals(new BigDecimal("90.00"), result.getGrandTotal());
    }

    @Test
    void confirm_shouldCalculatePersistAndReturnConfirmedOrder() {
        var username = "test-user";
        var orderDto = new OrderDto();
        orderDto.setCustomerId(1);
        orderDto.setOrderDetailDtos(List.of(new OrderDetailDto()));

        when(userService.getUserDetail(username)).thenReturn(userDto());
        when(orderService.generateOrderNo(any())).thenReturn("ORD5-20250101000000-ABC123");
        when(orderDetailService.populateOrderDetail(any(), anyInt(), anyString()))
                .thenReturn(List.of(new OrderDetail().setAmount(BigDecimal.valueOf(50))));
        promotionsGrantNothing();
        when(orderService.insertWithIdempotency(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderBusinessService.confirm(orderDto, username);

        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        verify(orderService).insertWithIdempotency(result, null);
    }

    @Test
    void confirm_withIdempotencyKey_whenNew_shouldPersistWithKey() {
        var username = "test-user";
        var orderDto = new OrderDto();
        orderDto.setCustomerId(1);
        orderDto.setOrderDetailDtos(List.of(new OrderDetailDto()));

        when(userService.getUserDetail(username)).thenReturn(userDto());
        when(orderService.generateOrderNo(any())).thenReturn("ORD5-20250101000000-ABC123");
        when(orderDetailService.populateOrderDetail(any(), anyInt(), anyString()))
                .thenReturn(List.of(new OrderDetail().setAmount(BigDecimal.valueOf(50))));
        promotionsGrantNothing();
        when(orderService.insertWithIdempotency(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderBusinessService.confirm(orderDto, username, "idem-key-1");

        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        verify(orderService).insertWithIdempotency(result, "idem-key-1");
    }

    @Test
    void confirm_withIdempotencyKey_whenExisting_shouldReturnExistingOrder() {
        var existingOrder = new Order().setId(42).setOrderNo("ORD5-EXISTING");
        var username = "test-user";
        var orderDto = new OrderDto();
        orderDto.setCustomerId(1);
        orderDto.setOrderDetailDtos(List.of(new OrderDetailDto()));

        when(userService.getUserDetail(username)).thenReturn(userDto());
        when(orderService.generateOrderNo(any())).thenReturn("ORD5-20250101000000-ABC123");
        when(orderDetailService.populateOrderDetail(any(), anyInt(), anyString()))
                .thenReturn(List.of(new OrderDetail().setAmount(BigDecimal.valueOf(50))));
        promotionsGrantNothing();
        when(orderService.insertWithIdempotency(any(), eq("idem-key-1"))).thenReturn(existingOrder);

        Order result = orderBusinessService.confirm(orderDto, username, "idem-key-1");

        assertEquals(42, result.getId().intValue());
        assertEquals("ORD5-EXISTING", result.getOrderNo());
    }

    @Test
    void confirm_withIdempotencyKey_whenNullKey_shouldDelegateNormally() {
        var username = "test-user";
        var orderDto = new OrderDto();
        orderDto.setCustomerId(1);
        orderDto.setOrderDetailDtos(List.of(new OrderDetailDto()));

        when(userService.getUserDetail(username)).thenReturn(userDto());
        when(orderService.generateOrderNo(any())).thenReturn("ORD5-20250101000000-ABC123");
        when(orderDetailService.populateOrderDetail(any(), anyInt(), anyString()))
                .thenReturn(List.of(new OrderDetail().setAmount(BigDecimal.valueOf(50))));
        promotionsGrantNothing();
        when(orderService.insertWithIdempotency(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderBusinessService.confirm(orderDto, username, null);

        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        verify(orderService).insertWithIdempotency(result, null);
    }

    @Test
    void confirm_shouldRecordThePromotionAuditOnceTheOrderLinesExist() {
        var username = "test-user";
        var orderDto = new OrderDto();
        orderDto.setOrderDetailDtos(List.of(new OrderDetailDto()));
        var orderDetails = List.of(new OrderDetail()
                .setId(500)
                .setAmount(new BigDecimal("100.00"))
                .setOrderDetailSkus(List.of(new OrderDetailSku().setId(9001).setAmount(new BigDecimal("100.00")))));

        when(userService.getUserDetail(username)).thenReturn(userDto());
        when(orderService.generateOrderNo(any())).thenReturn("ORD5-20250101000000-ABC123");
        when(orderDetailService.populateOrderDetail(any(), anyInt(), anyString())).thenReturn(orderDetails);
        when(orderPromotionService.applyPromotions(any(), any(), anyString()))
                .thenAnswer(invocation -> new CalculatedOrder(invocation.getArgument(0),
                        List.of(new CalculatedOrder.LineDiscount(null,
                                List.of(new CalculatedOrder.PromotionDiscount(99L, "HAPPY10", "Happy Hour",
                                        "PERCENTAGE", new BigDecimal("10.00"), new BigDecimal("10.00"))))),
                        new BigDecimal("10.00")));
        when(orderPromotionService.toSnapshots(any())).thenReturn(List.of(snapshot()));
        when(orderService.insertWithIdempotency(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));

        orderBusinessService.confirm(orderDto, username);

        verify(orderItemDiscountService).recordSnapshot(anyList());
    }

    @Test
    void confirm_shouldNotRecordTheAuditOnAnIdempotentReplay() {
        var username = "test-user";
        var orderDto = new OrderDto();
        orderDto.setOrderDetailDtos(List.of(new OrderDetailDto()));
        var existingOrder = new Order().setId(42).setOrderNo("ORD5-EXISTING");

        when(userService.getUserDetail(username)).thenReturn(userDto());
        when(orderService.generateOrderNo(any())).thenReturn("ORD5-20250101000000-ABC123");
        when(orderDetailService.populateOrderDetail(any(), anyInt(), anyString()))
                .thenReturn(List.of(new OrderDetail().setAmount(new BigDecimal("100.00"))));
        when(orderPromotionService.applyPromotions(any(), any(), anyString()))
                .thenAnswer(invocation -> new CalculatedOrder(invocation.getArgument(0),
                        List.of(new CalculatedOrder.LineDiscount(null,
                                List.of(new CalculatedOrder.PromotionDiscount(99L, "HAPPY10", "Happy Hour",
                                        "PERCENTAGE", new BigDecimal("10.00"), new BigDecimal("10.00"))))),
                        new BigDecimal("10.00")));
        when(orderService.insertWithIdempotency(any(), eq("idem-key-1"))).thenReturn(existingOrder);

        orderBusinessService.confirm(orderDto, username, "idem-key-1");

        verify(orderItemDiscountService, never()).recordSnapshot(anyList());
    }

    @Test
    void confirm_shouldNotCallTheMenuServiceWhenNothingWasDiscounted() {
        var username = "test-user";
        var orderDto = new OrderDto();
        orderDto.setOrderDetailDtos(List.of(new OrderDetailDto()));

        when(userService.getUserDetail(username)).thenReturn(userDto());
        when(orderService.generateOrderNo(any())).thenReturn("ORD5-20250101000000-ABC123");
        when(orderDetailService.populateOrderDetail(any(), anyInt(), anyString()))
                .thenReturn(List.of(new OrderDetail().setAmount(new BigDecimal("100.00"))));
        promotionsGrantNothing();
        when(orderPromotionService.toSnapshots(any())).thenReturn(List.of());
        when(orderService.insertWithIdempotency(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));

        orderBusinessService.confirm(orderDto, username);

        verify(orderItemDiscountService, never()).recordSnapshot(anyList());
    }

    @Test
    void confirm_shouldRecordEveryPromotionAppliedToALine() {
        var username = "test-user";
        var orderDto = new OrderDto();
        orderDto.setOrderDetailDtos(List.of(new OrderDetailDto()));

        when(userService.getUserDetail(username)).thenReturn(userDto());
        when(orderService.generateOrderNo(any())).thenReturn("ORD5-20250101000000-ABC123");
        when(orderDetailService.populateOrderDetail(any(), anyInt(), anyString()))
                .thenReturn(List.of(new OrderDetail().setAmount(new BigDecimal("100.00"))));
        when(orderPromotionService.applyPromotions(any(), any(), anyString()))
                .thenAnswer(invocation -> new CalculatedOrder(invocation.getArgument(0),
                        List.of(new CalculatedOrder.LineDiscount(null,
                                List.of(promotionDiscount(99L, "5.00"), promotionDiscount(100L, "2.00")))),
                        new BigDecimal("7.00")));
        when(orderPromotionService.toSnapshots(any()))
                .thenReturn(List.of(snapshot(), snapshot(), snapshot()));
        when(orderService.insertWithIdempotency(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));

        orderBusinessService.confirm(orderDto, username);

        verify(orderItemDiscountService).recordSnapshot(argThat(rows -> rows.size() == 3));
    }

    private static CalculatedOrder.PromotionDiscount promotionDiscount(Long promotionId, String amount) {
        return new CalculatedOrder.PromotionDiscount(promotionId, "HAPPY10", "Happy Hour",
                "PERCENTAGE", new BigDecimal("10.00"), new BigDecimal(amount));
    }


    private static OrderItemDiscount recordedDiscount(Long orderItemId, Long promotionId, String code,
                                                      String amount) {
        return new OrderItemDiscount()
                .setOrderItemId(orderItemId)
                .setPromotionId(promotionId)
                .setPromotionCode(code)
                .setPromotionName("Happy Hour")
                .setDiscountAmount(new BigDecimal(amount));
    }

    @Test
    void confirm_shouldAttachTheRecordedDiscountsToTheLineTheyWereTakenOff() {
        var username = "test-user";
        var orderDto = new OrderDto();
        orderDto.setOrderDetailDtos(List.of(new OrderDetailDto()));
        var orderDetails = List.of(new OrderDetail()
                .setId(500)
                .setAmount(new BigDecimal("100.00"))
                .setOrderDetailSkus(List.of(
                        new OrderDetailSku().setId(9001).setAmount(new BigDecimal("60.00")),
                        new OrderDetailSku().setId(9002).setAmount(new BigDecimal("40.00")))));

        when(userService.getUserDetail(username)).thenReturn(userDto());
        when(orderService.generateOrderNo(any())).thenReturn("ORD5-20250101000000-ABC123");
        when(orderDetailService.populateOrderDetail(any(), anyInt(), anyString())).thenReturn(orderDetails);
        promotionsGrantNothing();
        when(orderPromotionService.toSnapshots(any())).thenReturn(List.of(snapshot()));
        when(orderItemDiscountService.recordSnapshot(anyList())).thenReturn(List.of(
                recordedDiscount(9001L, 99L, "HAPPY10", "6.00"),
                recordedDiscount(9002L, 100L, "SAVE5", "2.00")));
        when(orderService.insertWithIdempotency(any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order confirmed = orderBusinessService.confirm(orderDto, username);

        var first = confirmed.getOrderDetails().get(0).getOrderDetailSkus().get(0);
        var second = confirmed.getOrderDetails().get(0).getOrderDetailSkus().get(1);
        assertEquals(1, first.getDiscounts().size());
        assertEquals("HAPPY10", first.getDiscounts().get(0).getPromotionCode());
        assertEquals(0, new BigDecimal("6.00").compareTo(first.getDiscounts().get(0).getDiscountAmount()));
        assertEquals(1, second.getDiscounts().size());
        assertEquals("SAVE5", second.getDiscounts().get(0).getPromotionCode());
    }

    @Test
    void confirm_shouldLeaveALineWithNoPromotionWithoutADiscountList() {
        var username = "test-user";
        var orderDto = new OrderDto();
        orderDto.setOrderDetailDtos(List.of(new OrderDetailDto()));
        var orderDetails = List.of(new OrderDetail()
                .setId(500)
                .setAmount(new BigDecimal("100.00"))
                .setOrderDetailSkus(List.of(
                        new OrderDetailSku().setId(9001),
                        new OrderDetailSku().setId(9002))));

        when(userService.getUserDetail(username)).thenReturn(userDto());
        when(orderService.generateOrderNo(any())).thenReturn("ORD5-20250101000000-ABC123");
        when(orderDetailService.populateOrderDetail(any(), anyInt(), anyString())).thenReturn(orderDetails);
        promotionsGrantNothing();
        when(orderPromotionService.toSnapshots(any())).thenReturn(List.of(snapshot()));
        when(orderItemDiscountService.recordSnapshot(anyList()))
                .thenReturn(List.of(recordedDiscount(9001L, 99L, "HAPPY10", "6.00")));
        when(orderService.insertWithIdempotency(any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order confirmed = orderBusinessService.confirm(orderDto, username);

        assertNotNull(confirmed.getOrderDetails().get(0).getOrderDetailSkus().get(0).getDiscounts());
        assertNull(confirmed.getOrderDetails().get(0).getOrderDetailSkus().get(1).getDiscounts());
    }

    @Test
    void confirm_shouldNotMislayADiscountWhoseLineCannotBeFound() {
        var username = "test-user";
        var orderDto = new OrderDto();
        orderDto.setOrderDetailDtos(List.of(new OrderDetailDto()));
        var orderDetails = List.of(new OrderDetail()
                .setId(500)
                .setAmount(new BigDecimal("100.00"))
                .setOrderDetailSkus(List.of(new OrderDetailSku().setId(9001))));

        when(userService.getUserDetail(username)).thenReturn(userDto());
        when(orderService.generateOrderNo(any())).thenReturn("ORD5-20250101000000-ABC123");
        when(orderDetailService.populateOrderDetail(any(), anyInt(), anyString())).thenReturn(orderDetails);
        promotionsGrantNothing();
        when(orderPromotionService.toSnapshots(any())).thenReturn(List.of(snapshot()));
        when(orderItemDiscountService.recordSnapshot(anyList()))
                .thenReturn(List.of(recordedDiscount(9999L, 99L, "HAPPY10", "6.00")));
        when(orderService.insertWithIdempotency(any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order confirmed = orderBusinessService.confirm(orderDto, username);

        var sku = confirmed.getOrderDetails().get(0).getOrderDetailSkus().get(0);
        assertNull(sku.getDiscounts(), "a discount for an unknown line must not land on another line");
    }

    @Test
    void confirm_shouldReturnTheCustomizationsAlreadyOnTheDetail() {
        var username = "test-user";
        var orderDto = new OrderDto();
        orderDto.setOrderDetailDtos(List.of(new OrderDetailDto()));
        var option = new OrderDetailCustomizationOption()
                .setCustomizationOptionsId(101)
                .setCustomizationOptionName("Large")
                .setPrice(new BigDecimal("1.50"))
                .setAmount(new BigDecimal("3.00"));
        var customization = new OrderDetailCustomization()
                .setCustomizationId(1)
                .setCustomizationName("Size")
                .setOptions(List.of(option));
        var orderDetails = List.of(new OrderDetail()
                .setId(500)
                .setAmount(new BigDecimal("103.00"))
                .setOrderDetailSkus(List.of(new OrderDetailSku()
                        .setId(9001)
                        .setAmount(new BigDecimal("100.00"))))
                .setCustomizations(List.of(customization)));

        when(userService.getUserDetail(username)).thenReturn(userDto());
        when(orderService.generateOrderNo(any())).thenReturn("ORD5-20250101000000-ABC123");
        when(orderDetailService.populateOrderDetail(any(), anyInt(), anyString())).thenReturn(orderDetails);
        promotionsGrantNothing();
        when(orderPromotionService.toSnapshots(any())).thenReturn(List.of());
        when(orderService.insertWithIdempotency(any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order confirmed = orderBusinessService.confirm(orderDto, username);

        var detail = confirmed.getOrderDetails().get(0);
        assertEquals(1, detail.getCustomizations().size());
        assertEquals("Size", detail.getCustomizations().get(0).getCustomizationName());
        assertEquals("Large", detail.getCustomizations().get(0).getOptions().get(0).getCustomizationOptionName());
    }

    private static OrderItemDiscountSnapshotDto snapshot() {
        OrderItemDiscountSnapshotDto dto = new OrderItemDiscountSnapshotDto();
        dto.setOrderItemId(9001L);
        dto.setPromotionId(99L);
        dto.setPromotionCode("HAPPY10");
        dto.setPromotionName("Happy Hour");
        dto.setDiscountType("PERCENTAGE");
        dto.setDiscountValue(new BigDecimal("10.00"));
        dto.setDiscountAmount(new BigDecimal("10.00"));
        return dto;
    }

    @Test
    void print_shouldCalculateWithoutPersisting() {
        var username = "test-user";
        var orderDto = new OrderDto();
        orderDto.setCustomerId(1);
        orderDto.setOrderDetailDtos(List.of(new OrderDetailDto()));

        when(userService.getUserDetail(username)).thenReturn(userDto());
        when(orderService.generateOrderNo(any())).thenReturn("ORD5-20250101000000-ABC123");
        when(orderDetailService.populateOrderDetail(any(), anyInt(), anyString()))
                .thenReturn(List.of(new OrderDetail().setAmount(BigDecimal.valueOf(50))));
        promotionsGrantNothing();

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
        var expectedOrders = List.of(new Order().setId(1));

        when(userService.getUserDetail(username)).thenReturn(userDto());
        when(orderService.selectByStoreId(any(), any(Timestamp.class), any(Timestamp.class)))
                .thenReturn(expectedOrders);

        List<Order> result = orderBusinessService.getDailyListByStore(username);

        assertEquals(1, result.size());
    }
}
