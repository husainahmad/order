package com.harmoni.pos.order.mapper;

import com.harmoni.pos.order.model.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderDetailMapper {

    int deleteByPrimaryKey(Integer id);
    int insert(OrderDetail orderDetail);
    void insertBulk(List<OrderDetail> orderDetails);
    OrderDetail selectByPrimaryKey(Integer id);
    int updateByPrimaryKey(OrderDetail orderDetail);

}