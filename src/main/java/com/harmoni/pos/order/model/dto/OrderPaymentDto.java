package com.harmoni.pos.order.model.dto;

import com.harmoni.pos.order.model.OrderPayment;
import lombok.Data;

@Data
public class OrderPaymentDto {

    private Integer orderId;
    private Integer paymentId;

    public OrderPayment toOrderPayment() {
        return new OrderPayment()
                .setOrderId(orderId)
                .setPaymentId(paymentId);
    }
}