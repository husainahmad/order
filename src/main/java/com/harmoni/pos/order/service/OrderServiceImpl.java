package com.harmoni.pos.order.service;

import com.harmoni.pos.order.exception.BusinessNoContentRequestException;
import com.harmoni.pos.order.mapper.OrderMapper;
import com.harmoni.pos.order.model.Order;
import com.harmoni.pos.order.model.OrderDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service("orderService")
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderDetailService orderDetailService;

    @Override
    public String generateOrderNo(Order order) {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase(); // 6-char random string
        return "ORD" + order.getStore().getId() + "-" + datePart + "-" + randomPart;
    }

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return orderMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(Order order) {
        order.setCreatedAt(new Date(System.currentTimeMillis()));
        int rowCount = orderMapper.insert(order);
        log.debug("{} Order Created ", rowCount);
        for (OrderDetail orderDetail : order.getOrderDetails()) {
            orderDetail.setOrderId(order.getId());
            orderDetail.setCreatedAt(new Date(System.currentTimeMillis()));
        }
        orderDetailService.insertBulk(order.getOrderDetails());
        return rowCount;
    }

    @Override
    public Order selectByPrimaryKey(Integer id) {
        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null) {
            throw new BusinessNoContentRequestException(
                    BusinessNoContentRequestException.NO_CONTENT, null);
        }
        return order;
    }

    @Override
    public List<Order> selectByStoreId(Integer storeId, Timestamp from, Timestamp until) {
        return orderMapper.selectByStoreId(storeId, from, until);
    }

    @Override
    public int updateByPrimaryKey(Order order) {
        return orderMapper.updateByPrimaryKey(order);
    }
}
