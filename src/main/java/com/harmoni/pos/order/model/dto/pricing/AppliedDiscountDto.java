package com.harmoni.pos.order.model.dto.pricing;

import lombok.Data;

import java.math.BigDecimal;

/**
 * One discount the menu service granted, as received.
 * <p>
 * The promotion code and name come back with the discount so they can be snapshotted
 * without a second lookup, and so a historic sale stays readable after the promotion is
 * renamed or deleted.
 *
 * @author husainahmad
 */
@Data
public class AppliedDiscountDto {

    private Long promotionId;

    private String promotionCode;

    private String promotionName;

    /**
     * How the discount was computed, for example {@code PERCENTAGE}. Carried as a string
     * because the order service never interprets it, only records it.
     */
    private String discountType;

    /**
     * The magnitude before it was resolved against the line.
     */
    private BigDecimal discountValue;

    /**
     * What the discount actually took off the line.
     */
    private BigDecimal discountAmount;
}
