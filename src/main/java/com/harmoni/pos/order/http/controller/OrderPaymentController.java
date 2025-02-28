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

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api/v1/order")
public class OrderPaymentController {

    private final OrderPaymentBusinessService orderPaymentBusinessService;

    @PutMapping("")
    public ResponseEntity<RestAPIResponse> payOrder(@Valid @RequestBody OrderPaymentDto orderPaymentDto,
                                                    @RequestHeader("Authorization") String authHeader) {
        RestAPIResponse restAPIResponse = RestAPIResponse.builder()
                .data(orderPaymentBusinessService.processPayment(orderPaymentDto, authHeader.substring(7)))
                .build();
        return new ResponseEntity<>(restAPIResponse, HttpStatus.CREATED);
    }

}
