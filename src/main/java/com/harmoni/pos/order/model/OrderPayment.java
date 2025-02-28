package com.harmoni.pos.order.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)

public class OrderPayment {
    private Integer id;
    private Integer orderId;
    private Integer paymentId;
    private BigDecimal amount;
    private Date createdAt;
    private Date updatedAt;
}