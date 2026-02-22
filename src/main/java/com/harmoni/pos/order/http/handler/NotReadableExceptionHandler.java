package com.harmoni.pos.order.http.handler;

import com.harmoni.pos.order.http.response.RestAPIResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;

/**
 * Global exception handler for {@link HttpMessageNotReadableException},
 * typically thrown when the request body is malformed or invalid.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@Slf4j
public class NotReadableExceptionHandler {

    /**
     * Handles exceptions where the HTTP message is not readable,
     * usually due to malformed JSON or incorrect content types.
     *
     * @param e      the exception
     * @param locale the current locale (not used but available for future enhancements)
     * @return a BAD_REQUEST (400) response with a generic error message
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RestAPIResponse> notReadableExceptionHandler(
            HttpMessageNotReadableException e, Locale locale) {

        log.error("Malformed JSON request", e);

        RestAPIResponse restAPIResponse = RestAPIResponse.builder()
                .httpStatus(HttpStatus.BAD_REQUEST.value())
                .timeStamp(System.currentTimeMillis())
                .error("Malformed JSON request")  // More user-friendly and searchable message
                .data(null)
                .build();

        return new ResponseEntity<>(restAPIResponse, HttpStatus.BAD_REQUEST);
    }

}
