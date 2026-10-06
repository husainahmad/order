package com.harmoni.pos.order.model.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * What the order service asks the menu service to confirm: the whole basket, for one store,
 * in a single call.
 * <p>
 * Sending the basket whole is what keeps this to one request per sale. Priced line by line,
 * a basket of six items with options on each would have to be sent seven times, and every
 * one of those answers is a slightly different snapshot of the catalogue.
 * <p>
 * The store is stated here rather than left to the menu service to work out from the
 * operator, so the prices that come back are provably the ones for the store this order is
 * being sold at, even if an operator is somehow working in more than one.
 *
 * @author husainahmad
 */
@Data
public class ValidateOrderItemsRequestDto {

    /**
     * The store the sale is being made at, which decides availability and the price tier.
     */
    private Integer storeId;

    /**
     * The lines being ordered.
     */
    private List<ValidateOrderItemRequestDto> items = new ArrayList<>();
}