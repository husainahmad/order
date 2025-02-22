package com.harmoni.pos.order.mapper;

import com.harmoni.pos.order.model.Order;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderMapper {

    int deleteByPrimaryKey(Integer id);
    int insert(Order row);
    Order selectByPrimaryKey(Integer id);
    List<Order> selectByStoreId(Integer storeId);
    int updateByPrimaryKey(Order row);

}