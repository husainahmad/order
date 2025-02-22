package com.harmoni.pos.order.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BusinessUnAuthorizedRequestException extends RuntimeException {
    private final String message;
    private final transient Object[] args;
}
