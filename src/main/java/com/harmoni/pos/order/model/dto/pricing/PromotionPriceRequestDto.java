package com.harmoni.pos.order.model.dto.pricing;

import lombok.Data;

import java.util.List;

/**
 * A request to the menu service to price a basket of sale lines.
 *
 * @author husainahmad
 */
@Data
public class PromotionPriceRequestDto {

    private List<CartLineDto> lines;

    private PromotionContextDto context;
}
