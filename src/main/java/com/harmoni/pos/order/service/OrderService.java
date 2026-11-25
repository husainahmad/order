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
    Order selectByIdWithPayment(Integer id);
    Order findByIdempotencyKey(String idempotencyKey);
    List<Order> selectByStoreId(Integer storeId, Timestamp from, Timestamp until);

    /**
     * One page of a store's orders, newest first.
     *
     * @param storeId  the store whose orders are wanted
     * @param from     start of the range, inclusive
     * @param until    end of the range, exclusive
     * @param beforeId return only orders older than this id; {@code null} for the first page
     * @param limit    the most orders to return
     * @return the page, each order carrying its lines, choices and payment
     */
    List<Order> selectByStoreIdPaged(Integer storeId, Timestamp from, Timestamp until,
                                     Integer beforeId, int limit);
    int updateByPrimaryKey(Order order);
    int voidOrderById(Order order);
}