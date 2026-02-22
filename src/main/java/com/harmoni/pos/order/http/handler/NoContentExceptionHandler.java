package com.harmoni.pos.order.http.handler;

import com.harmoni.pos.order.exception.BusinessNoContentRequestException;
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
 * Handles {@link BusinessNoContentRequestException} and returns
 * a proper HTTP 204 No Content response along with a localized message.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@Slf4j
public class NoContentExceptionHandler {

    private final MessageSource messageSource;

    /**
     * Constructs the exception handler with a {@link MessageSource}
     * for resolving localized error messages.
     *
     * @param messageSource the message source
     */
    @Autowired
    public NoContentExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Handles {@link BusinessNoContentRequestException} exceptions thrown in controllers.
     *
     * @param e      the exception containing the message code and arguments
     * @param locale the client's locale for message translation
     * @return a response entity with HTTP 204 No Content and the resolved message
     */
    @ExceptionHandler(BusinessNoContentRequestException.class)
    public ResponseEntity<RestAPIResponse> badRequestExceptionHandler(
            BusinessNoContentRequestException e, Locale locale) {

        log.warn("NoContentRequest: ", e);

        String messageName = e.getMessage();
        Object[] args = e.getArgs();

        String message = messageSource.getMessage(messageName, args, locale);

        RestAPIResponse restAPIResponse = RestAPIResponse.builder()
                .httpStatus(HttpStatus.NO_CONTENT.value())
                .timeStamp(System.currentTimeMillis())
                .data(null)
                .error(message)
                .build();

        log.warn("NoContentRequest: {}", message);

        return new ResponseEntity<>(restAPIResponse, HttpStatus.NO_CONTENT);
    }
}
