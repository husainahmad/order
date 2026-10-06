package com.harmoni.pos.order.business.service.menu;

import com.harmoni.pos.order.business.service.rest.RestClientService;
import com.harmoni.pos.order.config.MenuProperties;
import com.harmoni.pos.order.exception.BusinessBadRequestException;
import com.harmoni.pos.order.http.response.RestAPIResponse;
import com.harmoni.pos.order.model.dto.CustomizationGroupSelectionDto;
import com.harmoni.pos.order.model.dto.CustomizationOptionSelectionDto;
import com.harmoni.pos.order.model.dto.ValidateOrderItemRequestDto;
import com.harmoni.pos.order.model.dto.ValidateOrderItemsRequestDto;
import com.harmoni.pos.order.model.dto.ValidateOrderItemsResponseDto;
import com.harmoni.pos.order.model.dto.ValidatedCustomizationDto;
import com.harmoni.pos.order.model.dto.ValidatedOrderItemDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MenuServiceValidationTest {

    private RestClientService restClientService;
    private MenuService menuService;

    @BeforeEach
    void setUp() {
        restClientService = mock(RestClientService.class);
        MenuProperties menuProperties = new MenuProperties();
        menuProperties.setValidateOrderItems("http://menu:8082/internal/v1/menu/validate-order-items");
        menuService = new MenuService(restClientService, menuProperties);
    }

    private static ValidateOrderItemsRequestDto request() {
        ValidateOrderItemRequestDto item = new ValidateOrderItemRequestDto();
        item.setProductId(10);
        item.setSkuId(20);
        item.setQuantity(2);
        CustomizationOptionSelectionDto extraShotOption = new CustomizationOptionSelectionDto();
        extraShotOption.setCustomizationOptionId(101);
        extraShotOption.setQuantity(3);
        CustomizationGroupSelectionDto extraShots = new CustomizationGroupSelectionDto();
        extraShots.setCustomizationId(5);
        extraShots.setOptions(List.of(extraShotOption));
        item.setCustomizations(List.of(extraShots));

        ValidateOrderItemsRequestDto request = new ValidateOrderItemsRequestDto();
        request.setStoreId(1);
        request.setItems(List.of(item));
        return request;
    }

    private static ValidatedCustomizationDto confirmedChoice() {
        ValidatedCustomizationDto choice = new ValidatedCustomizationDto();
        choice.setCustomizationOptionId(101);
        choice.setCustomizationId(1);
        choice.setCustomizationName("Size");
        choice.setOptionName("Large");
        choice.setPrice(new BigDecimal("1.50"));
        choice.setQuantity(3);
        return choice;
    }

    private static ValidatedOrderItemDto confirmedLine() {
        ValidatedOrderItemDto line = new ValidatedOrderItemDto();
        line.setProductId(10);
        line.setProductName("Kopi Gula Aren");
        line.setCategoryId(5);
        line.setCategoryName("Kopi");
        line.setSkuId(20);
        line.setSkuName("Large");
        line.setSkuPrice(new BigDecimal("20000"));
        line.setQuantity(2);
        line.setCustomizations(List.of(confirmedChoice()));
        return line;
    }

    @Test
    void validateOrderItems_shouldCallTheValidationEndpointAsTheOperatorAndMapTheAnswer() {
        ValidateOrderItemsResponseDto expected = new ValidateOrderItemsResponseDto();
        expected.setValid(true);
        expected.setItems(List.of(confirmedLine()));
        when(restClientService.post(eq("u"), anyString(), any(Publisher.class),
                eq(ValidateOrderItemsRequestDto.class)))
                .thenReturn(Mono.just(RestAPIResponse.builder().data(expected).build()));

        ValidateOrderItemsResponseDto out = menuService.validateOrderItems(request(), "u");

        assertTrue(out.isValid());
        assertEquals(1, out.getItems().size());
        ValidatedOrderItemDto line = out.getItems().get(0);
        assertEquals("Kopi Gula Aren", line.getProductName());
        assertEquals(0, new BigDecimal("20000").compareTo(line.getSkuPrice()));
        assertEquals("Large", line.getCustomizations().get(0).getOptionName());
        verify(restClientService).post(eq("u"), contains("/internal/v1/menu/validate-order-items"),
                any(Publisher.class), eq(ValidateOrderItemsRequestDto.class));
    }

    /**
     * The answer arrives inside the envelope's data field as untyped JSON, the same way every
     * other menu call's does. This is the one place that could quietly turn every price into
     * a null, so it is checked rather than assumed.
     */
    @Test
    void validateOrderItems_shouldReadTheAnswerOutOfTheEnvelopesUntypedData() {
        Map<String, Object> data = Map.of(
                "valid", true,
                "items", List.of(Map.of(
                        "productId", 10,
                        "productName", "Kopi Gula Aren",
                        "categoryId", 5,
                        "categoryName", "Kopi",
                        "skuId", 20,
                        "skuName", "Large",
                        "skuPrice", 20000,
                        "quantity", 2,
                        "customizations", List.of(Map.of(
                                "customizationOptionId", 101,
                                "customizationId", 1,
                                "customizationName", "Size",
                                "optionName", "Large",
                                "price", 1500,
                                "quantity", 3)))));
        when(restClientService.post(anyString(), anyString(), any(Publisher.class),
                eq(ValidateOrderItemsRequestDto.class)))
                .thenReturn(Mono.just(RestAPIResponse.builder().data(data).build()));

        ValidateOrderItemsResponseDto out = menuService.validateOrderItems(request(), "u");

        ValidatedOrderItemDto line = out.getItems().get(0);
        assertEquals(0, new BigDecimal("20000").compareTo(line.getSkuPrice()));
        assertEquals(0, new BigDecimal("1500").compareTo(line.getCustomizations().get(0).getPrice()));
        assertEquals(2, line.getQuantity());
        assertEquals(3, line.getCustomizations().get(0).getQuantity());
    }

    @Test
    void validateOrderItems_shouldSendTheStoreSoThePricesAreTheOnesForThisStore() {
        ValidateOrderItemsResponseDto expected = new ValidateOrderItemsResponseDto();
        expected.setValid(true);
        expected.setItems(List.of(confirmedLine()));
        ArgumentCaptor<Publisher<?>> captor = ArgumentCaptor.forClass(Publisher.class);
        when(restClientService.post(anyString(), anyString(), captor.capture(),
                eq(ValidateOrderItemsRequestDto.class)))
                .thenReturn(Mono.just(RestAPIResponse.builder().data(expected).build()));

        menuService.validateOrderItems(request(), "u");

        ValidateOrderItemsRequestDto sent = (ValidateOrderItemsRequestDto) ((Mono<?>) captor.getValue()).block();
        assertEquals(1, sent.getStoreId().intValue());
        assertEquals(10, sent.getItems().get(0).getProductId().intValue());
        assertEquals(5, sent.getItems().get(0).getCustomizations().get(0).getCustomizationId().intValue());
        assertEquals(101, sent.getItems().get(0).getCustomizations().get(0).getOptions().get(0).getCustomizationOptionId().intValue());
        assertEquals(3, sent.getItems().get(0).getCustomizations().get(0).getOptions().get(0).getQuantity().intValue());
    }

    @Test
    void validateOrderItems_shouldThrowRatherThanTreatAnUnreachableMenuAsAnEmptyBasket() {
        when(restClientService.post(anyString(), anyString(), any(Publisher.class),
                eq(ValidateOrderItemsRequestDto.class)))
                .thenReturn(Mono.empty());

        BusinessBadRequestException thrown = assertThrows(BusinessBadRequestException.class,
                () -> menuService.validateOrderItems(request(), "u"));

        assertEquals("exception.validateOrderItems.unavailable", thrown.getMessage());
    }

    @Test
    void validateOrderItems_shouldThrowWhenTheMenuRepliesWithNoData() {
        when(restClientService.post(anyString(), anyString(), any(Publisher.class),
                eq(ValidateOrderItemsRequestDto.class)))
                .thenReturn(Mono.just(RestAPIResponse.builder().build()));

        BusinessBadRequestException thrown = assertThrows(BusinessBadRequestException.class,
                () -> menuService.validateOrderItems(request(), "u"));

        assertEquals("exception.validateOrderItems.unavailable", thrown.getMessage());
    }
}