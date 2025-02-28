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

@RequiredArgsConstructor
@Service("storeService")
public class StoreService implements Serializable {

    private final RestClientService restClientService;

    @Value("${menu.url.store}")
    private String urlStore;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

    public StoreDto getStoreDetail(Integer storeId, String token) {
        String finalUrl = UriComponentsBuilder.fromHttpUrl(urlStore.concat("/").concat(String.valueOf(storeId)))
                .toUriString();

        RestAPIResponse restAPIResponse = restClientService.get(token, finalUrl).block();

        return Optional.ofNullable(restAPIResponse)
                .map(response -> objectMapper.convertValue(response.getData(), new TypeReference<StoreDto>() {}))
                .orElseThrow();
    }

}
