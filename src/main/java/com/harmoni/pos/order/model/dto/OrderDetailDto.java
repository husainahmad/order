package com.harmoni.pos.order.model.dto;

import com.harmoni.pos.order.model.OrderDetail;
import lombok.Data;

import java.util.List;

@Data
public class OrderDetailDto {
    private Integer id;
    private Integer orderId;
    private Integer productId;
    private String remark;
    private List<OrderDetailSkuDto> orderDetailSkus;

    public OrderDetail toOrderDetail() {
        return new OrderDetail().setOrderId(orderId)
                .setProductId(productId);
    }
}