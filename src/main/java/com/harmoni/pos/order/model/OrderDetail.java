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
public class OrderDetail {
    private Integer id;
    private Integer orderId;
    private Integer productId;
    private String productName;
    private Integer categoryId;
    private String categoryName;
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
    private Date updatedAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<OrderDetailSku> orderDetailSkus;

    /**
     * Customization choices made on this detail, grouped by customization and with their
     * selected options.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<OrderDetailCustomization> customizations;
}
