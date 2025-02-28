package com.harmoni.pos.order.business.service.menu;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmoni.pos.order.business.service.rest.RestClientService;
import com.harmoni.pos.order.http.response.RestAPIResponse;
import com.harmoni.pos.order.model.dto.ProductDto;
import com.harmoni.pos.order.model.dto.SkuDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service("menuService")
public class MenuService implements Serializable {

    private final RestClientService restClientService;

    @Value("${menu.url.sku.price}")
    private String urlSkuPrice;

    @Value("${menu.url.product}")
    private String urlProduct;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

    public List<SkuDto> getSkusWithPrice(List<Integer> ids, String token) {
        String encodedIds = ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String finalUrl = UriComponentsBuilder.fromHttpUrl(urlSkuPrice)
                .queryParam("ids", encodedIds)
                .toUriString();

        RestAPIResponse restAPIResponse = restClientService.get(token, finalUrl).block();

        return Optional.ofNullable(restAPIResponse)
                .map(response -> objectMapper.convertValue(response.getData(), new TypeReference<List<SkuDto>>() {}))
                .orElse(List.of());
    }

    public List<ProductDto> getProductWithName(List<Integer> ids, String token) {
        String encodedIds = ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String finalUrl = UriComponentsBuilder.fromHttpUrl(urlProduct)
                .queryParam("ids", encodedIds)
                .toUriString();

        RestAPIResponse restAPIResponse = restClientService.get(token, finalUrl).block();

        return Optional.ofNullable(restAPIResponse)
                .map(response -> objectMapper.convertValue(response.getData(), new TypeReference<List<ProductDto>>() {}))
                .orElse(List.of());
    }

}
