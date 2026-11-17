package com.harmoni.pos.order.http.controller;

import com.harmoni.pos.order.business.service.OrderBusinessService;
import com.harmoni.pos.order.http.response.RestAPIResponse;
import com.harmoni.pos.order.model.dto.OrderDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for handling order-related operations such as creation, printing,
 * listing daily orders, and voiding orders.
 */
@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api/v1/order")
public class OrderController {

    private final OrderBusinessService orderBusinessService;

    /**
     * Creates a new order.
     *
     * @param orderDto       the order data transfer object
     * @param username       the username extracted from the X-Username header
     * @param idempotencyKey optional client-supplied idempotency key; when present, a
     *                       repeated request with the same key returns the existing order
     * @return a {@link ResponseEntity} containing the created order and HTTP status 201
     */
    @PostMapping("")
    @PreAuthorize("hasAnyAuthority('order:view', 'terminal:view', 'availability:view', 'rbac:manage')")
    public ResponseEntity<RestAPIResponse> createOrder(@Valid @RequestBody OrderDto orderDto,
                                                       @RequestHeader("X-Username") String username,
                                                       @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        RestAPIResponse restAPIResponse = RestAPIResponse.builder()
                .data(orderBusinessService.confirm(orderDto, username, idempotencyKey))
                .build();
        return new ResponseEntity<>(restAPIResponse, HttpStatus.CREATED);
    }

    /**
     * Prints the specified order (e.g., to receipt printer).
     *
     * @param orderDto the order data transfer object
     * @param username the username extracted from the X-Username header
     * @return a {@link ResponseEntity} containing print result and HTTP status 200
     */
    @PostMapping("/print")
    @PreAuthorize("hasAnyAuthority('order:view', 'terminal:view', 'availability:view', 'rbac:manage')")
    public ResponseEntity<RestAPIResponse> printOrder(@Valid @RequestBody OrderDto orderDto,
                                                       @RequestHeader("X-Username") String username) {
        RestAPIResponse restAPIResponse = RestAPIResponse.builder()
                .data(orderBusinessService.print(orderDto, username))
                .build();
        return new ResponseEntity<>(restAPIResponse, HttpStatus.OK);
    }

    /**
     * Retrieves the list of orders created on the current day for the store associated with the authenticated user.
     *
     * @param username the username extracted from the X-Username header
     * @return a {@link ResponseEntity} containing the list of today's orders and HTTP status 201
     */
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('order:view', 'terminal:view', 'availability:view', 'rbac:manage')")
    public ResponseEntity<RestAPIResponse> getListOrder(@RequestHeader("X-Username") String username) {
        RestAPIResponse restAPIResponse = RestAPIResponse.builder()
                .data(orderBusinessService.getDailyListByStore(username))
                .build();
        return new ResponseEntity<>(restAPIResponse, HttpStatus.CREATED);
    }

    /**
     * Retrieves one page of the current store's orders for today, newest first.
     *
     * <p><b>Endpoint:</b> GET /api/v1/order/page?limit=50&amp;beforeId=9577</p>
     *
     * <p>The response is <code>{ orders, nextCursor, hasMore }</code>. Pass the
     * {@code nextCursor} back as {@code beforeId} to append the next page; {@code hasMore}
     * is false on the last one. The cursor is an order id rather than an offset so a sale
     * confirmed while the cashier is scrolling cannot make the next page repeat a row.</p>
     *
     * @param username the username extracted from the X-Username header
     * @param limit    the most orders to return; defaults to 50, capped at 200
     * @param beforeId return only orders older than this id; omit it for the first page
     * @return a {@link ResponseEntity} containing the page and HTTP status 200
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyAuthority('order:view', 'terminal:view', 'availability:view', 'rbac:manage')")
    public ResponseEntity<RestAPIResponse> getOrderPage(
            @RequestHeader("X-Username") String username,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "beforeId", required = false) Integer beforeId) {
        RestAPIResponse restAPIResponse = RestAPIResponse.builder()
                .data(orderBusinessService.getDailyPageByStore(username, beforeId, limit))
                .build();
        return new ResponseEntity<>(restAPIResponse, HttpStatus.OK);
    }

    /**
     * Voids an order by its ID.
     *
     * @param orderId  the ID of the order to be voided
     * @param username the username extracted from the X-Username header
     * @return a {@link ResponseEntity} containing the result of the void operation and HTTP status 200
     */
    @PutMapping("/{orderId}/void")
    @PreAuthorize("hasAnyAuthority('order:approve', 'rbac:manage')")
    public ResponseEntity<RestAPIResponse> voidOrder(@PathVariable Integer orderId,
                                                      @RequestHeader("X-Username") String username) {
        RestAPIResponse response = RestAPIResponse.builder()
                .data(orderBusinessService.voidOrder(orderId, username))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
