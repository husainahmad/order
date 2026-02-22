package com.harmoni.pos.order.business.service.menu;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmoni.pos.order.business.service.rest.RestClientService;
import com.harmoni.pos.order.http.response.RestAPIResponse;
import com.harmoni.pos.order.model.dto.StoreDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.Serializable;
import java.util.Optional;

/**
 * Service for retrieving store-related information from the menu service.
 * <p>
 * This class communicates with the external store endpoint and maps the response
 * into a {@link StoreDto} object using Jackson.
 * </p>
 */
@RequiredArgsConstructor
@Service("storeService")
public class StoreService implements Serializable {

    private final RestClientService restClientService;

    @Value("${menu.url.store}")
    private String urlStore;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

    /**
     * Fetches detailed information about a specific store by its ID.
     *
     * @param storeId the unique identifier of the store
     * @param token   the bearer token used for authentication in the request
     * @return a {@link StoreDto} containing store details
     * @throws java.util.NoSuchElementException if the response is null or invalid
     */
    public StoreDto getStoreDetail(Integer storeId, String token) {
        String finalUrl = UriComponentsBuilder
                .fromHttpUrl(urlStore.concat("/").concat(String.valueOf(storeId)))
                .toUriString();

        RestAPIResponse restAPIResponse = restClientService.get(token, finalUrl).block();

        return Optional.ofNullable(restAPIResponse)
                .map(response -> objectMapper.convertValue(response.getData(), new TypeReference<StoreDto>() {}))
                .orElseThrow();
    }
}
