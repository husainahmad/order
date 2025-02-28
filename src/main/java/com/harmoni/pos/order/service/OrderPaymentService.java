package com.harmoni.pos.order.service;

import com.harmoni.pos.order.model.OrderPayment;

public interface OrderPaymentService {
    int deleteByPrimaryKey(Integer id);
    int insert(OrderPayment orderPayment);
    OrderPayment selectByPrimaryKey(Integer id);
    int updateByPrimaryKey(OrderPayment orderPayment);

}