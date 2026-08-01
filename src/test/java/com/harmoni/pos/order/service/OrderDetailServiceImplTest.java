package com.harmoni.pos.order.service;

import com.harmoni.pos.order.business.service.menu.MenuService;
import com.harmoni.pos.order.mapper.OrderDetailMapper;
import com.harmoni.pos.order.model.OrderDetail;
import com.harmoni.pos.order.model.OrderDetailSku;
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
        var skuDto = new SkuDto()
                .setId(100)
                .setSkuTierPrice(new SkuTierPriceDto().setPrice(BigDecimal.valueOf(50)));
        var category = new CategoryDto().setId(1).setName("Beverages");
        var productDto = new ProductDto()
                .setId(1)
                .setName("Coffee")
                .setCategory(category);

        var orderDetailSkuDto = new OrderDetailSkuDto();
        orderDetailSkuDto.setSkuId(100);
        orderDetailSkuDto.setSkuName("Regular Coffee");
        orderDetailSkuDto.setQuantity(2);

        var orderDetailDto = new OrderDetailDto();
        orderDetailDto.setProductId(1);
        orderDetailDto.setOrderDetailSkus(List.of(orderDetailSkuDto));

        when(menuService.getProductWithName(List.of(1))).thenReturn(List.of(productDto));
        when(menuService.getSkusWithPrice(List.of(100))).thenReturn(List.of(skuDto));

        List<OrderDetail> result = orderDetailService.populateOrderDetail(List.of(orderDetailDto));

        assertEquals(1, result.size());
        OrderDetail detail = result.get(0);
        assertEquals(1, detail.getProductId().intValue());
        assertEquals("Coffee", detail.getProductName());
        assertEquals(1, detail.getCategoryId().intValue());
        assertEquals("Beverages", detail.getCategoryName());
        assertEquals(BigDecimal.valueOf(100), detail.getAmount());

        assertEquals(1, detail.getOrderDetailSkus().size());
        OrderDetailSku resultSku = detail.getOrderDetailSkus().get(0);
        assertEquals(100, resultSku.getSkuId().intValue());
        assertEquals(BigDecimal.valueOf(50), resultSku.getPrice());
        assertEquals(BigDecimal.valueOf(100), resultSku.getAmount());
    }

    @Test
    void getDetailSkuWithPrice_shouldCollectSkuIdsAndCallMenuService() {
        var orderDetailSkuDto = new OrderDetailSkuDto();
        orderDetailSkuDto.setSkuId(1);
        var orderDetailDto = new OrderDetailDto();
        orderDetailDto.setProductId(10);
        orderDetailDto.setOrderDetailSkus(List.of(orderDetailSkuDto));

        orderDetailService.getDetailSkuWithPrice(List.of(orderDetailDto));

        verify(menuService).getSkusWithPrice(List.of(1));
    }

    @Test
    void getProductWithName_shouldCollectProductIdsAndCallMenuService() {
        var orderDetailDto = new OrderDetailDto();
        orderDetailDto.setProductId(10);

        orderDetailService.getProductWithName(List.of(orderDetailDto));

        verify(menuService).getProductWithName(List.of(10));
    }
}
