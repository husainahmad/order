package com.harmoni.pos.order.model.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * One line of {@link ValidateOrderItemsRequestDto}: what was ordered, and nothing that the
 * menu service would have to take on trust.
 * <p>
 * IDs, a quantity and the chosen customization groups with their options. No names, no prices.
 * A line that could name its own product or its own price would make the menu service's answer
 * decorative, so those fields are not accepted here and never were on the customization path either.
 *
 * @author husainahmad
 */
@Data
public class ValidateOrderItemRequestDto {

    /**
     * The product being ordered.
     */
    private Integer productId;

    /**
     * The SKU being ordered, which must belong to that product.
     */
    private Integer skuId;

    /**
     * How many units are being ordered.
     */
    private Integer quantity;

    /**
     * The chosen customization groups and their options.
     */
    private List<CustomizationGroupSelectionDto> customizations = new ArrayList<>();
}
