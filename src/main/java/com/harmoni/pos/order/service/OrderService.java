package com.harmoni.pos.order.service;

import com.harmoni.pos.order.model.Order;

import java.sql.Timestamp;
import java.util.List;

public interface OrderService {
    String generateOrderNo(Order order);
    int deleteByPrimaryKey(Integer id);
    int insert(Order order);
    Order insertWithIdempotency(Order order, String idempotencyKey);
    Order selectByPrimaryKey(Integer id);
    Order findByIdempotencyKey(String idempotencyKey);
    List<Order> selectByStoreId(Integer storeId, Timestamp from, Timestamp until);
    int updateByPrimaryKey(Order order);
    int voidOrderById(Order order);
}