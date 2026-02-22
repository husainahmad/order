package com.harmoni.pos.order.http.handler;

import com.harmoni.pos.order.exception.BusinessNotFoundRequestException;
import com.harmoni.pos.order.http.response.RestAPIResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;

/**
 * Handles exceptions of type {@link BusinessNotFoundRequestException}
 * and returns a standardized 404 Not Found response.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@Slf4j
public class NotFoundRequestExceptionHandler {

    private final MessageSource messageSource;

    /**
     * Constructor for injecting {@link MessageSource} used for localized messages.
     *
     * @param messageSource the message source
     */
    @Autowired
    public NotFoundRequestExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Handles {@link BusinessNotFoundRequestException} and returns
     * a localized HTTP 404 response.
     *
     * @param e      the exception containing the message code and arguments
     * @param locale the request locale for message translation
     * @return a 404 NOT_FOUND response with the error message
     */
    @ExceptionHandler(BusinessNotFoundRequestException.class)
    public ResponseEntity<RestAPIResponse> notFoundExceptionHandler(
            BusinessNotFoundRequestException e, Locale locale) {

        String messageName = e.getMessage();
        Object[] args = e.getArgs();

        String message = messageSource.getMessage(messageName, args, locale);

        log.warn("NotFoundRequest: {}", message);

        RestAPIResponse restAPIResponse = RestAPIResponse.builder()
                .httpStatus(HttpStatus.NOT_FOUND.value()) // Corrected from BAD_REQUEST
                .timeStamp(System.currentTimeMillis())
                .data(null)
                .error(message)
                .build();

        return new ResponseEntity<>(restAPIResponse, HttpStatus.NOT_FOUND);
    }
}
