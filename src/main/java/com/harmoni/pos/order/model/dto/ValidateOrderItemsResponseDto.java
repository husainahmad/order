package com.harmoni.pos.order.model.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * The menu service's answer for a whole basket: every line confirmed, with the official
 * data behind it.
 * <p>
 * Reaching this object at all means the basket passed validation. The menu service rejects
 * a bad basket outright, so there is no mixed answer to triage here where the flag would be
 * {@code false} next to a list of the lines it liked; the flag only says a caller should
 * check it before trusting an empty list, because an empty basket is also empty and valid.
 *
 * @author husainahmad
 */
@Data
public class ValidateOrderItemsResponseDto {

    /**
     * Whether the menu service confirmed the basket.
     */
    private boolean valid;

    /**
     * One confirmed entry per requested line, in the order they were sent.
     */
    private List<ValidatedOrderItemDto> items = new ArrayList<>();
}