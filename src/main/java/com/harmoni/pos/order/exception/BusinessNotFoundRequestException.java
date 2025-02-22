package com.harmoni.pos.order.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BusinessNotFoundRequestException extends RuntimeException {
    public static final String NOT_FOUND_TIER = "exception.tier.id.badRequest.notFound";
    private final String message;
    private final transient Object[] args;
}
