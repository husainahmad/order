package com.harmoni.pos.order.http.controller;

import com.harmoni.pos.order.business.service.OrderBusinessService;
import com.harmoni.pos.order.component.JwtUtil;
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

    private final JwtUtil jwtUtil;
    private final OrderBusinessService orderBusinessService;

    @PostMapping("")
    public ResponseEntity<RestAPIResponse> createOrder(@Valid @RequestBody OrderDto orderDto,
                                                    @RequestHeader("Authorization") String authHeader) throws Exception {
        orderBusinessService.test(orderDto);

        return new ResponseEntity<>(RestAPIResponse.builder().build(), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestAPIResponse> updateOrder(@PathVariable Integer id, @RequestBody OrderDto orderDto) {

        return new ResponseEntity<>(RestAPIResponse.builder().build(), HttpStatus.OK);
    }

    @GetMapping("")
    public ResponseEntity<RestAPIResponse> gets(@RequestHeader("Authorization") String authHeader)  {
        RestAPIResponse restAPIResponse = RestAPIResponse.builder()
                .httpStatus(HttpStatus.OK.value())
                .data(orderBusinessService.gets())
                .build();
        return new ResponseEntity<>(restAPIResponse, HttpStatus.OK);
    }

}
