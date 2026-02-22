package com.harmoni.pos.order.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Custom exception indicating that the client is not authorized to perform the requested operation.
 * <p>
 * Typically corresponds to an HTTP 401 Unauthorized or 403 Forbidden response,
 * depending on how it is mapped in the global exception handler.
 * </p>
 */
@Getter
@AllArgsConstructor
public class BusinessUnAuthorizedRequestException extends RuntimeException {

    /**
     * Message key representing a generic unauthorized access condition.
     */
    public static final String UN_AUTHORIZED = "exception.unAuthorized";

    /**
     * A message key or human-readable message describing the unauthorized error.
     */
    private final String message;

    /**
     * Optional arguments used for parameterized or localized error messages.
     */
    private final transient Object[] args;
}
