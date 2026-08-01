package com.harmoni.pos.order.service;

import com.harmoni.pos.order.mapper.OrderPaymentMapper;
import com.harmoni.pos.order.model.OrderPayment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderPaymentServiceImplTest {

    @Mock
    private OrderPaymentMapper orderPaymentMapper;

    @InjectMocks
    private OrderPaymentServiceImpl orderPaymentService;

    @Test
    void deleteByPrimaryKey_shouldReturnAffectedRows() {
        when(orderPaymentMapper.deleteByPrimaryKey(1)).thenReturn(1);

        int result = orderPaymentService.deleteByPrimaryKey(1);

        assertEquals(1, result);
    }

    @Test
    void insert_shouldReturnAffectedRows() {
        var payment = new OrderPayment().setOrderId(1);
        when(orderPaymentMapper.insert(payment)).thenReturn(1);

        int result = orderPaymentService.insert(payment);

        assertEquals(1, result);
    }

    @Test
    void selectByPrimaryKey_shouldReturnPayment() {
        var expected = new OrderPayment().setId(1);
        when(orderPaymentMapper.selectByPrimaryKey(1)).thenReturn(expected);

        var actual = orderPaymentService.selectByPrimaryKey(1);

        assertEquals(expected, actual);
    }

    @Test
    void updateByPrimaryKey_shouldReturnAffectedRows() {
        var payment = new OrderPayment().setId(1);
        when(orderPaymentMapper.updateByPrimaryKey(payment)).thenReturn(1);

        int result = orderPaymentService.updateByPrimaryKey(payment);

        assertEquals(1, result);
    }
}
