package com.harmoni.pos.order.business.service;

import com.harmoni.pos.order.model.Order;
import com.harmoni.pos.order.model.OrderDetail;
import com.harmoni.pos.order.model.OrderStatus;
import com.harmoni.pos.order.model.dto.OrderDetailDto;
import com.harmoni.pos.order.model.dto.OrderDto;
import com.harmoni.pos.order.service.OrderDetailService;
import com.harmoni.pos.order.service.OrderDetailSkuService;
import com.harmoni.pos.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service("orderBusinessService")
public class OrderBusinessService {

    private final OrderService orderService;
    private final OrderDetailService orderDetailService;
    private final OrderDetailSkuService orderDetailSkuService;

    public void test(OrderDto orderDto) {
        orderDto.setStoreId(1);
        orderDto.setStoreServiceTypesId(1);

        Order order = orderDto.toOrder();
        order.setStatus(OrderStatus.PENDING);

        int orderId = orderService.insert(orderDto.toOrder());
        for (OrderDetailDto orderDetailDto: orderDto.getOrderDetailDtos()) {
            OrderDetail orderDetail = orderDetailDto.toOrderDetail();
            orderDetail.setOrderId(orderId);

            int orderDetailId = orderDetailService.insert(orderDetail);
            orderDetailDto.setOrderId(orderId);
            orderDetailDto.setId(orderDetailId);
        }
    }

    public List<Order> gets() {
        return orderService.selectByStoreId(1);
    }
}
