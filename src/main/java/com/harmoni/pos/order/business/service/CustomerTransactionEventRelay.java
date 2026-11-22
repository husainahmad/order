package com.harmoni.pos.order.business.service;

import com.harmoni.pos.order.config.EventProperties;
import com.harmoni.pos.order.mapper.OrderTransactionOutboxMapper;
import com.harmoni.pos.order.model.OrderTransactionOutbox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Drains pending outbox rows to the customer service. A failed publish leaves the
 * row pending with an incremented attempt count, so the next tick retries it.
 */
@Slf4j
@Component
public class CustomerTransactionEventRelay {

    private static final int MAX_ERROR_LENGTH = 500;

    private final OrderTransactionOutboxMapper outboxMapper;
    private final CustomerEventPublisher publisher;
    private final EventProperties properties;

    public CustomerTransactionEventRelay(OrderTransactionOutboxMapper outboxMapper,
                                         CustomerEventPublisher publisher,
                                         EventProperties properties) {
        this.outboxMapper = outboxMapper;
        this.publisher = publisher;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${harmoni.events.poll-ms:5000}")
    public void relayPending() {
        List<OrderTransactionOutbox> pending;
        try {
            pending = outboxMapper.selectPending(properties.getBatchSize());
        } catch (Exception e) {
            log.debug("Transaction outbox not ready: {}", e.getMessage());
            return;
        }
        for (OrderTransactionOutbox row : pending) {
            try {
                publisher.publish(row.getPayload());
                outboxMapper.markSent(row.getId());
            } catch (Exception e) {
                log.warn("Failed to relay transaction event for order {}: {}",
                        row.getOrderId(), e.getMessage());
                outboxMapper.markFailed(row.getId(), truncate(e.getMessage()));
            }
        }
    }

    private static String truncate(String message) {
        if (message == null) {
            return "unknown error";
        }
        return message.length() > MAX_ERROR_LENGTH ? message.substring(0, MAX_ERROR_LENGTH) : message;
    }
}
