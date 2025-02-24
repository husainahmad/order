package com.harmoni.pos.order.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDetailSku {
    private Integer id;
    private Integer orderDetailId;
    private Integer skuId;
    private String skuName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal amount;
    private Date createdAt;
}