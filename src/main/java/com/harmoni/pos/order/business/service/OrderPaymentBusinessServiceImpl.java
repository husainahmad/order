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

@RequiredArgsConstructor
@Service("orderPaymentBusinessService")
public class OrderPaymentBusinessServiceImpl implements OrderPaymentBusinessService {

    private final OrderService orderService;
    private final OrderPaymentService orderPaymentService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

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
