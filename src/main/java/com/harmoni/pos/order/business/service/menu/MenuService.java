package com.harmoni.pos.order.business.service.menu;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmoni.pos.order.business.service.rest.RestClientService;
import com.harmoni.pos.order.config.MenuProperties;
import com.harmoni.pos.order.http.response.RestAPIResponse;
import com.harmoni.pos.order.model.dto.ProductDto;
import com.harmoni.pos.order.model.dto.SkuDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class responsible for fetching menu-related data such as SKUs with prices and products with names.
 * <p>
 * It communicates with external services via {@link RestClientService} and deserializes
 * the responses into DTOs using Jackson's {@link ObjectMapper}.
 * </p>
 */
@RequiredArgsConstructor
@Service("menuService")
public class MenuService implements Serializable {

    private final RestClientService restClientService;
    private final MenuProperties menuProperties;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

    /**
     * Retrieves a list of {@link SkuDto} including price information for the given list of SKU IDs.
     *
     * @param ids a list of SKU ID integers
     * @return a list of {@link SkuDto} with price data, or an empty list if no data is returned
     */
    public List<SkuDto> getSkusWithPrice(List<Integer> ids) {
        String encodedIds = ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String finalUrl = UriComponentsBuilder.fromHttpUrl(menuProperties.getSkuPrice())
                .queryParam("ids", encodedIds)
                .toUriString();

        RestAPIResponse restAPIResponse = restClientService.get(finalUrl).block();

        return Optional.ofNullable(restAPIResponse)
                .map(response -> objectMapper.convertValue(response.getData(), new TypeReference<List<SkuDto>>() {}))
                .orElse(List.of());
    }

    /**
     * Retrieves a list of {@link ProductDto} with name and metadata for the given product IDs.
     *
     * @param ids a list of product ID integers
     * @return a list of {@link ProductDto} with names and attributes, or an empty list if no data is returned
     */
    public List<ProductDto> getProductWithName(List<Integer> ids) {
        String encodedIds = ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String finalUrl = UriComponentsBuilder.fromHttpUrl(menuProperties.getProduct())
                .queryParam("ids", encodedIds)
                .toUriString();

        RestAPIResponse restAPIResponse = restClientService.get(finalUrl).block();

        return Optional.ofNullable(restAPIResponse)
                .map(response -> objectMapper.convertValue(response.getData(), new TypeReference<List<ProductDto>>() {}))
                .orElse(List.of());
    }
}
