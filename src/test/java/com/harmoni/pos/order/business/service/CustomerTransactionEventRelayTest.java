package com.harmoni.pos.order.business.service;

import com.harmoni.pos.order.config.EventProperties;
import com.harmoni.pos.order.mapper.OrderTransactionOutboxMapper;
import com.harmoni.pos.order.model.OrderTransactionOutbox;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerTransactionEventRelayTest {

    @Mock
    private OrderTransactionOutboxMapper outboxMapper;

    @Mock
    private CustomerEventPublisher publisher;

    private CustomerTransactionEventRelay relay() {
        return new CustomerTransactionEventRelay(outboxMapper, publisher, new EventProperties());
    }

    @Test
    void relayPending_publishesAndMarksRowsSent() {
        OrderTransactionOutbox row = new OrderTransactionOutbox().setId(1L).setOrderId(42).setPayload("{}");
        when(outboxMapper.selectPending(20)).thenReturn(List.of(row));

        relay().relayPending();

        verify(publisher).publish("{}");
        verify(outboxMapper).markSent(1L);
        verify(outboxMapper, never()).markFailed(eq(1L), anyString());
    }

    @Test
    void relayPending_whenPublishFails_marksRowFailedForRetry() {
        OrderTransactionOutbox row = new OrderTransactionOutbox().setId(2L).setOrderId(43).setPayload("{}");
        when(outboxMapper.selectPending(20)).thenReturn(List.of(row));
        doThrow(new IllegalStateException("connection refused")).when(publisher).publish("{}");

        relay().relayPending();

        verify(outboxMapper, never()).markSent(2L);
        verify(outboxMapper).markFailed(eq(2L), eq("connection refused"));
    }

    @Test
    void relayPending_whenOutboxIsUnavailable_doesNothing() {
        when(outboxMapper.selectPending(20)).thenThrow(new IllegalStateException("no datasource"));

        relay().relayPending();

        verify(publisher, never()).publish(anyString());
    }
}
