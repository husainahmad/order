package com.harmoni.pos.order.service;

import com.harmoni.pos.order.mapper.ReportMapper;
import com.harmoni.pos.order.model.PaymentSummary;
import com.harmoni.pos.order.model.SettlementReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the {@link ReportService} interface that provides reporting functionality
 * such as generating settlement reports based on payment transactions between specified time ranges.
 * <p>
 * This service queries the database using the {@link ReportMapper} to retrieve summarized data
 * and composes it into a {@link SettlementReport} object, including a breakdown by payment type.
 * </p>
 *
 * @author Ahmad
 * @since 1.0
 */
@RequiredArgsConstructor
@Service("reportService")
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;

    /**
     * Generates a settlement report containing total transaction summaries and a breakdown
     * of payment methods used between the specified start and end times.
     *
     * @param start the beginning of the reporting time range (inclusive)
     * @param end   the end of the reporting time range (inclusive)
     * @return a {@link SettlementReport} containing the total summary and payment breakdown
     * @throws IllegalArgumentException if the start time is after the end time
     */
    @Override
    public SettlementReport generateSettlementReport(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }

        SettlementReport report = reportMapper.getSettlementReport(start, end);
        if (report == null) {
            report = new SettlementReport();
        }

        List<PaymentSummary> payments = reportMapper.getPaymentBreakdown(start, end);
        Map<String, BigDecimal> breakdown = new HashMap<>();
        if (payments != null) {
            for (PaymentSummary ps : payments) {
                breakdown.put(ps.getPaymentType(), ps.getTotalAmount());
            }
        }

        report.setPaymentBreakdown(breakdown);
        return report;
    }
}
