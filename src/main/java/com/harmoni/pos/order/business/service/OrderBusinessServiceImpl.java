package com.harmoni.pos.order.business.service;

import com.harmoni.pos.order.business.service.menu.MenuService;
import com.harmoni.pos.order.model.Order;
import com.harmoni.pos.order.model.dto.OrderDto;
import com.harmoni.pos.order.model.dto.StoreDto;
import com.harmoni.pos.order.service.OrderDetailService;
import com.harmoni.pos.order.service.OrderDetailSkuService;
import com.harmoni.pos.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@RequiredArgsConstructor
@Service("orderBusinessService")
public class OrderBusinessServiceImpl implements OrderBusinessService {

    private final OrderService orderService;
    private final OrderDetailService orderDetailService;
    private final OrderDetailSkuService orderDetailSkuService;
    private final MenuService menuService;

    @Override
    public StoreDto getStoreDetail(Integer id, String authHeader) {
        return menuService.getStoreDetail(id, authHeader);
    }

    @Override
    public Order calculateSubtotal(Order order) {
        BigDecimal subTotal = order.getOrderDetails().stream()
                .map(orderDetail -> Optional.ofNullable(orderDetail.getAmount()).orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Sum all amounts

        order.setSubTotal(subTotal);
        return order;
    }

    @Override
    public Order calculateServiceCharge(Order order) {
        // TODO: Implement service charge calculation if applicable
        order.setServiceChargeTotal(BigDecimal.ZERO);
        return order;
    }

    @Override
    public Order calculateDiscount(Order order, OrderDto orderDto) {
        BigDecimal discount = Optional.ofNullable(orderDto.getDiscount()).orElse(BigDecimal.ZERO);
        order.setDiscountTotal(discount);

        // Ensure grand total is recalculated
        return calculateGrandTotal(order);
    }

    @Override
    public Order calculateTax(Order order, OrderDto orderDto) {
        // TODO: Implement tax calculation logic if required
        order.setTaxTotal(BigDecimal.ZERO);
        return order;
    }

    @Override
    public Order calculateGrandTotal(Order order) {
        BigDecimal subTotal = Optional.ofNullable(order.getSubTotal()).orElse(BigDecimal.ZERO);
        BigDecimal discountTotal = Optional.ofNullable(order.getDiscountTotal()).orElse(BigDecimal.ZERO);
        BigDecimal taxTotal = Optional.ofNullable(order.getTaxTotal()).orElse(BigDecimal.ZERO);
        BigDecimal serviceCharge = Optional.ofNullable(order.getServiceChargeTotal()).orElse(BigDecimal.ZERO);

        BigDecimal grandTotal = subTotal.subtract(discountTotal).add(taxTotal).add(serviceCharge);
        order.setGrandTotal(grandTotal);
        return order;
    }

    @Override
    public Order calculate(OrderDto orderDto, String authHeader) {
        Order order = orderDto.toOrder();
        order.setStore(this.getStoreDetail(order.getStoreId(), authHeader));
        order.setCustomerId(-1);
        order.setOrderDetails(orderDetailService.populateOrderDetail(orderDto.getOrderDetailDtos(), authHeader));

        calculateSubtotal(order);
        calculateDiscount(order, orderDto);
        calculateTax(order, orderDto);
        calculateServiceCharge(order);
        calculateGrandTotal(order);

        return order;
    }
}
