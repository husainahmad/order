package com.harmoni.pos.order.business.service;

import com.harmoni.pos.order.model.Order;
import com.harmoni.pos.order.model.dto.OrderPaymentDto;

public interface OrderPaymentBusinessService {
    Order processPayment(OrderPaymentDto paymentDto, String token);
}
