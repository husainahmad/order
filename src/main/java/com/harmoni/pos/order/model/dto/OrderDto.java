package com.harmoni.pos.order.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.harmoni.pos.order.model.Order;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


@Data
public class OrderDto {

    private Integer storeId;
    private String storeName;
    private Integer customerId;
    private String customerName;
    private Integer storeServiceTypesId;
    private BigDecimal discount;
    private String remark;

    @JsonProperty("orderDetails")
    public List<OrderDetailDto> orderDetailDtos;

    public Order toOrder() {
        return new Order()
                .setCustomerId(customerId)
                .setCustomerName(customerName)
                .setStoreId(storeId)
                .setStoreServiceTypesId(storeServiceTypesId);
    }
}