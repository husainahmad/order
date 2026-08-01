package com.harmoni.pos.order.http.controller;

import com.harmoni.pos.order.business.service.ReportBusinessService;
import com.harmoni.pos.order.business.service.menu.UserService;
import com.harmoni.pos.order.http.response.RestAPIResponse;
import com.harmoni.pos.order.model.SettlementReport;
import com.harmoni.pos.order.model.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final UserService userService;
    private final ReportBusinessService reportBusinessService;

    /**
     * Retrieves the settlement report for the authenticated user's store
     * within a specific date-time range.
     *
     * <p><b>Endpoint:</b> GET /api/v1/reports/settlement</p>
     *
     * @param start      the start date and time (ISO format), e.g., 2025-06-01T00:00:00
     * @param end        the end date and time (ISO format), e.g., 2025-06-01T23:59:59
     * @param authHeader the Authorization header containing a Bearer JWT token
     * @return a ResponseEntity containing the settlement report wrapped in RestAPIResponse
     */
    @GetMapping("/settlement")
    public ResponseEntity<RestAPIResponse> getSettlementReport(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestHeader("X-Username") String username
    ) {
        SettlementReport report = reportBusinessService.generateSettlementReport(getUserDto(username).getStoreId(), start, end);
        RestAPIResponse restAPIResponse = RestAPIResponse.builder()
                .data(report)
                .build();
        return new ResponseEntity<>(restAPIResponse, HttpStatus.OK);
    }

    /**
     * Retrieves the top-selling products for the authenticated user's store
     * within a specific date-time range, limited by the given number of results.
     *
     * <p><b>Endpoint:</b> GET /api/v1/reports/top-products</p>
     *
     * @param start    the start date and time (ISO format), e.g., 2025-06-01T00:00:00
     * @param end      the end date and time (ISO format), e.g., 2025-06-14T23:59:59
     * @param username the username extracted from the X-Username header
     * @param limit    optional number of top products to return; defaults to 10
     * @return a ResponseEntity containing the list of top products in RestAPIResponse
     */
    @GetMapping("/top-products")
    public ResponseEntity<RestAPIResponse> getTopProducts(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestHeader("X-Username") String username,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        RestAPIResponse restAPIResponse = RestAPIResponse.builder()
                .data(reportBusinessService.getTopProducts(getUserDto(username).getStoreId(), start, end, limit))
                .build();
        return new ResponseEntity<>(restAPIResponse, HttpStatus.OK);
    }

    /**
     * Retrieves the user information (UserDto) for the given username.
     *
     * @param username the username to look up
     * @return UserDto containing user details including store ID
     */
    private UserDto getUserDto(String username) {
        return userService.getUserDetail(username);
    }

}
