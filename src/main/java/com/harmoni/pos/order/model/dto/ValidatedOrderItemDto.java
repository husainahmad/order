package com.harmoni.pos.order.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * One line the menu service confirmed, carrying the names and prices it read from the
 * catalogue at the store's tier.
 */
@Data
public class ValidatedOrderItemDto {

    private Integer productId;

    private String productName;

    private Integer categoryId;

    private String categoryName;

    private Integer skuId;

    private String skuName;

    private BigDecimal skuPrice;

    private Integer quantity;

    private List<ValidatedCustomizationDto> customizations = new ArrayList<>();
}
