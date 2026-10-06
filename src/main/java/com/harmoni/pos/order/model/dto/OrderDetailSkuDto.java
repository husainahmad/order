package com.harmoni.pos.order.model.dto;

import com.harmoni.pos.order.model.OrderDetailSku;
import jakarta.validation.Valid;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * One SKU line within an order detail, carrying the SKU id, quantity, and chosen
 * customization groups with their options.
 */
@Data
public class OrderDetailSkuDto {
    private Integer skuId;
    private int quantity;

    /**
     * The customization groups chosen on this line, each with its selected options.
     * <p>
     * Only the IDs and quantities are accepted. The names and prices that end up on
     * the order are resolved by the menu service from the catalogue at the operator's tier,
     * so a client cannot name its own price for a customised item. An empty list is fine for
     * a line with nothing to choose; the menu service rejects the line if the product
     * requires a customization that has been left empty.
     * <p>
     * {@code skuName} is not accepted here either, for the same reason the customization
     * names are not: the line's name comes from the catalogue, not from the request.
     */
    @Valid
    private List<CustomizationGroupSelectionDto> customizations = new ArrayList<>();

    public OrderDetailSku toOrderDetailSku() {
        return new OrderDetailSku()
                .setSkuId(skuId)
                .setQuantity(quantity);
    }
}
