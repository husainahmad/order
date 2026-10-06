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

/**
 * {@code RestClientService} is a centralized WebClient-based service
 * that facilitates asynchronous HTTP communication (GET, POST, PUT, DELETE)
 * with external REST APIs.
 * <p>
 * This service includes consistent error handling and bearer token-based authorization.
 */
@RequiredArgsConstructor
@Service("restClientService")
@Slf4j
public class RestClientService implements Serializable {

    private static final String LOG_BAD_REQUEST = "BAD_REQUEST Server Response {}";
    private static final String LOG_NO_CONTENT = "LOG_NO_CONTENT Server Response {}";
    private static final String LOG_UN_AUTHORIZED = "LOG_UN_AUTHORIZED Server Response {}";
    private static final String LOG_INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR Server Response {}";
    private static final String X_USERNAME = "X-Username";
    private static final WebClient webClient = WebClient.builder().build();

    /**
     * Sends a POST request to the given URL, identifying the caller by username alone.
     * <p>
     * Use this for the internal service to service calls, which are made on behalf of an
     * already authenticated operator and are not authorised with a bearer token. The
     * {@code X-Username} header is what the target service resolves the acting store from.
     *
     * @param username  username forwarded to the target service
     * @param url       Target URL
     * @param publisher Reactive body content
     * @param className Type of the request body
     * @return Mono emitting {@link RestAPIResponse}
     */
    public Mono<RestAPIResponse> post(String username, String url, Publisher<?> publisher, Class<?> className) {
        return webClient.post()
                .uri(url)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(X_USERNAME, username)
                .body(publisher, className)
                .retrieve()
                .onStatus(HttpStatus.NO_CONTENT::equals, this::handleNoContent)
                .onStatus(HttpStatus.BAD_REQUEST::equals, this::handleBadRequest)
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, this::handleInternalServerError)
                .onStatus(HttpStatus.UNAUTHORIZED::equals, this::handleUnAuthorized)
                .bodyToMono(RestAPIResponse.class);
    }

    /**
     * Sends a GET request to the given URL.
     *
     * @param username username forwarded to the target service
     * @param url      Target URL
     * @return Mono emitting {@link RestAPIResponse}
     */
    public Mono<RestAPIResponse> get(String username, String url) {
        WebClient.ResponseSpec retrieve = webClient.get()
                .uri(url)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(X_USERNAME, username)
                .retrieve();

        retrieve.onStatus(HttpStatus.NO_CONTENT::equals, this::handleNoContent);
        retrieve.onStatus(HttpStatus.BAD_REQUEST::equals, this::handleBadRequest);
        retrieve.onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, this::handleInternalServerError);
        retrieve.onStatus(HttpStatus.UNAUTHORIZED::equals, this::handleUnAuthorized);

        return retrieve.bodyToMono(RestAPIResponse.class);
    }

    private static void logError(String message, RestAPIResponse response) {
        log.error(message, response.getHttpStatus());
    }

    private Mono<? extends Throwable> handleBadRequest(ClientResponse response) {
        return response.bodyToMono(RestAPIResponse.class)
                .handle((restAPIResponse, sink) -> logError(LOG_BAD_REQUEST, restAPIResponse));
    }

    private Mono<? extends Throwable> handleNoContent(ClientResponse response) {
        return response.bodyToMono(RestAPIResponse.class)
                .handle((restAPIResponse, sink) -> logError(LOG_NO_CONTENT, restAPIResponse));
    }

    private Mono<? extends Throwable> handleUnAuthorized(ClientResponse response) {
        return response.bodyToMono(RestAPIResponse.class)
                .handle((restAPIResponse, sink) -> logError(LOG_UN_AUTHORIZED, restAPIResponse));
    }

    private Mono<? extends Throwable> handleInternalServerError(ClientResponse response) {
        return response.bodyToMono(RestAPIResponse.class)
                .handle((restAPIResponse, sink) -> logError(LOG_INTERNAL_SERVER_ERROR, restAPIResponse));
    }
}
