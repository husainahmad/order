package com.harmoni.pos.order.mapper;

import com.harmoni.pos.order.model.OrderCustomer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Mapper interface for OrderCustomer entity database operations.
 */
@Mapper
public interface OrderCustomerMapper {

    /**
     * Deletes the customer snapshot for an order.
     *
     * @param orderId the order ID
     * @return the number of deleted rows
     */
    int deleteByOrderId(Integer orderId);

    /**
     * Inserts a new OrderCustomer snapshot.
     *
     * @param row the OrderCustomer object
     * @return the number of inserted rows
     */
    int insert(OrderCustomer row);

    /**
     * Selects the customer snapshot for an order.
     *
     * @param orderId the order ID
     * @return the OrderCustomer object, or {@code null} if the order has none
     */
    OrderCustomer selectByOrderId(Integer orderId);

    /**
     * Selects a customer's orders within an optional date range, newest first.
     *
     * @param customerId the customer in the customer service
     * @param from       optional inclusive lower bound on the order timestamp
     * @param to         optional inclusive upper bound on the order timestamp
     * @return list of OrderCustomer objects, most recent first
     */
    List<OrderCustomer> selectHistory(@Param("customerId") Long customerId,
                                      @Param("from") Date from,
                                      @Param("to") Date to);
}
