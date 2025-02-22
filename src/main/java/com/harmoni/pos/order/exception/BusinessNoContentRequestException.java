package com.harmoni.pos.order.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BusinessNoContentRequestException extends RuntimeException {
    public static final String NO_CONTENT = "exception.noContent";
    private final String message;
    private final transient Object[] args;
}
