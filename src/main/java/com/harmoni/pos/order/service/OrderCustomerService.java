package com.harmoni.pos.order.service;

import com.harmoni.pos.order.model.Order;
import com.harmoni.pos.order.model.OrderCustomer;

import java.util.Date;
import java.util.List;

/**
 * Captures who an order was sold to, and reads back what a customer has bought.
 * <p>
 * The customer service owns customer identity. This only records the two identifying
 * fields against the order, so history survives a later rename or a soft delete.
 */
public interface OrderCustomerService {

    /**
     * Records the customer snapshot for a confirmed order.
     * <p>
     * A walk-in sale writes no row. There is no customer to describe, so a row of nulls
     * would be a record of nothing, and {@code customer_id IS NULL} matching every
     * walk-in in the shop is a trap for anyone querying history later.
     *
     * @param order the confirmed order, carrying the customer it was sold to
     * @return the recorded snapshot, or {@code null} for a walk-in sale
     */
    OrderCustomer record(Order order);

    /**
     * Retrieves the customer snapshot for an order.
     *
     * @param orderId the order ID
     * @return the snapshot, or {@code null} when the order has none
     */
    OrderCustomer getByOrderId(Integer orderId);

    /**
     * Retrieves what a customer has ordered, most recent first.
     *
     * @param customerId the customer in the customer service
     * @param from       optional inclusive lower bound on the order timestamp
     * @param to         optional inclusive upper bound on the order timestamp
     * @return the customer's orders, most recent first, never including walk-in sales
     */
    List<OrderCustomer> getHistory(Long customerId, Date from, Date to);

    /**
     * Deletes the customer snapshot for an order.
     *
     * @param orderId the order ID
     * @return the number of deleted rows
     */
    int deleteByOrderId(Integer orderId);
}
