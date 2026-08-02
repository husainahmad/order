package com.harmoni.pos.order.business.service.menu;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmoni.pos.order.business.service.rest.RestClientService;
import com.harmoni.pos.order.config.MenuProperties;
import com.harmoni.pos.order.http.response.RestAPIResponse;
import com.harmoni.pos.order.model.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.Serializable;
import java.util.Optional;

/**
 * Service for retrieving user details from an external user management API.
 * <p>
 * This service uses a reactive HTTP client to call the user API, and deserializes
 * the JSON response into a {@link UserDto} object.
 * </p>
 */
@RequiredArgsConstructor
@Service("userService")
public class UserService implements Serializable {

    private final RestClientService restClientService;
    private final MenuProperties menuProperties;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

    /**
     * Fetches user detail by username from the external service.
     *
     * @param username the username of the user to retrieve
     * @return the {@link UserDto} containing user details
     * @throws java.util.NoSuchElementException if the user response is null or cannot be parsed
     */
    public UserDto getUserDetail(String username) {
        String finalUrl = UriComponentsBuilder.fromHttpUrl(menuProperties.getUser().concat("/").concat(username))
                .toUriString();

        RestAPIResponse restAPIResponse = restClientService.get(username, finalUrl).block();

        return Optional.ofNullable(restAPIResponse)
                .map(response -> objectMapper.convertValue(response.getData(), new TypeReference<UserDto>() {}))
                .orElseThrow();
    }
}
