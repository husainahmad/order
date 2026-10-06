package com.harmoni.pos.order.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDetailSku {
    private Integer id;
    private Integer orderDetailId;
    private Integer skuId;
    private String skuName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal amount;

    /**
     * What the promotion engine took off this line, zero when no promotion applied.
     * <p>
     * {@link #amount} stays the gross figure the customer would have paid without a
     * promotion, so the order's subtotal remains the full value of the basket and the
     * saving is reported once at order level. This column is the per line breakdown of
     * that saving, and is the line identifier the menu service records its promotion
     * audit rows against.
     */
    private BigDecimal discountAmount;
    private Date createdAt;

    /**
     * The promotions applied to this line, one entry per promotion that took something off.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<OrderItemDiscount> discounts;
}
