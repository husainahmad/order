package com.harmoni.pos.order.service;

import com.harmoni.pos.order.mapper.OrderDetailSkuMapper;
import com.harmoni.pos.order.model.OrderDetailSku;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of {@link OrderDetailSkuService} that handles
 * CRUD operations for {@link OrderDetailSku} entities.
 * <p>
 * All database operations are delegated to {@link OrderDetailSkuMapper}.
 * </p>
 *
 * @author Ahmad
 * @since 1.0
 */
@RequiredArgsConstructor
@Service("orderDetailSkuService")
public class OrderDetailSkuServiceImpl implements OrderDetailSkuService {

    private final OrderDetailSkuMapper orderDetailSkuMapper;

    /**
     * Deletes an OrderDetailSku by its primary key.
     *
     * @param id the ID of the OrderDetailSku to delete
     * @return the number of rows affected
     */
    @Override
    public int deleteByPrimaryKey(Integer id) {
        return orderDetailSkuMapper.deleteByPrimaryKey(id);
    }

    /**
     * Inserts a new OrderDetailSku into the database.
     *
     * @param row the {@link OrderDetailSku} to insert
     * @return the number of rows affected
     */
    @Override
    public int insert(OrderDetailSku row) {
        return orderDetailSkuMapper.insert(row);
    }

    /**
     * Inserts a list of OrderDetailSku entities in bulk.
     *
     * @param orderDetailSkus the list of {@link OrderDetailSku} to insert
     */
    @Override
    public void insertBulk(List<OrderDetailSku> orderDetailSkus) {
        orderDetailSkuMapper.insertBulk(orderDetailSkus);
    }

    /**
     * Selects an OrderDetailSku by its primary key.
     *
     * @param id the ID of the OrderDetailSku to retrieve
     * @return the matching {@link OrderDetailSku}, or {@code null} if not found
     */
    @Override
    public OrderDetailSku selectByPrimaryKey(Integer id) {
        return orderDetailSkuMapper.selectByPrimaryKey(id);
    }

    /**
     * Updates an existing OrderDetailSku record.
     *
     * @param row the {@link OrderDetailSku} entity with updated values
     * @return the number of rows affected
     */
    @Override
    public int updateByPrimaryKey(OrderDetailSku row) {
        return orderDetailSkuMapper.updateByPrimaryKey(row);
    }
}
