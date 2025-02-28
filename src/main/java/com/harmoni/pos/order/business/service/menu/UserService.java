package com.harmoni.pos.order.business.service.menu;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmoni.pos.order.business.service.rest.RestClientService;
import com.harmoni.pos.order.http.response.RestAPIResponse;
import com.harmoni.pos.order.model.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.Serializable;
import java.util.Optional;

@RequiredArgsConstructor
@Service("userService")
public class UserService implements Serializable {

    private final RestClientService restClientService;

    @Value("${menu.url.user}")
    private String urlUser;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

    public UserDto getUserDetail(String username, String token) {
        String finalUrl = UriComponentsBuilder.fromHttpUrl(urlUser.concat("/").concat(username))
                .toUriString();

        RestAPIResponse restAPIResponse = restClientService.get(token, finalUrl).block();

        return Optional.ofNullable(restAPIResponse)
                .map(response -> objectMapper.convertValue(response.getData(), new TypeReference<UserDto>() {}))
                .orElseThrow();
    }

}
