package com.harmoni.pos.order.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderVolumeReport {
    private Integer totalOrders;
    private Integer peakTimeOrders;
    private Integer nonPeakTimeOrders;
}
