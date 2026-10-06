package com.harmoni.pos.order.model.dto.pricing;

import com.harmoni.pos.order.exception.BusinessBadRequestException;
import com.harmoni.pos.order.model.DiscountType;
import com.harmoni.pos.order.model.OrderItemDiscount;
import lombok.Data;

import java.math.BigDecimal;

/**
 * A discount the menu service already computed, sent back to be recorded as the audit
 * trail for a confirmed order line.
 * <p>
 * The order item ID is the order service's own {@code order_detail_skus.id}, which only
 * exists once the order has been inserted, and is why recording is a separate call from
 * pricing. The promotion code, name and amount are whatever the menu service priced,
 * passed through untouched: the order service has no catalogue of its own to recompute
 * them from, and must not try.
 *
 * @author husainahmad
 */
@Data
public class OrderItemDiscountSnapshotDto {

    /**
     * The order service's {@code order_detail_skus.id} for the discounted line.
     */
    private Long orderItemId;

    private Long promotionId;

    private String promotionCode;

    private String promotionName;

    private String discountType;

    private BigDecimal discountValue;

    private BigDecimal discountAmount;

    /**
     * Converts this snapshot into the entity to persist, timestamp left unset for the
     * service to stamp once for the whole batch.
     * <p>
     * The discount type arrives as a string because it crossed the wire as one. It is
     * turned back into the enum here rather than stored loose, so a value neither side
     * recognises fails the checkout instead of sitting in the audit table unreadable.
     *
     * @return the entity carrying the caller supplied values
     */
    public OrderItemDiscount toOrderItemDiscount() {
        return new OrderItemDiscount()
                .setOrderItemId(orderItemId)
                .setPromotionId(promotionId)
                .setPromotionCode(promotionCode)
                .setPromotionName(promotionName)
                .setDiscountType(parseDiscountType())
                .setDiscountValue(discountValue)
                .setDiscountAmount(discountAmount);
    }

    private DiscountType parseDiscountType() {
        if (discountType == null) {
            return null;
        }
        try {
            return DiscountType.valueOf(discountType);
        } catch (IllegalArgumentException e) {
            throw new BusinessBadRequestException(
                    "exception.orderItemDiscount.badRequest.unknownDiscountType", new Object[]{discountType});
        }
    }
}
