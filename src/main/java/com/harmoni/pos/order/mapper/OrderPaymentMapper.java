package com.harmoni.pos.order.mapper;

import com.harmoni.pos.order.model.OrderPayment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderPaymentMapper {

    int deleteByPrimaryKey(Integer id);
    int insert(OrderPayment row);
    OrderPayment selectByPrimaryKey(Integer id);
    int updateByPrimaryKey(OrderPayment row);

}