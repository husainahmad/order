package com.harmoni.pos.order.business.service;

import com.harmoni.pos.order.business.service.menu.UserService;
import com.harmoni.pos.order.business.service.promotion.OrderPromotionService;
import com.harmoni.pos.order.component.CallerScope;
import com.harmoni.pos.order.exception.BusinessBadRequestException;
import com.harmoni.pos.order.exception.ScopeAccessDeniedException;
import com.harmoni.pos.order.model.Order;
import com.harmoni.pos.order.model.OrderPayment;
import com.harmoni.pos.order.model.OrderStatus;
import com.harmoni.pos.order.service.OrderCustomerService;
import com.harmoni.pos.order.service.OrderDetailService;
import com.harmoni.pos.order.service.OrderItemDiscountService;
import com.harmoni.pos.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderBusinessServiceImplOrderDetailTest {

    @Mock
    private OrderService orderService;

    @Mock
    private OrderDetailService orderDetailService;

    @Mock
    private UserService userService;

    @Mock
    private OrderPromotionService orderPromotionService;

    @Mock
    private OrderItemDiscountService orderItemDiscountService;

    @Mock
    private OrderCustomerService orderCustomerService;

    @InjectMocks
    private OrderBusinessServiceImpl orderBusinessService;

    @Test
    void getOrderById_shouldReturnOrderWithPayment() {
        Order order = new Order().setId(7).setStoreId(5).setOrderNo("ORD5-001")
                .setStatus(OrderStatus.CONFIRMED)
                .setOrderPayment(new OrderPayment().setId(3).setAmount(new BigDecimal("5.00")));
        when(orderService.selectByIdWithPayment(7)).thenReturn(order);

        Order result = orderBusinessService.getOrderById(7, CallerScope.global());

        assertSame(order, result);
        assertNotNull(result.getOrderPayment());
        verify(orderService).selectByIdWithPayment(7);
    }

    @Test
    void getOrderById_shouldAllowStoreInScope() {
        when(orderService.selectByIdWithPayment(7)).thenReturn(new Order().setId(7).setStoreId(5));

        Order result = orderBusinessService.getOrderById(7, CallerScope.of(Set.of(5, 6)));

        assertEquals(7, result.getId());
    }

    @Test
    void getOrderById_shouldDenyStoreOutsideScope() {
        when(orderService.selectByIdWithPayment(7)).thenReturn(new Order().setId(7).setStoreId(9));

        assertThrows(ScopeAccessDeniedException.class,
                () -> orderBusinessService.getOrderById(7, CallerScope.of(Set.of(5))));
    }

    @Test
    void getOrderById_shouldDenyNullStoreForScopedCaller() {
        when(orderService.selectByIdWithPayment(7)).thenReturn(new Order().setId(7));

        assertThrows(ScopeAccessDeniedException.class,
                () -> orderBusinessService.getOrderById(7, CallerScope.of(Set.of(5))));
    }

    @Test
    void getOrderById_shouldRejectNonPositiveId() {
        assertThrows(BusinessBadRequestException.class,
                () -> orderBusinessService.getOrderById(0, CallerScope.global()));
        assertThrows(BusinessBadRequestException.class,
                () -> orderBusinessService.getOrderById(null, CallerScope.global()));
        verifyNoInteractions(orderService);
    }
}
