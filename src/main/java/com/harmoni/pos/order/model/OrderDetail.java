package com.harmoni.pos.order.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDetail {
    private Integer id;
    private Integer orderId;
    private Integer productId;
    private String productName;
    private Integer categoryId;
    private String categoryName;
    private List<OrderDetailSku> orderDetailSkus;
    private BigDecimal amount;
    private Date createdAt;
    private Date updatedAt;
}