package com.harmoni.pos.order.mapper;

import com.harmoni.pos.order.model.OrderTransactionOutbox;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Outbox rows holding order-created events until the customer service accepts them.
 */
@Mapper
public interface OrderTransactionOutboxMapper {

    int insert(OrderTransactionOutbox row);

    List<OrderTransactionOutbox> selectPending(@Param("limit") int limit);

    int markSent(@Param("id") Long id);

    int markFailed(@Param("id") Long id, @Param("error") String error);
}
