package com.harmoni.pos.order.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)

public class SettlementReport {
    private LocalDate reportDate;
    private int totalOrders;
    private BigDecimal totalSales;
    private BigDecimal totalDiscounts;
    private BigDecimal totalTax;
    private BigDecimal totalNetSales;
    private Map<String, BigDecimal> paymentBreakdown; // e.g. {"CASH": 500, "CARD": 350}
    private BigDecimal totalRefunds;
}

