package com.harmoni.pos.order.business.service;

/**
 * Publishes a serialized order-created event to the customer service.
 */
public interface CustomerEventPublisher {

    void publish(String payload);
}
