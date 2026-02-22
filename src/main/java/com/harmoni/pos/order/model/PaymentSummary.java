package com.harmoni.pos.order.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)

public class PaymentSummary {
    private String paymentType;
    private BigDecimal totalAmount;
}
