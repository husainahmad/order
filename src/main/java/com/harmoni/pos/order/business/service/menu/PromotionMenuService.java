package com.harmoni.pos.order.business.service.menu;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmoni.pos.order.business.service.rest.RestClientService;
import com.harmoni.pos.order.config.MenuProperties;
import com.harmoni.pos.order.exception.BusinessBadRequestException;
import com.harmoni.pos.order.http.response.RestAPIResponse;
import com.harmoni.pos.order.model.dto.pricing.PromotionPriceRequestDto;
import com.harmoni.pos.order.model.dto.pricing.PromotionPriceResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.Optional;

/**
 * Client for the menu service's promotion endpoints.
 * <p>
 * The menu service owns the promotion catalogue, so it is the only place a discount can
 * be decided. The order service asks it what a basket costs and records the answer; it
 * never works out a discount itself.
 * <p>
 * Pricing happens before the order is inserted, because the grand total written into
 * the order row has to already have the promotion applied. The audit rows that record
 * which promotion produced it are written afterwards, by this service into its own
 * database, once the order line IDs exist to point them at.
 *
 * @author husainahmad
 */
@RequiredArgsConstructor
@Service("promotionMenuService")
public class PromotionMenuService implements Serializable {

    private final RestClientService restClientService;
    private final MenuProperties menuProperties;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

    /**
     * Prices a basket of sale lines against the live promotion catalogue.
     * <p>
     * A failure here fails the checkout rather than falling back to full price. That is
     * deliberate: the alternative is to confirm an order whose total silently ignores
     * every promotion, quietly overcharging the customer. An unpriceable basket is not a
     * sale that can be taken.
     *
     * @param request  the sale lines and the store, chain and zone the sale is in
     * @param username the operator the sale is being made for
     * @return the priced basket
     * @throws BusinessBadRequestException if the menu service returned no price
     */
    public PromotionPriceResponseDto price(PromotionPriceRequestDto request, String username) {
        String url = UriComponentsBuilder.fromHttpUrl(menuProperties.getPromotionPrice()).toUriString();

        RestAPIResponse restAPIResponse = restClientService.post(username, url, Mono.just(request),
                PromotionPriceRequestDto.class).block();

        return Optional.ofNullable(restAPIResponse)
                .map(response -> objectMapper.convertValue(response.getData(),
                        new TypeReference<PromotionPriceResponseDto>() {
                        }))
                .orElseThrow(() -> new BusinessBadRequestException("exception.promotionPrice.unavailable", null));
    }
}
