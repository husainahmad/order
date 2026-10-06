package com.harmoni.pos.order.business.service.promotion;

import com.harmoni.pos.order.model.CalculatedOrder;
import com.harmoni.pos.order.model.Order;
import com.harmoni.pos.order.model.dto.UserDto;
import com.harmoni.pos.order.model.dto.pricing.OrderItemDiscountSnapshotDto;

import java.util.List;

/**
 * Applies the promotion catalogue to an order as it is being calculated.
 * <p>
 * The order service has no promotion data of its own. It asks the menu service what a
 * basket is worth and writes the answer onto the order, so a client's own idea of what
 * the order costs is never consulted.
 *
 * @author husainahmad
 */
public interface OrderPromotionService {

    /**
     * Prices the order's lines against the live promotion catalogue and writes the
     * resulting discount onto each line and onto the order.
     * <p>
     * Line amounts are left at their gross value. The saving is recorded separately on
     * {@link OrderDetail#getDiscountAmount()}, per line, and on
     * {@link Order#getDiscountTotal()} for the order as a whole, so the subtotal still
     * reports the full value of the basket and a discounted basket stays comparable to an
     * undiscounted one in reporting.
     *
     * @param order    the order with its lines already priced at gross
     * @param userDto  the acting operator, used for the store and its time zone
     * @param username the operator the sale is being made for
     * @return the order together with the discount granted on each line
     */
    CalculatedOrder applyPromotions(Order order, UserDto userDto, String username);

    /**
     * Turns the discount granted on each line into the audit rows the menu service
     * records, now that the order has been inserted and the lines have their IDs.
     * <p>
     * Lines are walked in the same order {@link #applyPromotions} flattened them, so each
     * discount lands on the line it was priced for rather than on whichever line happens
     * to share its position.
     *
     * @param calculatedOrder the result of {@link #applyPromotions}, with its order now persisted
     * @return one audit row per promotion applied, empty when nothing was discounted
     */
    List<OrderItemDiscountSnapshotDto> toSnapshots(CalculatedOrder calculatedOrder);
}
