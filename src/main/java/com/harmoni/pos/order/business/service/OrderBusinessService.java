package com.harmoni.pos.order.business.service;

import com.harmoni.pos.order.model.Order;
import com.harmoni.pos.order.model.dto.OrderDto;

import java.util.List;

public interface OrderBusinessService {
    void calculateSubtotal(Order order);
    void calculateServiceCharge(Order order);
    void calculateDiscount(Order order, OrderDto orderDto);
    void calculateTax(Order order, OrderDto orderDto);
    void calculateGrandTotal(Order order);
    Order calculate(OrderDto orderDto, String token);
    Order confirm(OrderDto orderDto, String token);
    Order print(OrderDto orderDto, String token);
    List<Order> getDailyListByStore(String token);
}
