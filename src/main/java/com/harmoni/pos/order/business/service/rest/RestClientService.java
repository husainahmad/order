package com.harmoni.pos.order.business.service.rest;

import com.harmoni.pos.order.http.response.RestAPIResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.Serializable;

@RequiredArgsConstructor
@Service("restClientService")
@Slf4j
public class RestClientService implements Serializable {

    private static final String LOG_BAD_REQUEST = "BAD_REQUEST Server Response {}";
    private static final String LOG_NO_CONTENT = "LOG_NO_CONTENT Server Response {}";
    private static final String LOG_UN_AUTHORIZED = "LOG_UN_AUTHORIZED Server Response {}";
    private static final String LOG_INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR Server Response {}";
    private static final String BEARER = "Bearer ";
    private static final WebClient webClient = WebClient.builder().build();

    public Mono<RestAPIResponse> post(String token, String url, Publisher<?> publisher, Class<?> className) {
        return
                webClient.post()
                        .uri(url)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.AUTHORIZATION, BEARER.concat(token))
                        .body(publisher, className)
                        .retrieve()
                        .onStatus(httpStatusCode -> httpStatusCode.equals(HttpStatus.NO_CONTENT),this::handleNoContent)
                        .onStatus(httpStatusCode -> httpStatusCode.equals(HttpStatus.BAD_REQUEST),this::handleBadRequest)
                        .onStatus(httpStatusCode -> httpStatusCode.equals(HttpStatus.INTERNAL_SERVER_ERROR),this::handleInternalServerError)
                        .onStatus(httpStatusCode -> httpStatusCode.equals(HttpStatus.UNAUTHORIZED), this::handleUnAuthorized)
                        .bodyToMono(RestAPIResponse.class);
    }

    public Mono<RestAPIResponse> get(String token, String url) {
        WebClient.ResponseSpec retrieve = webClient.get()
                .uri(url)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, BEARER.concat(token))
                .retrieve();
        retrieve.onStatus(httpStatusCode -> httpStatusCode.equals(HttpStatus.NO_CONTENT),this::handleNoContent);
        retrieve.onStatus(httpStatusCode -> httpStatusCode.equals(HttpStatus.BAD_REQUEST),this::handleBadRequest);
        retrieve.onStatus(httpStatusCode -> httpStatusCode.equals(HttpStatus.INTERNAL_SERVER_ERROR),this::handleInternalServerError);
        retrieve.onStatus(httpStatusCode -> httpStatusCode.equals(HttpStatus.UNAUTHORIZED), this::handleUnAuthorized);
        return retrieve.bodyToMono(RestAPIResponse.class);
    }

    public Mono<RestAPIResponse> put(String token, String url, Publisher<?> publisher, Class<?> className) {
        return
                webClient.put()
                        .uri(url)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.AUTHORIZATION, BEARER.concat(token))
                        .body(publisher, className)
                        .retrieve()
                        .onStatus(httpStatusCode -> httpStatusCode.equals(HttpStatus.NO_CONTENT),this::handleNoContent)
                        .onStatus(httpStatusCode -> httpStatusCode.equals(HttpStatus.BAD_REQUEST),this::handleBadRequest)
                        .onStatus(httpStatusCode -> httpStatusCode.equals(HttpStatus.INTERNAL_SERVER_ERROR),this::handleInternalServerError)
                        .onStatus(httpStatusCode -> httpStatusCode.equals(HttpStatus.UNAUTHORIZED), this::handleUnAuthorized)
                        .bodyToMono(RestAPIResponse.class);
    }

    public Mono<RestAPIResponse> delete(String token, String url) {
        return
                webClient.delete()
                        .uri(url)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.AUTHORIZATION, BEARER.concat(token))
                        .retrieve()
                        .onStatus(httpStatusCode -> httpStatusCode.equals(HttpStatus.NO_CONTENT),this::handleNoContent)
                        .onStatus(httpStatusCode -> httpStatusCode.equals(HttpStatus.BAD_REQUEST),this::handleBadRequest)
                        .onStatus(httpStatusCode -> httpStatusCode.equals(HttpStatus.INTERNAL_SERVER_ERROR),this::handleInternalServerError)
                        .onStatus(httpStatusCode -> httpStatusCode.equals(HttpStatus.UNAUTHORIZED), this::handleUnAuthorized)
                        .bodyToMono(RestAPIResponse.class);
    }

    private static void logError(String s, RestAPIResponse restAPIResponse) {
        log.error(s, restAPIResponse.getHttpStatus());
    }

    private Mono<? extends Throwable> handleBadRequest(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(RestAPIResponse.class)
                .handle(((restAPIResponse, throwableSynchronousSink) -> logError(LOG_BAD_REQUEST, restAPIResponse)));
    }

    private Mono<? extends Throwable> handleNoContent(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(RestAPIResponse.class)
                .handle(((restAPIResponse, throwableSynchronousSink) -> logError(LOG_NO_CONTENT, restAPIResponse)));
    }

    private Mono<? extends Throwable> handleUnAuthorized(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(RestAPIResponse.class)
                .handle(((restAPIResponse, throwableSynchronousSink) -> logError(LOG_UN_AUTHORIZED, restAPIResponse)));
    }

    private Mono<? extends Throwable> handleInternalServerError(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(RestAPIResponse.class)
                .handle(((restAPIResponse, throwableSynchronousSink) -> logError(LOG_INTERNAL_SERVER_ERROR, restAPIResponse)));
    }

}
