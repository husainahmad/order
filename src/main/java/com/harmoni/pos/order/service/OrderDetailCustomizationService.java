package com.harmoni.pos.order.service;

import com.harmoni.pos.order.model.OrderDetailCustomization;

import java.util.List;

public interface OrderDetailCustomizationService {

    int record(List<OrderDetailCustomization> rows);

    List<OrderDetailCustomization> getByOrderDetailIds(List<Integer> orderDetailIds);
}
