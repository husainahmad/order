package com.harmoni.pos.order.service;

import com.harmoni.pos.order.model.OrderDetailSku;

import java.util.List;

public interface OrderDetailSkuService {
    int deleteByPrimaryKey(Integer id);
    int insert(OrderDetailSku row);
    void insertBulk(List<OrderDetailSku> orderDetailSkus);
    OrderDetailSku selectByPrimaryKey(Integer id);
    int updateByPrimaryKey(OrderDetailSku row);
}