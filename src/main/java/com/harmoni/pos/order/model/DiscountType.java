package com.harmoni.pos.order.model;

/**
 * Enumeration of how a discount was finally computed and snapshotted onto an
 * order line.
 * <p>
 * Persisted as {@code order_item_discounts.discount_type}. The row is an immutable
 * audit record: {@code promotion_code} and {@code promotion_name} are denormalised
 * on purpose so historic orders stay readable after a promotion is renamed or
 * deleted. {@link #MANUAL} covers discounts applied directly by a cashier without
 * any promotion attached.
 */
public enum DiscountType {

    /** Percentage off, resolved at checkout time. */
    PERCENTAGE,

    /** Fixed monetary amount off, resolved at checkout time. */
    FIXED_AMOUNT,

    /** Price replaced by a promotional special price. */
    SPECIAL_PRICE,

    /** Manually keyed by a cashier, not linked to a promotion. */
    MANUAL
}
