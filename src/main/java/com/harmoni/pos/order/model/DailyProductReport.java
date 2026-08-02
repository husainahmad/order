package com.harmoni.pos.order.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DailyProductReport {
    private Integer productId;
    private String productName;
    private Integer quantity;
    private BigDecimal netSales;
    private BigDecimal discount;
}
