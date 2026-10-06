package com.harmoni.pos.order.business.service.menu;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmoni.pos.order.business.service.rest.RestClientService;
import com.harmoni.pos.order.config.MenuProperties;
import com.harmoni.pos.order.exception.BusinessBadRequestException;
import com.harmoni.pos.order.http.response.RestAPIResponse;
import com.harmoni.pos.order.model.dto.ValidateOrderItemsRequestDto;
import com.harmoni.pos.order.model.dto.ValidateOrderItemsResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.Serializable;

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
     * Asks the menu service to confirm a whole basket and to name and price every line in it.
     * <p>
     * The menu service owns the catalogue. It knows which products a store sells, which SKU
     * prices apply at that store's tier, and what a customization choice costs, so it both
     * checks the basket and resolves the names and prices. This service passes on the IDs,
     * the quantities and the chosen options, and stores what comes back.
     * <p>
     * One call for the basket rather than one per line. Priced a line at a time, an order of
     * six items with options on each needs seven round trips and seven separate readings of
     * the catalogue, and a basket can straddle a rename or a re-tier between them.
     *
     * @param request  the store and the lines being ordered
     * @param username the operator the call is made on behalf of
     * @return the confirmed lines, carrying the catalogue's names and prices
     * @throws BusinessBadRequestException if the menu service could not be reached or
     *         answered with nothing, since a basket that was never checked cannot be sold
     */
    public ValidateOrderItemsResponseDto validateOrderItems(ValidateOrderItemsRequestDto request,
                                                            String username) {
        RestAPIResponse restAPIResponse = restClientService
                .post(username, menuProperties.getValidateOrderItems(), Mono.just(request),
                        ValidateOrderItemsRequestDto.class)
                .block();

        if (restAPIResponse == null || restAPIResponse.getData() == null) {
            // Deliberately not an empty answer. Silence here has to be distinguishable from
            // a genuinely empty basket: dropping a line that was never checked would sell
            // something the catalogue may no longer stock, at no price, with nothing in the
            // order to say why.
            throw new BusinessBadRequestException("exception.validateOrderItems.unavailable", null);
        }

        return objectMapper.convertValue(restAPIResponse.getData(),
                ValidateOrderItemsResponseDto.class);
    }
}