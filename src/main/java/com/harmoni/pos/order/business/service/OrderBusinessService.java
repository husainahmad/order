package com.harmoni.pos.order.business.service;

import com.harmoni.pos.order.model.Order;
import com.harmoni.pos.order.model.dto.OrderDto;

import java.util.List;

/**
 * {@code OrderBusinessService} defines the business logic operations
 * related to order lifecycle management, such as calculations,
 * confirmation, voiding, and retrieving daily orders.
 */
public interface OrderBusinessService {

    /**
     * Calculates the subtotal based on order details' individual amounts.
     *
     * @param order the order for which the subtotal should be calculated
     */
    void calculateSubtotal(Order order);

    /**
     * Calculates the service charge and sets it on the order.
     * Default implementation sets it to zero unless overridden.
     *
     * @param order the order to calculate the service charge for
     */
    void calculateServiceCharge(Order order);

    /**
     * Calculates and applies the discount from the order DTO.
     * Automatically recalculates the grand total afterward.
     *
     * @param order     the order object to apply the discount to
     * @param orderDto  the source DTO containing the discount information
     */
    void calculateDiscount(Order order, OrderDto orderDto);

    /**
     * Calculates and applies tax to the order.
     * Default implementation sets it to zero unless overridden.
     *
     * @param order     the order object to apply tax to
     * @param orderDto  the source DTO which may affect tax calculation
     */
    void calculateTax(Order order, OrderDto orderDto);

    /**
     * Calculates and sets the grand total of the order.
     * Includes subtotal, tax, service charge, and subtracts discount.
     *
     * @param order the order to calculate the grand total for
     */
    void calculateGrandTotal(Order order);

    /**
     * Performs a full calculation and preparation of an order from the given DTO.
     * Populates store, customer, order number, details, and all totals.
     *
     * @param orderDto the DTO containing the incoming order data
     * @param username the username used for fetching user/store context
     * @return the fully calculated {@link Order}
     */
    Order calculate(OrderDto orderDto, String username);

    /**
     * Finalizes and persists the given order as a confirmed order.
     *
     * @param orderDto the DTO containing the order data to confirm
     * @param username the username used for user/store validation
     * @return the confirmed {@link Order}
     */
    Order confirm(OrderDto orderDto, String username);

    /**
     * Prepares an order for printing purposes without saving it.
     *
     * @param orderDto the DTO containing the order data
     * @param username the username used for user/store validation
     * @return the prepared {@link Order} with totals populated
     */
    Order print(OrderDto orderDto, String username);

    /**
     * Marks an order as void (canceled).
     *
     * @param orderId  the ID of the order to void
     * @param username the username used for authorization
     * @return the voided {@link Order}
     */
    Order voidOrder(Integer orderId, String username);

    /**
     * Retrieves a list of confirmed orders for the current store for the current day.
     *
     * @param username the username used to determine the store and time zone
     * @return a list of today's orders for the store
     */
    List<Order> getDailyListByStore(String username);
}
