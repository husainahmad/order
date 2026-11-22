package com.harmoni.pos.order.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmoni.pos.order.model.Order;
import com.harmoni.pos.order.model.OrderDetail;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionEventPayload {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Integer orderId;
    private Integer customerId;
    private Integer storeId;
    private BigDecimal totalAmount;
    private Date paidAt;
    private List<TransactionItem> items;

    public static TransactionEventPayload from(Order order) {
        List<TransactionItem> items = order.getOrderDetails() == null
                ? List.of()
                : order.getOrderDetails().stream()
                        .map(detail -> new TransactionItem()
                                .setProductId(detail.getProductId())
                                .setCategoryId(detail.getCategoryId())
                                .setAmount(detail.getAmount())
                                .setDiscountAmount(detail.getDiscountAmount()))
                        .toList();
        Integer customerId = order.getCustomerId() != null && order.getCustomerId() > 0
                ? order.getCustomerId()
                : null;
        return new TransactionEventPayload()
                .setOrderId(order.getId())
                .setCustomerId(customerId)
                .setStoreId(order.getStoreId())
                .setTotalAmount(order.getGrandTotal())
                .setPaidAt(order.getCreatedAt())
                .setItems(items);
    }

    public static String toJson(Order order) {
        try {
            return MAPPER.writeValueAsString(from(order));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize transaction event for order " + order.getId(), e);
        }
    }

    @Data
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TransactionItem {
        private Integer productId;
        private Integer categoryId;
        private BigDecimal amount;
        private BigDecimal discountAmount;
    }
}
