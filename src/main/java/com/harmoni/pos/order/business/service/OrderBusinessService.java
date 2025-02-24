package com.harmoni.pos.order.business.service;

import com.harmoni.pos.order.model.Order;
import com.harmoni.pos.order.model.dto.OrderDto;
import com.harmoni.pos.order.model.dto.StoreDto;

public interface OrderBusinessService {
    StoreDto getStoreDetail(Integer id, String authHeader);
    Order calculateSubtotal(Order order);
    Order calculateServiceCharge(Order order);
    Order calculateDiscount(Order order, OrderDto orderDto);
    Order calculateTax(Order order, OrderDto orderDto);
    Order calculateGrandTotal(Order order);
    Order calculate(OrderDto orderDto, String authHeader);
}
