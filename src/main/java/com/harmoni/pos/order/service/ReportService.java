package com.harmoni.pos.order.service;

import com.harmoni.pos.order.model.SettlementReport;

import java.time.LocalDateTime;

public interface ReportService {
    SettlementReport generateSettlementReport(LocalDateTime start, LocalDateTime end);
}
