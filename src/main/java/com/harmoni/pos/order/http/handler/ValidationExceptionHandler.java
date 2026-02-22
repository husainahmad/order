package com.harmoni.pos.order.http.handler;

import com.harmoni.pos.order.http.response.RestAPIResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler for validation errors in the POS order service.
 * <p>
 * Captures and formats {@link MethodArgumentNotValidException} thrown when a request body fails
 * to pass bean validation (e.g., annotated with {@code @Valid}).
 * </p>
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@Slf4j
public class ValidationExceptionHandler {

    /**
     * Handles exceptions thrown when method arguments annotated with {@code @Valid}
     * fail validation during request body binding.
     *
     * @param e the {@link MethodArgumentNotValidException} containing details about validation failures
     * @return a {@link ResponseEntity} containing a {@link RestAPIResponse} with a list of validation error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestAPIResponse> handleValidationException(MethodArgumentNotValidException e) {
        List<String> errors = getErrors(e);

        RestAPIResponse validationResponse = RestAPIResponse.builder()
                .httpStatus(HttpStatus.BAD_REQUEST.value())
                .timeStamp(System.currentTimeMillis())
                .data(null)
                .error(errors)
                .build();

        log.warn("Validation failed: {}", errors);

        return new ResponseEntity<>(validationResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Extracts field-level and global-level validation errors from the exception.
     *
     * @param e the exception containing validation errors
     * @return a list of formatted error messages
     */
    private static List<String> getErrors(MethodArgumentNotValidException e) {
        List<String> errors = new ArrayList<>();

        // Collect field-specific validation errors
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errors.add(String.format("%s: %s", fieldError.getField(), fieldError.getDefaultMessage()));
        }

        // Collect global object-level validation errors
        for (ObjectError objectError : e.getBindingResult().getGlobalErrors()) {
            errors.add(String.format("%s: %s", objectError.getObjectName(), objectError.getDefaultMessage()));
        }

        return errors;
    }
}
