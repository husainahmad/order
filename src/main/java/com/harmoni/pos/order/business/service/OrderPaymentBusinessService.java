package com.harmoni.pos.order.business.service;

import com.harmoni.pos.order.model.Order;
import com.harmoni.pos.order.model.dto.OrderPaymentDto;

/**
 * {@code OrderPaymentBusinessService} handles the business logic for processing
 * order payments, ensuring proper status update, validation, and payment record creation.
 */
public interface OrderPaymentBusinessService {

    /**
     * Processes a payment for a given order.
     * <p>
     * This involves:
     * <ul>
     *     <li>Validating that the order belongs to the same store as the requesting user</li>
     *     <li>Updating the order status to {@code PAID}</li>
     *     <li>Persisting a new {@code OrderPayment} record</li>
     * </ul>
     *
     * @param paymentDto the payment details including the order ID and method
     * @param token      JWT token used to authenticate and authorize the user
     * @return the updated {@link Order} with payment information
     * @throws com.harmoni.pos.order.exception.BusinessUnAuthorizedRequestException
     *         if the order does not belong to the authenticated user's store
     */
    Order processPayment(OrderPaymentDto paymentDto, String token);
}
