package com.harmoni.pos.order.service;

import com.harmoni.pos.order.mapper.OrderPaymentMapper;
import com.harmoni.pos.order.model.OrderPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link OrderPaymentService} that provides
 * basic CRUD operations for {@link OrderPayment} entities.
 *
 * <p>This service delegates all operations to the {@link OrderPaymentMapper}
 * to interact with the database.</p>
 *
 * @author Ahmad
 * @since 1.0
 */
@RequiredArgsConstructor
@Service("orderPaymentService")
public class OrderPaymentServiceImpl implements OrderPaymentService {

    private final OrderPaymentMapper orderPaymentMapper;

    /**
     * Deletes an order payment by its primary key.
     *
     * @param id the ID of the order payment to delete
     * @return number of rows affected
     */
    @Override
    public int deleteByPrimaryKey(Integer id) {
        return orderPaymentMapper.deleteByPrimaryKey(id);
    }

    /**
     * Inserts a new order payment record into the database.
     *
     * @param orderPayment the order payment entity to insert
     * @return number of rows affected
     */
    @Override
    public int insert(OrderPayment orderPayment) {
        return orderPaymentMapper.insert(orderPayment);
    }

    /**
     * Retrieves an order payment by its primary key.
     *
     * @param id the ID of the order payment
     * @return the matching {@link OrderPayment}, or {@code null} if not found
     */
    @Override
    public OrderPayment selectByPrimaryKey(Integer id) {
        return orderPaymentMapper.selectByPrimaryKey(id);
    }

    /**
     * Updates an existing order payment record.
     *
     * @param orderPayment the updated order payment object
     * @return number of rows affected
     */
    @Override
    public int updateByPrimaryKey(OrderPayment orderPayment) {
        return orderPaymentMapper.updateByPrimaryKey(orderPayment);
    }
}
