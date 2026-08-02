package com.harmoni.pos.order.service;

import com.harmoni.pos.order.model.OrderDetail;
import com.harmoni.pos.order.model.dto.OrderDetailDto;
import com.harmoni.pos.order.model.dto.ProductDto;
import com.harmoni.pos.order.model.dto.SkuDto;

import java.util.List;

public interface OrderDetailService {
    int deleteByPrimaryKey(Integer id);
    int insert(OrderDetail orderDetail);
    void insertBulk(List<OrderDetail> orderDetails);
    OrderDetail selectByPrimaryKey(Integer id);
    List<OrderDetail> populateOrderDetail(List<OrderDetailDto> orderDetailDtos, String username);
    List<SkuDto> getDetailSkuWithPrice(List<OrderDetailDto> orderDetailDtos, String username);
    List<ProductDto> getProductWithName(List<OrderDetailDto> orderDetailDtos, String username);
    int updateByPrimaryKey(OrderDetail orderDetail);
}