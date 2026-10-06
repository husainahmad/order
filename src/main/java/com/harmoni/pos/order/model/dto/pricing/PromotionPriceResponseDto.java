package com.harmoni.pos.order.model.dto.pricing;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * The menu service's answer for a whole basket.
 *
 * @author husainahmad
 */
@Data
public class PromotionPriceResponseDto {

    private List<PricedLineDto> lines;

    private BigDecimal totalDiscount;

    private BigDecimal totalNet;
}
