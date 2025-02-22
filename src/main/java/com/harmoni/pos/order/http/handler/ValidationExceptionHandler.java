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

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@Slf4j
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestAPIResponse>
    handleValidationException(MethodArgumentNotValidException e) {

        List<String> errors = new ArrayList<>();

        for (FieldError fieldError: e.getBindingResult().getFieldErrors()) {
            errors.add(fieldError.getField() + ">" + fieldError.getDefaultMessage());
        }

        for (ObjectError objectError: e.getBindingResult().getGlobalErrors()) {
            errors.add(objectError.getObjectName() + ">" + objectError.getDefaultMessage());
        }

        RestAPIResponse genericResponse = RestAPIResponse.builder()
                .httpStatus(HttpStatus.BAD_REQUEST.value())
                .timeStamp(System.currentTimeMillis())
                .data(null)
                .error(errors)
                .build();

        log.warn("Validation: {}", e.getMessage());

        return new ResponseEntity<>(genericResponse, HttpStatus.BAD_REQUEST);
    }
}
