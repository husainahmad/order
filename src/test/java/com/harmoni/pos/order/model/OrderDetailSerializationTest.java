package com.harmoni.pos.order.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that an order actually carries its customizations and discounts in the JSON
 * handed back to a client.
 * <p>
 * Customizations live on the {@link OrderDetail} and discounts on the
 * {@link OrderDetailSku}; both lists are {@code NON_NULL}, so the wiring is easy to get
 * subtly wrong: a field that is populated in memory but never serialized, or stripped
 * as null, would leave a receipt printing "no customizations" for a customised coffee.
 * These assertions are on the serialized form rather than the object, because that is
 * what the client sees.
 */
class OrderDetailSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static OrderDetailCustomization customization() {
        var option = new OrderDetailCustomizationOption()
                .setCustomizationOptionsId(101)
                .setCustomizationOptionName("Large")
                .setPrice(new BigDecimal("1.50"))
                .setAmount(new BigDecimal("3.00"));
        return new OrderDetailCustomization()
                .setCustomizationId(1)
                .setCustomizationName("Size")
                .setOptions(List.of(option));
    }

    private static OrderDetailSku lineWithDiscount() {
        OrderItemDiscount discount = new OrderItemDiscount()
                .setOrderItemId(9001L)
                .setPromotionId(99L)
                .setPromotionCode("HAPPY10")
                .setPromotionName("Happy Hour")
                .setDiscountAmount(new BigDecimal("6.00"));

        return new OrderDetailSku()
                .setId(9001)
                .setSkuId(100)
                .setSkuName("Regular Coffee")
                .setQuantity(2)
                .setPrice(new BigDecimal("50.00"))
                .setAmount(new BigDecimal("100.00"))
                .setDiscountAmount(new BigDecimal("6.00"))
                .setDiscounts(List.of(discount));
    }

    @Test
    void orderDetailShouldSerializeCustomizationsAndDiscounts() throws Exception {
        Order order = new Order()
                .setId(7)
                .setOrderNo("ORD-1")
                .setOrderDetails(List.of(new OrderDetail()
                        .setId(500)
                        .setProductName("Coffee")
                        .setAmount(new BigDecimal("103.00"))
                        .setOrderDetailSkus(List.of(lineWithDiscount()))
                        .setCustomizations(List.of(customization()))));

        String json = objectMapper.writeValueAsString(order);

        assertTrue(json.contains("\"customizations\""), "order detail must carry the customization choices");
        assertTrue(json.contains("\"discounts\""), "order line must carry the discounts");
        assertTrue(json.contains("\"customizationOptionName\":\"Large\""));
        assertTrue(json.contains("\"promotionCode\":\"HAPPY10\""));
        assertTrue(json.contains("\"customizationName\":\"Size\""));
    }

    @Test
    void anUncustomisedUndiscountedOrderShouldOmitBothLists() throws Exception {
        Order order = new Order()
                .setId(7)
                .setOrderNo("ORD-1")
                .setOrderDetails(List.of(new OrderDetail()
                        .setId(500)
                        .setOrderDetailSkus(List.of(new OrderDetailSku()
                                .setId(9001)
                                .setSkuId(100)
                                .setQuantity(1)
                                .setAmount(new BigDecimal("50.00"))))));

        String json = objectMapper.writeValueAsString(order);

        assertFalse(json.contains("customizations"), "a detail with no choices should not claim an empty list");
        assertFalse(json.contains("discounts"), "a line with no promotion should not claim an empty list");
    }

    @Test
    void bothListsShouldBeOmittedWhenUnsetRatherThanSerialisedAsNull() throws Exception {
        OrderDetail detail = new OrderDetail()
                .setId(500)
                .setOrderDetailSkus(List.of(lineWithDiscount().setDiscounts(List.of())));

        String json = objectMapper.writeValueAsString(detail);

        assertFalse(json.contains("customizations"), "an unset customization list should be omitted");
        assertTrue(json.contains("\"discounts\":[]"));
    }
}
