package com.harmoni.pos.order.http.controller;

import com.harmoni.pos.order.business.service.OrderBusinessService;
import com.harmoni.pos.order.http.response.RestAPIResponse;
import com.harmoni.pos.order.model.dto.OrderDto;
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
public class OrderController {

    private final OrderBusinessService orderBusinessService;

    @PostMapping("")
    public ResponseEntity<RestAPIResponse> createOrder(@Valid @RequestBody OrderDto orderDto,
                                                    @RequestHeader("Authorization") String authHeader) {
        RestAPIResponse restAPIResponse = RestAPIResponse.builder()
                .data(orderBusinessService.confirm(orderDto, authHeader.substring(7)))
                .build();
        return new ResponseEntity<>(restAPIResponse, HttpStatus.CREATED);
    }

    @PostMapping("/print")
    public ResponseEntity<RestAPIResponse> printOrder(@Valid @RequestBody OrderDto orderDto,
                                                       @RequestHeader("Authorization") String authHeader) {
        RestAPIResponse restAPIResponse = RestAPIResponse.builder()
                .data(orderBusinessService.print(orderDto, authHeader.substring(7)))
                .build();
        return new ResponseEntity<>(restAPIResponse, HttpStatus.OK);
    }

    @GetMapping("")
    public ResponseEntity<RestAPIResponse> getListOrder(@RequestHeader("Authorization") String authHeader) {
        RestAPIResponse restAPIResponse = RestAPIResponse.builder()
                .data(orderBusinessService.getDailyListByStore(authHeader.substring(7)))
                .build();
        return new ResponseEntity<>(restAPIResponse, HttpStatus.CREATED);
    }
}
