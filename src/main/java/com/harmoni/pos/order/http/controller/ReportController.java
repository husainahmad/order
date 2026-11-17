package com.harmoni.pos.order.http.controller;

import com.harmoni.pos.order.business.service.ReportBusinessService;
import com.harmoni.pos.order.business.service.menu.UserService;
import com.harmoni.pos.order.component.CallerScope;
import com.harmoni.pos.order.component.CallerScopeContext;
import com.harmoni.pos.order.http.response.RestAPIResponse;
import com.harmoni.pos.order.model.SettlementReport;
import com.harmoni.pos.order.model.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @PreAuthorize("hasAnyAuthority('report:view', 'report:read', 'rbac:manage')")
    public ResponseEntity<RestAPIResponse> getSettlementReport(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(value = "storeIds", required = false) List<Integer> storeIds,
            @RequestHeader("X-Username") String username
    ) {
        SettlementReport report = reportBusinessService.generateSettlementReport(resolveStoreIds(storeIds, username), start, end);
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
    @PreAuthorize("hasAnyAuthority('report:view', 'report:read', 'rbac:manage')")
    public ResponseEntity<RestAPIResponse> getTopProducts(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(value = "storeIds", required = false) List<Integer> storeIds,
            @RequestHeader("X-Username") String username,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        RestAPIResponse restAPIResponse = RestAPIResponse.builder()
                .data(reportBusinessService.getTopProducts(resolveStoreIds(storeIds, username), start, end, limit))
                .build();
        return new ResponseEntity<>(restAPIResponse, HttpStatus.OK);
    }

    /**
     * Retrieves the daily report for the authenticated user's store
     * within a specific date-time range. Each day contains the products sold
     * with their quantity, net sales, and discount.
     *
     * <p><b>Endpoint:</b> GET /api/v1/reports/daily</p>
     *
     * @param start    the start date and time (ISO format), e.g., 2025-06-01T00:00:00
     * @param end      the end date and time (ISO format), e.g., 2025-06-14T23:59:59
     * @param username the username extracted from the X-Username header
     * @return a ResponseEntity containing the list of daily reports in RestAPIResponse
     */
    @GetMapping("/daily")
    @PreAuthorize("hasAnyAuthority('report:view', 'report:read', 'rbac:manage')")
    public ResponseEntity<RestAPIResponse> getDailyReport(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(value = "storeIds", required = false) List<Integer> storeIds,
            @RequestHeader("X-Username") String username
    ) {
        RestAPIResponse restAPIResponse = RestAPIResponse.builder()
                .data(reportBusinessService.getDailyReport(resolveStoreIds(storeIds, username), start, end))
                .build();
        return new ResponseEntity<>(restAPIResponse, HttpStatus.OK);
    }

    /**
     * Retrieves the sales report for the authenticated user's store within a
     * specific date-time range, aggregated by product and category.
     *
     * <p><b>Endpoint:</b> GET /api/v1/reports/sales</p>
     *
     * @param start    the start date and time (ISO format), e.g., 2025-06-01T00:00:00
     * @param end      the end date and time (ISO format), e.g., 2025-06-14T23:59:59
     * @param username the username extracted from the X-Username header
     * @return a ResponseEntity containing the list of product sales grouped by category
     */
    @GetMapping("/sales")
    @PreAuthorize("hasAnyAuthority('report:view', 'report:read', 'rbac:manage')")
    public ResponseEntity<RestAPIResponse> getSalesReport(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(value = "storeIds", required = false) List<Integer> storeIds,
            @RequestHeader("X-Username") String username
    ) {
        RestAPIResponse restAPIResponse = RestAPIResponse.builder()
                .data(reportBusinessService.getSalesReport(resolveStoreIds(storeIds, username), start, end))
                .build();
        return new ResponseEntity<>(restAPIResponse, HttpStatus.OK);
    }

    /**
     * Retrieves the order volume report for the authenticated user's store within
     * a specific date-time range, showing the total number of orders, the number
     * placed during each day's peak hour(s), and the number placed outside of them.
     *
     * <p><b>Endpoint:</b> GET /api/v1/reports/order-volume</p>
     *
     * @param start    the start date and time (ISO format), e.g., 2025-06-01T00:00:00
     * @param end      the end date and time (ISO format), e.g., 2025-06-14T23:59:59
     * @param username the username extracted from the X-Username header
     * @return a ResponseEntity containing the order volume report in RestAPIResponse
     */
    @GetMapping("/order-volume")
    @PreAuthorize("hasAnyAuthority('report:view', 'report:read', 'rbac:manage')")
    public ResponseEntity<RestAPIResponse> getOrderVolumeReport(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(value = "storeIds", required = false) List<Integer> storeIds,
            @RequestHeader("X-Username") String username
    ) {
        RestAPIResponse restAPIResponse = RestAPIResponse.builder()
                .data(reportBusinessService.getOrderVolumeReport(resolveStoreIds(storeIds, username), start, end))
                .build();
        return new ResponseEntity<>(restAPIResponse, HttpStatus.OK);
    }

    /**
     * Resolves the store scope for a report: the explicit store ids when the
     * caller supplied them, otherwise the signed-in user's store.
     *
     * @param storeIds the requested store ids, may be {@code null}
     * @param username the signed-in user
     * @return the store ids to report on, never {@code null}
     */
    private List<Integer> resolveStoreIds(List<Integer> storeIds, String username) {
        List<Integer> requested = new ArrayList<>();
        if (storeIds != null && !storeIds.isEmpty()) {
            requested.addAll(storeIds);
        } else {
            Integer storeId = getUserDto(username).getStoreId();
            if (storeId != null) {
                requested.add(storeId);
            }
        }
        CallerScope scope = CallerScopeContext.current();
        return scope.filterStoreIds(requested);
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
