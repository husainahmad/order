package com.harmoni.pos.order.http.controller;

import com.harmoni.pos.order.http.response.RestAPIResponse;
import com.harmoni.pos.order.model.OrderCustomer;
import com.harmoni.pos.order.service.OrderCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * Reads what a customer has ordered.
 * <p>
 * The customer service owns customer identity; this only owns the record of which
 * orders belonged to whom. The name and phone returned are the ones captured at the
 * time of each sale, so history stays correct after a customer is renamed or removed.
 * <p>
 * There is no write endpoint. Snapshots are taken during confirmation from the order
 * being confirmed, so a way to post one in would mean accepting a customer identity
 * from the network that no sale ever established.
 *
 * @author husainahmad
 */
@RestController
@RequestMapping("/api/v1/order-customers")
@RequiredArgsConstructor
public class OrderCustomerController {

    private final OrderCustomerService orderCustomerService;

    /**
     * Retrieves the customer captured for one order.
     *
     * <p><b>Endpoint:</b> GET /api/v1/order-customers/order/{orderId}</p>
     *
     * @param orderId  the order ID
     * @param username the operator making the request
     * @return the customer snapshot, or a response with no data for a walk-in sale
     */
    @GetMapping("/order/{orderId:\\d+}")
    public ResponseEntity<RestAPIResponse> getByOrderId(
            @PathVariable("orderId") Integer orderId,
            @RequestHeader("X-Username") String username
    ) {
        OrderCustomer snapshot = orderCustomerService.getByOrderId(orderId);
        RestAPIResponse restAPIResponse = RestAPIResponse.builder()
                .httpStatus(HttpStatus.OK.value())
                .data(snapshot)
                .build();
        return new ResponseEntity<>(restAPIResponse, HttpStatus.OK);
    }

    /**
     * Retrieves everything a customer has ordered, most recent first. This is the order
     * history, and it does not depend on the customer record still existing in the
     * customer service.
     *
     * <p><b>Endpoint:</b> GET /api/v1/order-customers/customer/{customerId}/history</p>
     *
     * @param customerId the customer in the customer service
     * @param from       optional inclusive lower bound, ISO-8601, e.g. 2026-01-01T00:00:00
     * @param to         optional inclusive upper bound, ISO-8601, e.g. 2026-01-31T23:59:59
     * @param username   the operator making the request
     * @return the customer's orders, most recent first
     */
    @GetMapping("/customer/{customerId:\\d+}/history")
    public ResponseEntity<RestAPIResponse> getHistory(
            @PathVariable("customerId") Long customerId,
            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestHeader("X-Username") String username
    ) {
        List<OrderCustomer> history = orderCustomerService.getHistory(customerId, toDate(from), toDate(to));
        RestAPIResponse restAPIResponse = RestAPIResponse.builder()
                .httpStatus(HttpStatus.OK.value())
                .data(history)
                .build();
        return new ResponseEntity<>(restAPIResponse, HttpStatus.OK);
    }

    /**
     * Converts an optional query parameter into the {@link Date} the mapper takes,
     * leaving an absent bound as {@code null} so the SQL omits the range entirely.
     *
     * @param value the parsed date time, or {@code null} when the caller omitted it
     * @return the equivalent legacy date, or {@code null}
     */
    private static Date toDate(LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
    }
}
