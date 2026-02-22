package com.harmoni.pos.order.business.service;

import com.harmoni.pos.order.business.service.menu.UserService;
import com.harmoni.pos.order.component.JwtUtil;
import com.harmoni.pos.order.exception.BusinessUnAuthorizedRequestException;
import com.harmoni.pos.order.model.OrderPayment;
import com.harmoni.pos.order.model.Order;
import com.harmoni.pos.order.model.OrderStatus;
import com.harmoni.pos.order.model.dto.OrderPaymentDto;
import com.harmoni.pos.order.model.dto.UserDto;
import com.harmoni.pos.order.service.OrderPaymentService;
import com.harmoni.pos.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Implementation of {@link OrderPaymentBusinessService} that handles
 * business logic related to processing order payments.
 */
@RequiredArgsConstructor
@Service("orderPaymentBusinessService")
public class OrderPaymentBusinessServiceImpl implements OrderPaymentBusinessService {

    private final OrderService orderService;
    private final OrderPaymentService orderPaymentService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * Processes a payment for a given order:
     * <ul>
     *   <li>Validates the store ownership using the JWT token</li>
     *   <li>Updates the order status to {@link OrderStatus#PAID}</li>
     *   <li>Creates and persists an {@link OrderPayment}</li>
     * </ul>
     *
     * @param paymentDto the DTO containing payment information
     * @param token the JWT token used for authentication and authorization
     * @return the updated {@link Order} object, including its payment
     * @throws BusinessUnAuthorizedRequestException if the user does not belong to the same store
     */
    @Override
    public Order processPayment(OrderPaymentDto paymentDto, String token) {
        Order order = orderService.selectByPrimaryKey(paymentDto.getOrderId());

        UserDto userDto = userService.getUserDetail(jwtUtil.extractUsername(token), token);
        if (!userDto.getStore().getId().equals(order.getStoreId())) {
            throw new BusinessUnAuthorizedRequestException(BusinessUnAuthorizedRequestException.UN_AUTHORIZED, null);
        }

        order.setStatus(OrderStatus.PAID);
        order.setUpdatedAt(new Date(System.currentTimeMillis()));
        orderService.updateByPrimaryKey(order);

        OrderPayment orderPayment = paymentDto.toOrderPayment();
        orderPayment.setAmount(order.getGrandTotal());
        orderPayment.setCreatedAt(new Date(System.currentTimeMillis()));
        orderPaymentService.insert(orderPayment);
        order.setOrderPayment(orderPayment);

        return order;
    }
}
