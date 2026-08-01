package com.harmoni.pos.order.business.service;

import com.harmoni.pos.order.business.service.menu.UserService;
import com.harmoni.pos.order.mapper.ReportMapper;
import com.harmoni.pos.order.model.PaymentSummary;
import com.harmoni.pos.order.model.SettlementReport;
import com.harmoni.pos.order.model.TopProductReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service implementation for generating sales and settlement reports.
 * Uses MyBatis mapper to fetch data from the database.
 */
@RequiredArgsConstructor
@Service("reportBusinessService")
public class ReportBusinessServiceImpl implements ReportBusinessService {

    private final ReportMapper reportMapper;
    private final UserService userService;

    /**
     * Generates a settlement report for the given store and time range.
     * The report includes order totals and a breakdown of payments by type.
     *
     * @param storeId the ID of the store to generate the report for
     * @param start   the start datetime of the report range
     * @param end     the end datetime of the report range
     * @return a {@link SettlementReport} containing summarized sales data and payment breakdown
     */
    @Override
    public SettlementReport generateSettlementReport(Integer storeId, LocalDateTime start, LocalDateTime end) {

        // Fetch main report data
        SettlementReport report = reportMapper.getSettlementReport(start, end);

        // Fetch payment breakdown
        List<PaymentSummary> paymentSummaries = reportMapper.getPaymentBreakdown(start, end);

        // Convert list to Map for easy access in the report
        Map<String, BigDecimal> paymentBreakdown = new HashMap<>();
        for (PaymentSummary paymentSummary : paymentSummaries) {
            paymentBreakdown.put(paymentSummary.getPaymentType(), paymentSummary.getTotalAmount());
        }

        report.setPaymentBreakdown(paymentBreakdown);

        return report;
    }

    /**
     * Retrieves a list of top-selling products within the specified date range
     * for the given store, limited by the specified number of results.
     *
     * @param storeId the ID of the store
     * @param start   the start datetime of the query range
     * @param end     the end datetime of the query range
     * @param limit   maximum number of top products to return
     * @return a list of {@link TopProductReport} items sorted by sales volume
     */
    @Override
    public List<TopProductReport> getTopProducts(Integer storeId, LocalDateTime start, LocalDateTime end, int limit) {
        return reportMapper.getTopProducts(storeId, start, end, limit);
    }
}
