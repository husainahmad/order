package com.harmoni.pos.order.service;

import com.harmoni.pos.order.model.Order;

import java.util.List;

public interface OrderService {
    int deleteByPrimaryKey(Integer id);
    int insert(Order row);
    Order selectByPrimaryKey(Integer id);
    List<Order> selectByStoreId(Integer storeId);
    int updateByPrimaryKey(Order row);

}