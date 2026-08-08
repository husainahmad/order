package com.harmoni.pos.order.business.service;

import com.harmoni.pos.order.model.DailyReport;
import com.harmoni.pos.order.model.OrderVolumeReport;
import com.harmoni.pos.order.model.ProductSalesReport;
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

    /**
     * Retrieves a daily report for a store within a given date-time range.
     * Each day includes the products sold with their quantity, net sales, and discount.
     *
     * @param storeId the ID of the store
     * @param start   the start datetime of the reporting period
     * @param end     the end datetime of the reporting period
     * @return a list of {@link DailyReport} grouped by day
     */
    List<DailyReport> getDailyReport(Integer storeId, LocalDateTime start, LocalDateTime end);

    /**
     * Retrieves the sales report for a store within a given date-time range,
     * aggregated by product and category.
     *
     * @param storeId the ID of the store
     * @param start   the start datetime of the reporting period
     * @param end     the end datetime of the reporting period
     * @return a list of {@link ProductSalesReport} grouped by product and category
     */
    List<ProductSalesReport> getSalesReport(Integer storeId, LocalDateTime start, LocalDateTime end);

    /**
     * Retrieves the order volume report for a store within a given date-time range.
     * Reports the total number of orders, the number of orders placed during the
     * peak hour(s) of each day (the hour(s) with the most orders), and the number
     * of orders placed outside of those peak hours.
     *
     * @param storeId the ID of the store
     * @param start   the start datetime of the reporting period
     * @param end     the end datetime of the reporting period
     * @return an {@link OrderVolumeReport} with total, peak time, and non-peak time order counts
     */
    OrderVolumeReport getOrderVolumeReport(Integer storeId, LocalDateTime start, LocalDateTime end);
}
