package com.harmoni.pos.order.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Entity representing a discount that was applied to a single order line.
 * <p>
 * This is an append-only audit record written when an order is confirmed. It lives
 * in the order database, next to the {@link OrderDetailSku} rows it points at,
 * because that is the only service that can read them back. The amounts are not
 * decided here: the menu service prices the basket and this records what it said,
 * so the row is a faithful copy of an engine result rather than a second opinion.
 * <p>
 * The promotion reference is nullable because a cashier may apply a
 * {@link DiscountType#MANUAL} discount with no promotion attached. The promotion
 * code and name are copied rather than joined on purpose: once the snapshot is
 * taken the historic order line must stay readable even if the promotion is later
 * renamed, deactivated or deleted.
 *
 * @author husainahmad
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderItemDiscount {

    /**
     * The unique identifier of the order item discount.
     */
    private Long id;

    /**
     * The order line this discount was applied to.
     */
    private Long orderItemId;

    /**
     * The promotion that produced the discount, {@code null} for a manual discount.
     */
    private Long promotionId;

    /**
     * The denormalised promotion code, {@code null} for a manual discount.
     */
    private String promotionCode;

    /**
     * The denormalised promotion name, {@code null} for a manual discount.
     */
    private String promotionName;

    /**
     * How the discount was computed, see {@link DiscountType}.
     */
    private DiscountType discountType;

    /**
     * The discount magnitude before it was resolved against the line amount: a
     * percentage for {@link DiscountType#PERCENTAGE}, otherwise the monetary value.
     */
    private BigDecimal discountValue;

    /**
     * The monetary discount actually granted on the line.
     */
    private BigDecimal discountAmount;

    /**
     * The timestamp the discount was snapshotted.
     */
    private Date createdAt;
}
