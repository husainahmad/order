package com.harmoni.pos.order.service;

import com.harmoni.pos.order.model.OrderDetailCustomizationOption;

import java.util.List;

public interface OrderDetailCustomizationOptionService {

    int record(List<OrderDetailCustomizationOption> rows);

    /**
     * Reads the options chosen under the given customization groups.
     *
     * @param customizationIds the {@code order_detail_customizations} row ids
     * @return the options, empty when none of the groups has any
     */
    List<OrderDetailCustomizationOption> getByCustomizationIds(List<Long> customizationIds);
}
