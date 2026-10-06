package com.harmoni.pos.order.mapper;

import com.harmoni.pos.order.model.OrderItemDiscount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Mapper interface for OrderItemDiscount entity database operations.
 */
@Mapper
public interface OrderItemDiscountMapper {

    /**
     * Inserts a batch of new OrderItemDiscounts.
     *
     * @param rows the OrderItemDiscount objects
     * @return the number of inserted rows
     */
    int insertBatch(@Param("orderItemDiscounts") List<OrderItemDiscount> rows);
}
