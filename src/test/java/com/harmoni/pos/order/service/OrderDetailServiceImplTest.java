package com.harmoni.pos.order.service;

import com.harmoni.pos.order.business.service.menu.MenuService;
import com.harmoni.pos.order.mapper.OrderDetailMapper;
import com.harmoni.pos.order.model.OrderDetail;
import com.harmoni.pos.order.model.OrderDetailCustomization;
import com.harmoni.pos.order.model.OrderDetailCustomizationOption;
import com.harmoni.pos.order.model.OrderDetailSku;
import com.harmoni.pos.order.exception.BusinessBadRequestException;
import com.harmoni.pos.order.model.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderDetailServiceImplTest {

    @Mock
    private OrderDetailMapper orderDetailMapper;

    @Mock
    private MenuService menuService;

    @Mock
    private OrderDetailSkuService orderDetailSkuService;

    @Mock
    private OrderDetailCustomizationService orderDetailCustomizationService;

    @InjectMocks
    private OrderDetailServiceImpl orderDetailService;

    @Test
    void deleteByPrimaryKey_shouldReturnAffectedRows() {
        when(orderDetailMapper.deleteByPrimaryKey(1)).thenReturn(1);

        int result = orderDetailService.deleteByPrimaryKey(1);

        assertEquals(1, result);
    }

    @Test
    void insert_shouldReturnAffectedRows() {
        var detail = new OrderDetail().setProductId(1);
        when(orderDetailMapper.insert(detail)).thenReturn(1);

        int result = orderDetailService.insert(detail);

        assertEquals(1, result);
    }

    @Test
    void insertBulk_shouldInsertDetailsAndAssociatedSkus() {
        var sku = new OrderDetailSku().setSkuId(1);
        var detail = new OrderDetail().setId(1).setOrderDetailSkus(List.of(sku));
        var details = List.of(detail);

        orderDetailService.insertBulk(details);

        verify(orderDetailMapper).insertBulk(details);
        assertEquals(1, sku.getOrderDetailId());
        assertNotNull(sku.getCreatedAt());
        verify(orderDetailSkuService).insertBulk(List.of(sku));
        verify(orderDetailCustomizationService).record(List.of());
    }

    @Test
    void insertBulk_shouldRecordCustomizationsAgainstTheDetailId() {
        var option = new OrderDetailCustomizationOption()
                .setCustomizationOptionsId(101)
                .setCustomizationOptionName("Large")
                .setPrice(new BigDecimal("1.50"))
                .setAmount(new BigDecimal("3.00"));
        var customization = new OrderDetailCustomization()
                .setCustomizationId(1)
                .setCustomizationName("Size")
                .setOptions(List.of(option));
        var detail = new OrderDetail().setId(1).setOrderDetailSkus(List.of(new OrderDetailSku().setSkuId(1))).setCustomizations(List.of(customization));

        orderDetailService.insertBulk(List.of(detail));

        ArgumentCaptor<List<OrderDetailCustomization>> captor = ArgumentCaptor.forClass(List.class);
        verify(orderDetailCustomizationService).record(captor.capture());
        List<OrderDetailCustomization> recorded = captor.getValue();
        assertEquals(1, recorded.size());
        assertEquals(1, recorded.get(0).getOrderDetailId().intValue());
        assertEquals(1, recorded.get(0).getCustomizationId().intValue());
        assertEquals("Size", recorded.get(0).getCustomizationName());
        assertNotNull(recorded.get(0).getCreatedAt());
        assertEquals(1, recorded.get(0).getOptions().size());
        assertEquals(101, recorded.get(0).getOptions().get(0).getCustomizationOptionsId().intValue());
    }

    @Test
    void insertBulk_shouldNotWriteRowsForLinesWithNoChoices() {
        var sku = new OrderDetailSku().setSkuId(1);
        var detail = new OrderDetail().setId(1).setOrderDetailSkus(List.of(sku));

        orderDetailService.insertBulk(List.of(detail));

        verify(orderDetailCustomizationService).record(List.of());
    }

    @Test
    void selectByPrimaryKey_shouldReturnDetail() {
        var expected = new OrderDetail().setId(1);
        when(orderDetailMapper.selectByPrimaryKey(1)).thenReturn(expected);

        var actual = orderDetailService.selectByPrimaryKey(1);

        assertEquals(expected, actual);
    }

    @Test
    void updateByPrimaryKey_shouldReturnAffectedRows() {
        var detail = new OrderDetail().setId(1);
        when(orderDetailMapper.updateByPrimaryKey(detail)).thenReturn(1);

        int result = orderDetailService.updateByPrimaryKey(detail);

        assertEquals(1, result);
    }


    @Test
    void populateOrderDetail_shouldMapProductsAndSkus() {
        givenConfirmedBasket(List.of(confirmedLine(1, 100, "Coffee", "Regular Coffee", "50", 2, List.of())));

        List<OrderDetail> result = orderDetailService.populateOrderDetail(List.of(basketWith(1, 100, 2, List.of())),
                7, "test-user");

        assertEquals(1, result.size());
        OrderDetail detail = result.get(0);
        assertEquals(1, detail.getProductId().intValue());
        assertEquals("Coffee", detail.getProductName());
        assertEquals(3, detail.getCategoryId().intValue());
        assertEquals("Beverages", detail.getCategoryName());
        assertEquals(BigDecimal.valueOf(100), detail.getAmount());

        assertEquals(1, detail.getOrderDetailSkus().size());
        OrderDetailSku resultSku = detail.getOrderDetailSkus().get(0);
        assertEquals(100, resultSku.getSkuId().intValue());
        assertEquals(BigDecimal.valueOf(50), resultSku.getPrice());
        assertEquals(BigDecimal.valueOf(100), resultSku.getAmount());
    }

    /**
     * The line's name is what the receipt prints, so it has to be the catalogue's. It used to
     * come off the request, which let a client label a line anything it liked and have it
     * stored, and printed, as though the menu had said so.
     */
    @Test
    void populateOrderDetail_shouldNameTheLineFromTheCatalogueNotTheRequest() {
        givenConfirmedBasket(List.of(confirmedLine(1, 100, "Coffee", "Regular Coffee", "50", 2, List.of())));

        List<OrderDetail> result =
                orderDetailService.populateOrderDetail(List.of(basketWith(1, 100, 2, List.of())), 7, "test-user");

        assertEquals("Regular Coffee", result.get(0).getOrderDetailSkus().get(0).getSkuName());
    }

    /**
     * The whole basket goes out in one request. Priced a line at a time, an order of six items
     * with options on each needs seven round trips and seven separate readings of the
     * catalogue, and a basket can straddle a rename or a re-tier between them.
     */
    @Test
    void populateOrderDetail_shouldSendTheWholeBasketWithTheStoreInOneRequest() {
        givenConfirmedBasket(List.of(
                confirmedLine(1, 100, "Coffee", "Regular Coffee", "50", 3, List.of()),
                confirmedLine(2, 200, "Tea", "Large Tea", "30", 1, List.of())));

        orderDetailService.populateOrderDetail(
                List.of(basketWith(1, 100, 3, List.of(choice(101), choice(102, 2))),
                        basketWith(2, 200, 1, List.of())), 7, "test-user");

        ArgumentCaptor<ValidateOrderItemsRequestDto> captor =
                ArgumentCaptor.forClass(ValidateOrderItemsRequestDto.class);
        verify(menuService).validateOrderItems(captor.capture(), eq("test-user"));
        assertEquals(7, captor.getValue().getStoreId().intValue());
        assertEquals(2, captor.getValue().getItems().size());

        ValidateOrderItemRequestDto first = captor.getValue().getItems().get(0);
        assertEquals(1, first.getProductId().intValue());
        assertEquals(100, first.getSkuId().intValue());
        assertEquals(3, first.getQuantity().intValue());
        List<CustomizationGroupSelectionDto> sent = first.getCustomizations();
        assertEquals(2, sent.size());
        assertEquals(1, sent.get(0).getCustomizationId().intValue());
        assertEquals(101, sent.get(0).getOptions().get(0).getCustomizationOptionId().intValue());
        assertEquals(1, sent.get(0).getOptions().get(0).getQuantity().intValue());
        assertEquals(1, sent.get(1).getCustomizationId().intValue());
        assertEquals(102, sent.get(1).getOptions().get(0).getCustomizationOptionId().intValue());
        assertEquals(2, sent.get(1).getOptions().get(0).getQuantity().intValue());

        ValidateOrderItemRequestDto second = captor.getValue().getItems().get(1);
        assertEquals(2, second.getProductId().intValue());
        assertEquals(200, second.getSkuId().intValue());
        assertTrue(second.getCustomizations().isEmpty());
    }

    /**
     * A product ordered as two SKUs is one line on the order, not two, so the confirmed
     * answers for both come back as one product line carrying both SKUs and one total.
     */
    @Test
    void populateOrderDetail_shouldGroupSeveralSkusOfOneProductUnderOneLine() {
        givenConfirmedBasket(List.of(
                confirmedLine(1, 100, "Coffee", "Regular Coffee", "50", 2, List.of()),
                confirmedLine(1, 101, "Coffee", "Large Coffee", "70", 1, List.of())));

        List<OrderDetail> result = orderDetailService.populateOrderDetail(
                List.of(basketWith(1, List.of(100, 101), List.of(2, 1), List.of(List.of(), List.of()))), 7, "test-user");

        assertEquals(1, result.size());
        OrderDetail detail = result.get(0);
        assertEquals(2, detail.getOrderDetailSkus().size());
        assertAmountEquals("170.00", detail.getAmount());
    }

    /**
     * The same product and SKU can legitimately be ordered twice, as two lines or two SKUs
     * of one line. Answers are paired off position by position, so each request line keeps
     * its own.
     */
    @Test
    void populateOrderDetail_shouldKeepRepeatedLinesApart() {
        givenConfirmedBasket(List.of(
                confirmedLine(1, 100, "Coffee", "Regular Coffee", "50", 1, List.of()),
                confirmedLine(1, 100, "Coffee", "Regular Coffee", "50", 3, List.of())));

        List<OrderDetail> result = orderDetailService.populateOrderDetail(
                List.of(basketWith(1, List.of(100, 100), List.of(1, 3), List.of(List.of(), List.of()))), 7, "test-user");

        OrderDetail detail = result.get(0);
        assertEquals(2, detail.getOrderDetailSkus().size());
        assertEquals(1, detail.getOrderDetailSkus().get(0).getQuantity());
        assertEquals(3, detail.getOrderDetailSkus().get(1).getQuantity());
        assertAmountEquals("200.00", detail.getAmount());
    }

    @Test
    void populateOrderDetail_shouldAddTheCustomizationSurchargeToTheDetailAmount() {
        givenConfirmedBasket(List.of(confirmedLine(1, 100, "Coffee", "Regular Coffee", "50", 2,
                List.of(confirmedChoice(101, "Large", "1.50", 3)))));

        List<OrderDetail> result =
                orderDetailService.populateOrderDetail(List.of(basketWith(1, 100, 2, List.of(choice(101, 3)))),
                        7, "test-user");

        // Surcharge is now on OrderDetail level, not SKU
        assertAmountEquals("109.00", result.get(0).getAmount());
        assertAmountEquals("100.00", result.get(0).getOrderDetailSkus().get(0).getAmount());
    }

    @Test
    void populateOrderDetail_shouldKeepTheSkuPriceSeparateFromTheSurcharge() {
        givenConfirmedBasket(List.of(confirmedLine(1, 100, "Coffee", "Regular Coffee", "50", 2,
                List.of(confirmedChoice(101, "Large", "1.50", 1)))));

        List<OrderDetail> result =
                orderDetailService.populateOrderDetail(List.of(basketWith(1, 100, 2, List.of(choice(101)))),
                        7, "test-user");

        OrderDetailSku sku = result.get(0).getOrderDetailSkus().get(0);
        assertAmountEquals("50.00", sku.getPrice());
        assertAmountEquals("100.00", sku.getAmount());
        assertAmountEquals("103.00", result.get(0).getAmount());
    }

    @Test
    void populateOrderDetail_shouldRecordThePricedChoicesOnTheDetail() {
        givenConfirmedBasket(List.of(confirmedLine(1, 100, "Coffee", "Regular Coffee", "50", 2,
                List.of(confirmedChoice(101, "Large", "1.50", 1),
                        confirmedChoice(102, "Oat Milk", "0.80", 2)))));

        List<OrderDetail> result =
                orderDetailService.populateOrderDetail(List.of(basketWith(1, 100, 2, choices(101, 102))),
                        7, "test-user");

        List<OrderDetailCustomization> recorded = result.get(0).getCustomizations();
        assertNotNull(recorded);
        assertEquals(1, recorded.size()); // grouped by customizationId
        OrderDetailCustomization group = recorded.get(0);
        assertEquals(1, group.getCustomizationId().intValue());
        assertEquals("Size", group.getCustomizationName());
        assertEquals(2, group.getOptions().size());
        assertEquals(101, group.getOptions().get(0).getCustomizationOptionsId().intValue());
        assertEquals("Large", group.getOptions().get(0).getCustomizationOptionName());
        assertEquals(102, group.getOptions().get(1).getCustomizationOptionsId().intValue());
        assertEquals("Oat Milk", group.getOptions().get(1).getCustomizationOptionName());
        assertAmountEquals("3.20", group.getOptions().get(1).getAmount());
    }

    /**
     * A choice's charge is its per unit price times how many were chosen times how many units
     * of the line were ordered. The menu service prices the parts and deliberately does not
     * total them: it does not know the line quantity, and it is not the side that applies
     * promotions afterwards. Two coffees with oat milk asked for twice is therefore 0.80 x 2 x
     * 2, not 0.80 x 2.
     */
    @Test
    void populateOrderDetail_shouldChargeAChoiceForEveryUnitOnTheLine() {
        givenConfirmedBasket(List.of(confirmedLine(1, 100, "Coffee", "Regular Coffee", "50", 2,
                List.of(confirmedChoice(102, "Oat Milk", "0.80", 2)))));

        List<OrderDetail> result =
                orderDetailService.populateOrderDetail(List.of(basketWith(1, 100, 2, choices(102, 102))),
                        7, "test-user");

        List<OrderDetailCustomization> recorded = result.get(0).getCustomizations();
        assertNotNull(recorded);
        assertEquals(1, recorded.size());
        assertEquals(1, recorded.get(0).getOptions().size());
        OrderDetailCustomizationOption opt = recorded.get(0).getOptions().get(0);
        assertAmountEquals("0.80", opt.getPrice());
        assertEquals(2, opt.getQuantity().intValue());
        assertAmountEquals("3.20", opt.getAmount());
    }

    /**
     * A choice with no count stated means one. The default lives on the DTO rather than here
     * so it also applies to a request built by hand, and so the menu service is the one that
     * decides whether a count breaks the customization's limit.
     */
    @Test
    void aChoiceThatStatesNoCountShouldBeSentAsOne() {
        assertEquals(1, new CustomizationOptionSelectionDto().getQuantity());
    }

    @Test
    void populateOrderDetail_shouldSellAPlainLineAtTheCataloguePrice() {
        givenConfirmedBasket(List.of(confirmedLine(1, 100, "Coffee", "Regular Coffee", "50", 2, List.of())));

        List<OrderDetail> result =
                orderDetailService.populateOrderDetail(List.of(basketWith(1, 100, 2, List.of())), 7, "test-user");

        OrderDetailSku sku = result.get(0).getOrderDetailSkus().get(0);
        assertAmountEquals("100.00", sku.getAmount());
        assertAmountEquals("100.00", result.get(0).getAmount());
        assertNull(result.get(0).getCustomizations()); // no customizations field on detail
        verify(menuService, times(1)).validateOrderItems(any(), anyString());
    }

    @Test
    void populateOrderDetail_shouldSurfaceTheMenuServiceRejectionOfInvalidChoices() {
        when(menuService.validateOrderItems(any(), eq("test-user")))
                .thenThrow(new BusinessBadRequestException(
                        "exception.orderValidation.requiredCustomizationMissing", new Object[]{"Size"}));

        BusinessBadRequestException thrown = assertThrows(BusinessBadRequestException.class,
                () -> orderDetailService.populateOrderDetail(
                        List.of(basketWith(1, 100, 1, choices(101))), 7, "test-user"));

        assertEquals("exception.orderValidation.requiredCustomizationMissing", thrown.getMessage());
    }

    /**
     * Answers are paired to requests by position, so a short or reordered answer would be
     * recorded against the wrong line, and on a receipt that means the wrong price next to
     * the wrong name. Refusing the whole basket is the only safe reading.
     */
    @Test
    void populateOrderDetail_shouldRefuseAnAnswerThatIsNotTheOneAskedFor() {
        givenConfirmedBasket(List.of(confirmedLine(1, 100, "Coffee", "Regular Coffee", "50", 2, List.of())));

        BusinessBadRequestException thrown = assertThrows(BusinessBadRequestException.class,
                () -> orderDetailService.populateOrderDetail(
                        List.of(basketWith(1, 100, 2, List.of()), basketWith(2, 200, 1, List.of())), 7, "test-user"));

        assertEquals("exception.validateOrderItems.unavailable", thrown.getMessage());
    }

    @Test
    void populateOrderDetail_shouldRefuseAnAnswerThatNamesAnotherProduct() {
        givenConfirmedBasket(List.of(confirmedLine(2, 200, "Tea", "Large Tea", "30", 1, List.of())));

        BusinessBadRequestException thrown = assertThrows(BusinessBadRequestException.class,
                () -> orderDetailService.populateOrderDetail(List.of(basketWith(1, 100, 2, List.of())), 7, "test-user"));

        assertEquals("exception.validateOrderItems.unavailable", thrown.getMessage());
    }

    @Test
    void populateOrderDetail_shouldRefuseAnUnconfirmedAnswer() {
        ValidateOrderItemsResponseDto response = new ValidateOrderItemsResponseDto();
        response.setValid(false);
        response.setItems(List.of(confirmedLine(1, 100, "Coffee", "Regular Coffee", "50", 2, List.of())));
        when(menuService.validateOrderItems(any(), eq("test-user"))).thenReturn(response);

        BusinessBadRequestException thrown = assertThrows(BusinessBadRequestException.class,
                () -> orderDetailService.populateOrderDetail(List.of(basketWith(1, 100, 2, List.of())), 7, "test-user"));

        assertEquals("exception.validateOrderItems.unavailable", thrown.getMessage());
    }

    @Test
    void populateOrderDetail_shouldNotAskTheMenuAboutAnEmptyBasket() {
        List<OrderDetail> result = orderDetailService.populateOrderDetail(List.of(), 7, "test-user");

        assertTrue(result.isEmpty());
        verify(menuService, never()).validateOrderItems(any(), anyString());
    }

    // ----------------------------------------------------------------------- fixtures

    private static OrderDetailDto basketWith(int productId, int skuId, int quantity,
                                             List<CustomizationGroupSelectionDto> customizations) {
        var orderDetailSkuDto = new OrderDetailSkuDto();
        orderDetailSkuDto.setSkuId(skuId);
        orderDetailSkuDto.setQuantity(quantity);
        orderDetailSkuDto.setCustomizations(customizations);

        var orderDetailDto = new OrderDetailDto();
        orderDetailDto.setProductId(productId);
        orderDetailDto.setOrderDetailSkus(List.of(orderDetailSkuDto));
        return orderDetailDto;
    }

    private static OrderDetailDto basketWith(int productId, List<Integer> skuIds, List<Integer> quantities,
                                             List<List<CustomizationGroupSelectionDto>> customizations) {
        List<OrderDetailSkuDto> skus = new java.util.ArrayList<>();
        for (int index = 0; index < skuIds.size(); index++) {
            var sku = new OrderDetailSkuDto();
            sku.setSkuId(skuIds.get(index));
            sku.setQuantity(quantities.get(index));
            sku.setCustomizations(customizations.get(index));
            skus.add(sku);
        }
        var orderDetailDto = new OrderDetailDto();
        orderDetailDto.setProductId(productId);
        orderDetailDto.setOrderDetailSkus(skus);
        return orderDetailDto;
    }

    private static CustomizationOptionSelectionDto option(Integer optionId, int quantity) {
        var selection = new CustomizationOptionSelectionDto();
        selection.setCustomizationOptionId(optionId);
        selection.setQuantity(quantity);
        return selection;
    }

    private static CustomizationGroupSelectionDto group(Integer customizationId,
                                                        CustomizationOptionSelectionDto... options) {
        var group = new CustomizationGroupSelectionDto();
        group.setCustomizationId(customizationId);
        group.setOptions(java.util.Arrays.stream(options).toList());
        return group;
    }

    private static CustomizationGroupSelectionDto choice(Integer optionId) {
        return choice(optionId, 1);
    }

    private static CustomizationGroupSelectionDto choice(Integer optionId, int quantity) {
        return group(1, option(optionId, quantity));
    }

    private static List<CustomizationGroupSelectionDto> choices(Integer... optionIds) {
        return List.of(group(1, java.util.Arrays.stream(optionIds)
                .map(optionId -> option(optionId, 1)).toArray(CustomizationOptionSelectionDto[]::new)));
    }

    private static ValidatedCustomizationDto confirmedChoice(Integer optionId, String name, String price,
                                                             int quantity) {
        var choice = new ValidatedCustomizationDto();
        choice.setCustomizationOptionId(optionId);
        choice.setCustomizationId(1);
        choice.setCustomizationName("Size");
        choice.setOptionName(name);
        choice.setPrice(new BigDecimal(price));
        choice.setQuantity(quantity);
        return choice;
    }

    private static ValidatedOrderItemDto confirmedLine(int productId, int skuId, String productName, String skuName,
                                                       String skuPrice, int quantity,
                                                       List<ValidatedCustomizationDto> customizations) {
        var line = new ValidatedOrderItemDto();
        line.setProductId(productId);
        line.setProductName(productName);
        line.setCategoryId(3);
        line.setCategoryName("Beverages");
        line.setSkuId(skuId);
        line.setSkuName(skuName);
        line.setSkuPrice(new BigDecimal(skuPrice));
        line.setQuantity(quantity);
        line.setCustomizations(customizations);
        return line;
    }

    private void givenConfirmedBasket(List<ValidatedOrderItemDto> lines) {
        var response = new ValidateOrderItemsResponseDto();
        response.setValid(true);
        response.setItems(lines);
        when(menuService.validateOrderItems(any(), eq("test-user"))).thenReturn(response);
    }

    /**
     * Compares two amounts by value. BigDecimal's own equals treats 50 and 50.00 as
     * different, which would make these tests assert on scale rather than on money.
     */
    private static void assertAmountEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual),
                "expected " + expected + " but was " + actual);
    }
}
