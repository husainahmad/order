package com.harmoni.pos.order.http.handler;

import com.harmoni.pos.order.exception.BusinessBadRequestException;
import com.harmoni.pos.order.http.response.RestAPIResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;

/**
 * Handles exceptions that should return HTTP 400 (Bad Request) responses.
 * Applies to all controllers globally due to {@link RestControllerAdvice}.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@AllArgsConstructor
@Slf4j
public class BadRequestExceptionHandler {

    /** Injected Spring message source for internationalized error messages. */
    private final MessageSource messageSource;

    /**
     * Handles custom {@link BusinessBadRequestException} by building a
     * {@link RestAPIResponse} with a localized error message.
     *
     * @param e      the thrown exception
     * @param locale the locale from the HTTP request
     * @return HTTP 400 response with error details
     */
    @ExceptionHandler(BusinessBadRequestException.class)
    public ResponseEntity<RestAPIResponse> badRequestExceptionHandler(BusinessBadRequestException e, Locale locale) {
        String messageName = e.getMessage();
        Object[] args = e.getArgs();
        String message = messageSource.getMessage(messageName, args, locale);

        RestAPIResponse restAPIResponse = RestAPIResponse.builder()
                .httpStatus(HttpStatus.BAD_REQUEST.value())
                .timeStamp(System.currentTimeMillis())
                .error(message)
                .data(null)
                .build();

        logAsWarning(message);

        return new ResponseEntity<>(restAPIResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Logs the warning-level message for debugging and tracing purposes.
     *
     * @param message the message to log
     */
    private static void logAsWarning(String message) {
        log.warn("BadRequest: {}", message);
    }

    /**
     * Handles missing required parameters in HTTP requests by returning a 400 response.
     *
     * @param e the exception thrown when a required parameter is missing
     * @return HTTP 400 response with error details
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<RestAPIResponse> missingRequiredParam(MissingServletRequestParameterException e) {
        String messageName = e.getMessage();

        RestAPIResponse restAPIResponse = RestAPIResponse.builder()
                .httpStatus(HttpStatus.BAD_REQUEST.value())
                .timeStamp(System.currentTimeMillis())
                .error(messageName)
                .data(null)
                .build();

        logAsWarning(messageName);

        return new ResponseEntity<>(restAPIResponse, HttpStatus.BAD_REQUEST);
    }
}
