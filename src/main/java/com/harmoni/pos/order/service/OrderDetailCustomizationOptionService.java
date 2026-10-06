package com.harmoni.pos.order.service;

import com.harmoni.pos.order.model.OrderDetailCustomizationOption;

import java.util.List;

public interface OrderDetailCustomizationOptionService {

    int record(List<OrderDetailCustomizationOption> rows);
}
