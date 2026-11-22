package com.harmoni.pos.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "harmoni.events")
public class EventProperties {

    private String customerBaseUrl = "http://localhost:8084";
    private String transactionEventsPath = "/internal/v1/customer-transactions";
    private long pollMs = 5000;
    private int batchSize = 20;

    public String transactionEventsUrl() {
        return customerBaseUrl + transactionEventsPath;
    }
}
