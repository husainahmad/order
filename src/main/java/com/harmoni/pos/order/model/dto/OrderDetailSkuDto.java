package com.harmoni.pos.order.model.dto;

import com.harmoni.pos.order.model.OrderDetailSku;
import lombok.Data;

@Data
public class OrderDetailSkuDto {
    private Integer skuId;
    private String skuName;
    private int quantity;

    public OrderDetailSku toOrderDetailSku() {
        return new OrderDetailSku()
                .setSkuId(skuId)
                .setSkuName(skuName)
                .setQuantity(quantity);
    }
}
