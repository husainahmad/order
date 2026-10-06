package com.harmoni.pos.order.service;

import com.harmoni.pos.order.model.OrderItemDiscount;
import com.harmoni.pos.order.model.dto.pricing.OrderItemDiscountSnapshotDto;

import java.util.List;

/**
 * Records the promotion discounts attached to order lines.
 * <p>
 * The amounts are never decided in this service. The menu service prices the basket
 * and this stores what it said, verbatim, so the audit row always agrees with the
 * total already written into the order. There is deliberately no method that accepts
 * a caller supplied amount: the order service has no promotion catalogue to check one
 * against, and a discount it invented would be a discount nobody can explain.
 */
public interface OrderItemDiscountService {

    /**
     * Records discounts the promotion engine has already computed, storing the
     * supplied amounts, promotion code and promotion name verbatim.
     * <p>
     * Re-running the engine here would be wrong: the catalogue could have moved on
     * since the basket was priced, and the audit row would then disagree with the
     * total already written into the order. So the engine's answer is copied, not
     * recomputed.
     *
     * @param snapshots the engine results to record, already carrying the real order line IDs
     * @return the recorded discounts, empty when nothing was submitted
     */
    List<OrderItemDiscount> recordSnapshot(List<OrderItemDiscountSnapshotDto> snapshots);
}
