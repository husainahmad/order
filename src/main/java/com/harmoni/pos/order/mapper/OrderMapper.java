package com.harmoni.pos.order.mapper;

import com.harmoni.pos.order.model.Order;
import org.apache.ibatis.annotations.Mapper;

import java.sql.Timestamp;
import java.util.List;

@Mapper
public interface OrderMapper {

    int deleteByPrimaryKey(Integer id);
    int insert(Order order);
    Order selectByPrimaryKey(Integer id);
    List<Order> selectByStoreId(Integer storeId, Timestamp from, Timestamp until);
    int updateByPrimaryKey(Order order);

}