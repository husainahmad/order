package com.harmoni.pos.order.mapper;

import com.harmoni.pos.order.model.OrderDetailSku;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderDetailSkuMapper {

    int deleteByPrimaryKey(Integer id);
    int insert(OrderDetailSku orderDetailSku);
    void insertBulk(List<OrderDetailSku> orderDetailSkus);
    OrderDetailSku selectByPrimaryKey(Integer id);
    int updateByPrimaryKey(OrderDetailSku orderDetailSku);
}