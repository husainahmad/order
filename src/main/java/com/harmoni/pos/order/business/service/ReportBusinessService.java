package com.harmoni.pos.order.business.service;

import com.harmoni.pos.order.model.SettlementReport;
import com.harmoni.pos.order.model.TopProductReport;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Business service interface for generating various reports related to orders and products.
 */
public interface ReportBusinessService {

    /**
     * Generates a settlement report for a specific store within a given date-time range.
     *
     * @param storeId the ID of the store to generate the report for
     * @param start   the start datetime of the reporting period
     * @param end     the end datetime of the reporting period
     * @return a {@link SettlementReport} containing aggregated sales, voids, and payment info
     */
    SettlementReport generateSettlementReport(Integer storeId, LocalDateTime start, LocalDateTime end);

    /**
     * Retrieves the top-selling products for a store within a specified time period.
     *
     * @param storeId the ID of the store
     * @param start   the start datetime of the reporting window
     * @param end     the end datetime of the reporting window
     * @param limit   the maximum number of top products to return
     * @return a list of {@link TopProductReport} ordered by sales volume or revenue
     */
    List<TopProductReport> getTopProducts(Integer storeId, LocalDateTime start, LocalDateTime end, int limit);
}
