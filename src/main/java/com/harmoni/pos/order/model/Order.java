package com.harmoni.pos.order.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Order {
    private Integer id;
    private String orderNo;
    private Double subTotal;
    private Double discountTotal;
    private Double grandTotal;
    private Double taxTotal;
    private Integer storeId;
    private Integer customerId;
    private String customerName;
    private Integer storeServiceTypesId;
    private OrderStatus status;
    private Date createdAt;
    private Date updatedAt;
}