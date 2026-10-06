package com.harmoni.pos.order.service;

import com.harmoni.pos.order.mapper.OrderCustomerMapper;
import com.harmoni.pos.order.model.Order;
import com.harmoni.pos.order.model.OrderCustomer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Implementation of {@link OrderCustomerService} that snapshots customer identity
 * against confirmed orders.
 * <p>
 * All database operations are delegated to {@link OrderCustomerMapper}.
 * </p>
 *
 * @author husainahmad
 */
@RequiredArgsConstructor
@Service("orderCustomerService")
public class OrderCustomerServiceImpl implements OrderCustomerService {

    /**
     * The value {@link Order#getCustomerId()} carries for a sale with no customer.
     * The orders table already uses it, so it is read here rather than assumed: a new
     * convention would silently start writing a snapshot for every walk-in.
     */
    private static final int WALK_IN_CUSTOMER_ID = -1;

    private final OrderCustomerMapper orderCustomerMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(transactionManager = "transactionSQLTransactionManager")
    public OrderCustomer record(Order order) {
        if (isWalkIn(order)) {
            return null;
        }
        OrderCustomer snapshot = new OrderCustomer()
                .setOrderId(order.getId())
                .setCustomerId(order.getCustomerId().longValue())
                .setCustomerName(order.getCustomerName())
                .setCustomerPhone(order.getCustomerPhone())
                .setCreatedAt(new Date());
        orderCustomerMapper.insert(snapshot);
        return snapshot;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderCustomer getByOrderId(Integer orderId) {
        return orderCustomerMapper.selectByOrderId(orderId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<OrderCustomer> getHistory(Long customerId, Date from, Date to) {
        return orderCustomerMapper.selectHistory(customerId, from, to);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(transactionManager = "transactionSQLTransactionManager")
    public int deleteByOrderId(Integer orderId) {
        return orderCustomerMapper.deleteByOrderId(orderId);
    }

    /**
     * A sale with no customer is either an absent ID or the {@code -1} the orders table
     * uses. Both are refused here so neither writes a row of nulls.
     *
     * @param order the order about to be recorded
     * @return {@code true} when the order names no customer
     */
    private static boolean isWalkIn(Order order) {
        return order == null || order.getCustomerId() == null
                || order.getCustomerId() == WALK_IN_CUSTOMER_ID;
    }
}
