package com.harmoni.pos.order.http.handler;

import com.harmoni.pos.order.http.response.RestAPIResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler that catches all uncaught exceptions
 * and returns an HTTP 500 Internal Server Error response.
 */
@RestControllerAdvice
@Slf4j
public class InternalServerExceptionHandler {

    /**
     * Handles any unhandled exceptions thrown in the application.
     * Logs the exception and returns a generic error response.
     *
     * @param exception the exception thrown
     * @return HTTP 500 response with error details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestAPIResponse> globalExceptionHandler(Exception exception) {
        log.error("FightBegun: {}", exception.getMessage(), exception);

        RestAPIResponse restAPIResponse = RestAPIResponse.builder()
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timeStamp(System.currentTimeMillis())
                .error(HttpStatus.INTERNAL_SERVER_ERROR)
                .data(null)
                .build();

        return new ResponseEntity<>(restAPIResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
