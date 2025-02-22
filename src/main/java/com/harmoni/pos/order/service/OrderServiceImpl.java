package com.harmoni.pos.order.service;

import com.harmoni.pos.order.mapper.OrderMapper;
import com.harmoni.pos.order.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service("orderService")
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return orderMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(Order row) {
        return orderMapper.insert(row);
    }

    @Override
    public Order selectByPrimaryKey(Integer id) {
        return orderMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<Order> selectByStoreId(Integer storeId) {
        return orderMapper.selectByStoreId(storeId);
    }

    @Override
    public int updateByPrimaryKey(Order row) {
        return orderMapper.updateByPrimaryKey(row);
    }
}
