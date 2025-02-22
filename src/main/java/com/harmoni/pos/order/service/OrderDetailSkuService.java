package com.harmoni.pos.order.service;

import com.harmoni.pos.order.model.OrderDetailSku;

public interface OrderDetailSkuService {
    int deleteByPrimaryKey(Integer id);
    int insert(OrderDetailSku row);
    OrderDetailSku selectByPrimaryKey(Integer id);
    int updateByPrimaryKey(OrderDetailSku row);
}