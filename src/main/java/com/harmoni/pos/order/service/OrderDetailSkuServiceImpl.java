package com.harmoni.pos.order.service;

import com.harmoni.pos.order.mapper.OrderDetailSkuMapper;
import com.harmoni.pos.order.model.OrderDetailSku;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service("orderDetailSkuService")
public class OrderDetailSkuServiceImpl implements OrderDetailSkuService {

    private final OrderDetailSkuMapper orderDetailSkuMapper;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return orderDetailSkuMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(OrderDetailSku row) {
        return orderDetailSkuMapper.insert(row);
    }

    @Override
    public OrderDetailSku selectByPrimaryKey(Integer id) {
        return orderDetailSkuMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKey(OrderDetailSku row) {
        return orderDetailSkuMapper.updateByPrimaryKey(row);
    }

}
