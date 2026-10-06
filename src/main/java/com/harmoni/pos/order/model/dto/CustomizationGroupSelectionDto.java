package com.harmoni.pos.order.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * One customization group the customer chose on an order line, with its selected options.
 * <p>
 * This groups options by their parent customization, so the menu service can validate
 * each group's rules (required, single-select, multi-select, max quantity) independently.
 */
@Data
public class CustomizationGroupSelectionDto {

    /**
     * The customization group being configured.
     */
    @NotNull
    private Integer customizationId;

    /**
     * The chosen options within this group.
     */
    @NotEmpty
    @Valid
    private List<CustomizationOptionSelectionDto> options = new ArrayList<>();
}
