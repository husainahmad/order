package com.harmoni.pos.order.service;

import com.harmoni.pos.order.mapper.OrderPaymentMapper;
import com.harmoni.pos.order.model.OrderPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service("orderPaymentService")
public class OrderPaymentServiceImpl implements OrderPaymentService {

    private final OrderPaymentMapper orderPaymentMapper;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return orderPaymentMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(OrderPayment orderPayment) {
        return orderPaymentMapper.insert(orderPayment);
    }

    @Override
    public OrderPayment selectByPrimaryKey(Integer id) {
        return orderPaymentMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKey(OrderPayment orderPayment) {
        return orderPaymentMapper.updateByPrimaryKey(orderPayment);
    }
}
