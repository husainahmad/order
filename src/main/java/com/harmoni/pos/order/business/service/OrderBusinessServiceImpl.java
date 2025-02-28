package com.harmoni.pos.order.business.service;

import com.harmoni.pos.order.business.service.menu.UserService;
import com.harmoni.pos.order.component.JwtUtil;
import com.harmoni.pos.order.model.Order;
import com.harmoni.pos.order.model.OrderStatus;
import com.harmoni.pos.order.model.dto.OrderDto;
import com.harmoni.pos.order.model.dto.UserDto;
import com.harmoni.pos.order.service.OrderDetailService;
import com.harmoni.pos.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static com.harmoni.pos.order.utils.DateTimeUtils.getDailyTimeRange;

@RequiredArgsConstructor
@Service("orderBusinessService")
public class OrderBusinessServiceImpl implements OrderBusinessService {

    private final OrderService orderService;
    private final OrderDetailService orderDetailService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Override
    public void calculateSubtotal(Order order) {
        BigDecimal subTotal = order.getOrderDetails().stream()
                .map(orderDetail -> Optional.ofNullable(orderDetail.getAmount()).orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Sum all amounts
        order.setSubTotal(subTotal);
    }

    @Override
    public void calculateServiceCharge(Order order) {
        // TODO: Implement service charge calculation if applicable
        order.setServiceChargeTotal(BigDecimal.ZERO);
    }

    @Override
    public void calculateDiscount(Order order, OrderDto orderDto) {
        BigDecimal discount = Optional.ofNullable(orderDto.getDiscount()).orElse(BigDecimal.ZERO);
        order.setDiscountTotal(discount);
        calculateGrandTotal(order);
    }

    @Override
    public void calculateTax(Order order, OrderDto orderDto) {
        // TODO: Implement tax calculation logic if required
        order.setTaxTotal(BigDecimal.ZERO);
    }

    @Override
    public void calculateGrandTotal(Order order) {
        BigDecimal subTotal = Optional.ofNullable(order.getSubTotal()).orElse(BigDecimal.ZERO);
        BigDecimal discountTotal = Optional.ofNullable(order.getDiscountTotal()).orElse(BigDecimal.ZERO);
        BigDecimal taxTotal = Optional.ofNullable(order.getTaxTotal()).orElse(BigDecimal.ZERO);
        BigDecimal serviceCharge = Optional.ofNullable(order.getServiceChargeTotal()).orElse(BigDecimal.ZERO);

        BigDecimal grandTotal = subTotal.subtract(discountTotal).add(taxTotal).add(serviceCharge);
        order.setGrandTotal(grandTotal);
    }

    @Override
    public Order calculate(OrderDto orderDto, String token) {
        Order order = orderDto.toOrder();
        UserDto userDto = userService.getUserDetail(jwtUtil.extractUsername(token), token);
        order.setStore(userDto.getStore());
        order.setOrderNo(orderService.generateOrderNo(order));
        order.setCustomerId(-1);
        order.setOrderDetails(orderDetailService.populateOrderDetail(orderDto.getOrderDetailDtos(), token));

        calculateSubtotal(order);
        calculateDiscount(order, orderDto);
        calculateTax(order, orderDto);
        calculateServiceCharge(order);
        calculateGrandTotal(order);

        return order;
    }

    @Override
    public Order confirm(OrderDto orderDto, String token) {
        Order order = calculate(orderDto, token);
        order.setStatus(OrderStatus.CONFIRMED);
        orderService.insert(order);
        return order;
    }

    @Override
    public Order print(OrderDto orderDto, String token) {
        return calculate(orderDto, token);
    }

    @Override
    public List<Order> getDailyListByStore(String token) {
        UserDto userDto = userService.getUserDetail(jwtUtil.extractUsername(token), token);
        ZoneId zoneId = ZoneId.of(userDto.getStore().getTimeZone());

        Timestamp[] dailyRange = getDailyTimeRange(zoneId);

        Timestamp from = dailyRange[0];
        Timestamp until = dailyRange[1];

        return orderService.selectByStoreId(userDto.getStore().getId(), from, until);
    }
}
