package com.harmoni.pos.order.business.service.menu;

import com.harmoni.pos.order.business.service.rest.RestClientService;
import com.harmoni.pos.order.config.MenuProperties;
import com.harmoni.pos.order.exception.BusinessBadRequestException;
import com.harmoni.pos.order.http.response.RestAPIResponse;
import com.harmoni.pos.order.model.dto.pricing.PromotionPriceRequestDto;
import com.harmoni.pos.order.model.dto.pricing.PromotionPriceResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PromotionMenuServiceTest {

    private RestClientService restClientService;
    private PromotionMenuService promotionMenuService;

    @BeforeEach
    void setUp() {
        restClientService = mock(RestClientService.class);
        MenuProperties menuProperties = new MenuProperties();
        menuProperties.setPromotionPrice("http://menu:8082/api/v1/promotion/price");
        promotionMenuService = new PromotionMenuService(restClientService, menuProperties);
    }

    @Test
    void price_shouldCallTokenlessPostWithUsernameAndMapResponse() {
        PromotionPriceRequestDto request = new PromotionPriceRequestDto();
        RestAPIResponse response = RestAPIResponse.builder().data(new PromotionPriceResponseDto()).build();

        when(restClientService.post(eq("u"), anyString(), any(Publisher.class), eq(PromotionPriceRequestDto.class)))
                .thenReturn(Mono.just(response));

        PromotionPriceResponseDto out = promotionMenuService.price(request, "u");
        assertNotNull(out);
        verify(restClientService).post(eq("u"), contains("/api/v1/promotion/price"), any(Publisher.class),
                eq(PromotionPriceRequestDto.class));
    }

    @Test
    void price_shouldThrowIfTheMenuServiceReturnsNothing() {
        PromotionPriceRequestDto request = new PromotionPriceRequestDto();
        when(restClientService.post(anyString(), anyString(), any(Publisher.class), eq(PromotionPriceRequestDto.class)))
                .thenReturn(Mono.empty());

        assertThrows(BusinessBadRequestException.class, () -> promotionMenuService.price(request, "u"));
    }

    @Test
    void price_shouldThrowWhenTheMenuRepliesWithoutAPrice() {
        PromotionPriceRequestDto request = new PromotionPriceRequestDto();
        when(restClientService.post(anyString(), anyString(), any(Publisher.class), eq(PromotionPriceRequestDto.class)))
                .thenReturn(Mono.just(RestAPIResponse.builder().build()));

        assertThrows(BusinessBadRequestException.class, () -> promotionMenuService.price(request, "u"));
    }
}
