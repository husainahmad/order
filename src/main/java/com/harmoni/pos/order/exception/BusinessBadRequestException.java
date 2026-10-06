package com.harmoni.pos.order.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Custom exception class representing a business logic failure that results in a 400 Bad Request.
 * <p>
 * This exception is typically thrown when the request is semantically incorrect or violates business constraints,
 * such as duplicate entries or references to non-existent resources.
 * </p>
 */
@Getter
@AllArgsConstructor
public class BusinessBadRequestException extends RuntimeException {

    /**
     * A message code or human-readable message associated with this exception.
     */
    private final String message;

    /**
     * Optional arguments used to fill placeholders in the message (e.g., for localization).
     */
    private final transient Object[] args;
}
