package com.harmoni.pos.order.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Custom exception thrown when a requested resource is not found in the system,
 * typically corresponding to an HTTP 404 Not Found response.
 * <p>
 * This exception is used to indicate that a lookup (such as for a Tier or Product ID)
 * failed because the specified item does not exist.
 * </p>
 */
@Getter
@AllArgsConstructor
public class BusinessNotFoundRequestException extends RuntimeException {

    /**
     * Message key for when a tier with the specified ID is not found.
     */
    public static final String NOT_FOUND_TIER = "exception.tier.id.badRequest.notFound";

    /**
     * A message key or human-readable message describing the not-found condition.
     */
    private final String message;

    /**
     * Optional arguments for parameterized or localized error messages.
     */
    private final transient Object[] args;
}
