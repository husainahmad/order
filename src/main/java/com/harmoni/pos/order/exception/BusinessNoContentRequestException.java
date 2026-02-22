package com.harmoni.pos.order.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Custom exception indicating that the business operation completed successfully,
 * but no content is available to return (typically corresponds to HTTP 204 No Content).
 * <p>
 * This can be used to signal intentionally empty results where data is not present
 * but the request itself was valid.
 * </p>
 */
@Getter
@AllArgsConstructor
public class BusinessNoContentRequestException extends RuntimeException {

    /**
     * Message key representing a generic no-content condition.
     */
    public static final String NO_CONTENT = "exception.noContent";

    /**
     * A message code or human-readable message describing the reason for the no-content result.
     */
    private final String message;

    /**
     * Optional arguments used for parameterized or localized error messages.
     */
    private final transient Object[] args;
}
