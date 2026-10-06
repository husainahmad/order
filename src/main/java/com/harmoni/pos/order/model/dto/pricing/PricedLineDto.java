package com.harmoni.pos.order.model.dto.pricing;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * What the menu service says one line costs once promotions are applied.
 * <p>
 * The gross amount is retained alongside the net so the order keeps a full before and
 * after picture of the line, and so the order's subtotal can stay the gross value of the
 * basket.
 *
 * @author husainahmad
 */
@Data
public class PricedLineDto {

    /**
     * Echoes the {@code lineIndex} that was sent, used to match this result back to the
     * order line it belongs to.
     */
    private Integer lineIndex;

    private BigDecimal grossAmount;

    private BigDecimal discountAmount;

    private BigDecimal netAmount;

    private boolean discounted;

    private List<AppliedDiscountDto> discounts;
}
