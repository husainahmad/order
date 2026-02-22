package com.harmoni.pos.order.http.controller;

import com.harmoni.pos.order.business.service.OrderPaymentBusinessService;
import com.harmoni.pos.order.http.response.RestAPIResponse;
import com.harmoni.pos.order.model.dto.OrderPaymentDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for handling order payment-related operations in the POS system.
 * <p>
 * Exposes endpoints for clients to process order payments.
 * </p>
 */
@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api/v1/order")
public class OrderPaymentController {

    private final OrderPaymentBusinessService orderPaymentBusinessService;

    /**
     * Processes payment for an order.
     * <p>
     * Expects a valid {@link OrderPaymentDto} payload and an Authorization header with a Bearer token.
     * The token is passed to the business service for authentication and processing.
     * </p>
     *
     * @param orderPaymentDto the payment details for the order
     * @param authHeader      the Authorization header containing a Bearer token
     * @return a {@link ResponseEntity} containing a {@link RestAPIResponse} with the result of the payment processing
     */
    @PutMapping("")
    public ResponseEntity<RestAPIResponse> payOrder(@Valid @RequestBody OrderPaymentDto orderPaymentDto,
                                                    @RequestHeader("Authorization") String authHeader) {
        RestAPIResponse restAPIResponse = RestAPIResponse.builder()
                .data(orderPaymentBusinessService.processPayment(orderPaymentDto, authHeader.substring(7)))
                .build();
        return new ResponseEntity<>(restAPIResponse, HttpStatus.CREATED);
    }

}
