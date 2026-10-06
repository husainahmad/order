package com.harmoni.pos.order.model.dto.pricing;

import lombok.Data;

import java.math.BigDecimal;

/**
 * One sale line as sent to the menu service when asking for a price.
 * <p>
 * Mirrors the menu service's cart line DTO. The identifiers are {@link Integer} here
 * because that is what the order model uses everywhere, and are widened to {@code Long}
 * by the menu service; JSON does not distinguish the two.
 * <p>
 * The line carries no order item ID. The basket is priced before the order is
 * persisted, so no ID exists yet, and {@code lineIndex} is what ties a priced line back
 * to the row that was sent.
 *
 * @author husainahmad
 */
@Data
public class CartLineDto {

    private Integer lineIndex;

    private Integer productId;

    private Integer skuId;

    private Integer categoryId;

    /**
     * The pre-discount price charged for one unit, resolved from the menu catalogue
     * rather than taken from the client.
     */
    private BigDecimal unitPrice;

    private Integer quantity;
}
