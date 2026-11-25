package com.harmoni.pos.order.http.handler;

import com.harmoni.pos.order.exception.ScopeAccessDeniedException;
import com.harmoni.pos.order.http.response.RestAPIResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@Slf4j
public class ScopeAccessDeniedExceptionHandler {

    @ExceptionHandler(ScopeAccessDeniedException.class)
    public ResponseEntity<RestAPIResponse> scopeAccessDenied(ScopeAccessDeniedException exception) {
        log.warn("Scope access denied: {}", exception.getMessage());
        RestAPIResponse response = RestAPIResponse.builder()
                .httpStatus(HttpStatus.FORBIDDEN.value())
                .timeStamp(System.currentTimeMillis())
                .data(null)
                .error(exception.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
}
