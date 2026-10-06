package com.harmoni.pos.order.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * What the promotion engine granted on one confirmed order, held between pricing the
 * basket and the order rows existing.
 * <p>
 * Pricing has to happen before the insert, because the grand total is written into the
 * order row and must already have the discount applied. The audit rows the menu service
 * keeps, on the other hand, need the generated order line IDs. So the outcome of pricing
 * is carried in this object until the insert has run and can be turned into audit rows
 * pointing at real rows.
 *
 * @param order          the order as calculated, not yet persisted
 * @param lineDiscounts  the discount granted on each order line, in the same order the
 *                       lines appear on the order
 * @param totalDiscount  the sum of every line discount
 * @author husainahmad
 */
public record CalculatedOrder(Order order,
                              List<LineDiscount> lineDiscounts,
                              BigDecimal totalDiscount) {

    /**
     * The discount granted on a single order line.
     *
     * @param orderItemId the {@code order_detail_skus.id} the discount belongs to, set
     *                    only once the order has been inserted
     * @param discounts   the individual promotions applied to that line
     */
    public record LineDiscount(Integer orderItemId,
                               List<PromotionDiscount> discounts) {
    }

    /**
     * One promotion's contribution to a line.
     *
     * @param promotionId    the promotion that was applied
     * @param promotionCode  the promotion code, snapshotted so the row stays readable
     *                       after the promotion changes
     * @param promotionName  the promotion name, snapshotted for the same reason
     * @param discountType   how the discount was computed
     * @param discountValue  the magnitude before it was resolved against the line
     * @param discountAmount what it actually took off the line
     */
    public record PromotionDiscount(Long promotionId,
                                    String promotionCode,
                                    String promotionName,
                                    String discountType,
                                    BigDecimal discountValue,
                                    BigDecimal discountAmount) {
    }
}
