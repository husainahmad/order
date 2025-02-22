package com.harmoni.pos.order.mapper;

import com.harmoni.pos.order.model.OrderDetailSku;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderDetailSkuMapper {

    int deleteByPrimaryKey(Integer id);
    int insert(OrderDetailSku row);
    OrderDetailSku selectByPrimaryKey(Integer id);
    int updateByPrimaryKey(OrderDetailSku row);
}