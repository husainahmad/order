package com.harmoni.pos.order.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * One customization option chosen within a group, and how many of it.
 */
@Data
public class CustomizationOptionSelectionDto {

    /**
     * The chosen option.
     */
    @NotNull
    private Integer customizationOptionId;

    /**
     * How many units of this option the line carries.
     * <p>
     * Defaults to one so a client that only cares about the choice itself, which is most of
     * them, does not have to spell it out. The menu service rejects a count that breaks the
     * customization's own limit.
     */
    @NotNull
    @Min(1)
    private Integer quantity = 1;
}
