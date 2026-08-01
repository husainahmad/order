package com.harmoni.pos.order.service;

import com.harmoni.pos.order.mapper.OrderDetailSkuMapper;
import com.harmoni.pos.order.model.OrderDetailSku;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderDetailSkuServiceImplTest {

    @Mock
    private OrderDetailSkuMapper orderDetailSkuMapper;

    @InjectMocks
    private OrderDetailSkuServiceImpl orderDetailSkuService;

    @Test
    void deleteByPrimaryKey_shouldReturnAffectedRows() {
        when(orderDetailSkuMapper.deleteByPrimaryKey(1)).thenReturn(1);

        int result = orderDetailSkuService.deleteByPrimaryKey(1);

        assertEquals(1, result);
    }

    @Test
    void insert_shouldReturnAffectedRows() {
        var sku = new OrderDetailSku().setSkuId(1);
        when(orderDetailSkuMapper.insert(sku)).thenReturn(1);

        int result = orderDetailSkuService.insert(sku);

        assertEquals(1, result);
    }

    @Test
    void insertBulk_shouldDelegateToMapper() {
        var skus = List.of(new OrderDetailSku().setSkuId(1));

        orderDetailSkuService.insertBulk(skus);

        verify(orderDetailSkuMapper).insertBulk(skus);
    }

    @Test
    void selectByPrimaryKey_shouldReturnSku() {
        var expected = new OrderDetailSku().setId(1);
        when(orderDetailSkuMapper.selectByPrimaryKey(1)).thenReturn(expected);

        var actual = orderDetailSkuService.selectByPrimaryKey(1);

        assertEquals(expected, actual);
    }

    @Test
    void updateByPrimaryKey_shouldReturnAffectedRows() {
        var sku = new OrderDetailSku().setId(1);
        when(orderDetailSkuMapper.updateByPrimaryKey(sku)).thenReturn(1);

        int result = orderDetailSkuService.updateByPrimaryKey(sku);

        assertEquals(1, result);
    }
}
