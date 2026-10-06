package com.harmoni.pos.order.model.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * One customization choice the menu service confirmed, named and priced.
 */
@Data
public class ValidatedCustomizationDto {

    /**
     * The group the option belongs to.
     */
    private Integer customizationId;

    /**
     * The group name as the catalogue currently has it.
     */
    private String customizationName;

    /**
     * The chosen option.
     */
    private Integer customizationOptionId;

    /**
     * The option name as the catalogue currently has it.
     */
    private String optionName;

    /**
     * The price of this option for one unit, at the store's tier.
     */
    private BigDecimal price;

    /**
     * The confirmed number of selections of this option on the line.
     */
    private Integer quantity;
}
