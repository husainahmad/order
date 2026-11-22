package com.harmoni.pos.order.business.service;

import com.harmoni.pos.order.config.EventProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class WebClientCustomerEventPublisher implements CustomerEventPublisher {

    private final WebClient webClient;

    public WebClientCustomerEventPublisher(EventProperties properties) {
        this.webClient = WebClient.builder()
                .baseUrl(properties.transactionEventsUrl())
                .build();
    }

    @Override
    public void publish(String payload) {
        webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
