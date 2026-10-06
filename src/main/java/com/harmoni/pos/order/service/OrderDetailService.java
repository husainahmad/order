package com.harmoni.pos.order.service;

import com.harmoni.pos.order.model.OrderDetail;
import com.harmoni.pos.order.model.dto.OrderDetailDto;

import java.util.List;

public interface OrderDetailService {
    int deleteByPrimaryKey(Integer id);
    int insert(OrderDetail orderDetail);
    void insertBulk(List<OrderDetail> orderDetails);
    OrderDetail selectByPrimaryKey(Integer id);

    /**
     * Confirms the basket with the menu service and turns it into order lines.
     *
     * @param orderDetailDtos the lines the client asked for
     * @param storeId         the store the sale is being made at, which decides the price tier
     * @param username        the operator the call is made on behalf of
     * @return the populated order lines, carrying the catalogue's names and prices
     */
    List<OrderDetail> populateOrderDetail(List<OrderDetailDto> orderDetailDtos, Integer storeId, String username);
    int updateByPrimaryKey(OrderDetail orderDetail);
}