package com.harmoni.pos.order.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TopProductReport {
    private Integer productId;
    private String productName;
    private BigDecimal totalSales;
    private Integer quantitySold;
}


