package com.harmoni.pos.order.service;

import com.harmoni.pos.order.model.OrderDetail;

public interface OrderDetailService {
    int deleteByPrimaryKey(Integer id);
    int insert(OrderDetail row);
    OrderDetail selectByPrimaryKey(Integer id);
    int updateByPrimaryKey(OrderDetail row);
}