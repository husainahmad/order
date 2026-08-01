package com.harmoni.pos.order.business.service;

import com.harmoni.pos.order.business.service.menu.UserService;
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
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.harmoni.pos.order.utils.DateTimeUtils.getDailyTimeRange;

/**
 * Implementation of {@link OrderBusinessService} that handles business logic
 * related to processing orders including calculations, confirmation, and retrieval.
 */
@RequiredArgsConstructor
@Service("orderBusinessService")
public class OrderBusinessServiceImpl implements OrderBusinessService {

    private final OrderService orderService;
    private final OrderDetailService orderDetailService;
    private final UserService userService;

    /**
     * Calculates the subtotal of the order based on the sum of its order detail amounts.
     *
     * @param order the order to calculate
     */
    @Override
    public void calculateSubtotal(Order order) {
        BigDecimal subTotal = order.getOrderDetails().stream()
                .map(orderDetail -> Optional.ofNullable(orderDetail.getAmount()).orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setSubTotal(subTotal);
    }

    /**
     * Sets the service charge for the order.
     * Currently set to zero; implement custom logic if applicable.
     *
     * @param order the order to calculate
     */
    @Override
    public void calculateServiceCharge(Order order) {
        // TODO: Implement service charge calculation if applicable
        order.setServiceChargeTotal(BigDecimal.ZERO);
    }

    /**
     * Sets the discount amount for the order.
     *
     * @param order     the order to update
     * @param orderDto  the input DTO containing the discount value
     */
    @Override
    public void calculateDiscount(Order order, OrderDto orderDto) {
        BigDecimal discount = Optional.ofNullable(orderDto.getDiscount()).orElse(BigDecimal.ZERO);
        order.setDiscountTotal(discount);
        calculateGrandTotal(order);
    }

    /**
     * Sets the tax amount for the order.
     * Currently set to zero; implement custom logic if applicable.
     *
     * @param order     the order to update
     * @param orderDto  the input DTO
     */
    @Override
    public void calculateTax(Order order, OrderDto orderDto) {
        // TODO: Implement tax calculation logic if required
        order.setTaxTotal(BigDecimal.ZERO);
    }

    /**
     * Calculates the grand total based on subtotal, discount, tax, and service charge.
     *
     * @param order the order to update
     */
    @Override
    public void calculateGrandTotal(Order order) {
        BigDecimal subTotal = Optional.ofNullable(order.getSubTotal()).orElse(BigDecimal.ZERO);
        BigDecimal discountTotal = Optional.ofNullable(order.getDiscountTotal()).orElse(BigDecimal.ZERO);
        BigDecimal taxTotal = Optional.ofNullable(order.getTaxTotal()).orElse(BigDecimal.ZERO);
        BigDecimal serviceCharge = Optional.ofNullable(order.getServiceChargeTotal()).orElse(BigDecimal.ZERO);

        BigDecimal grandTotal = subTotal.subtract(discountTotal).add(taxTotal).add(serviceCharge);
        order.setGrandTotal(grandTotal);
    }

    /**
     * Converts {@link OrderDto} into {@link Order} and performs all business calculations.
     *
     * @param orderDto the DTO containing order details
     * @param username the username used to get user/store context
     * @return the calculated {@link Order}
     */
    @Override
    public Order calculate(OrderDto orderDto, String username) {
        Order order = orderDto.toOrder();
        UserDto userDto = userService.getUserDetail(username);
        order.setStore(userDto.getStore());
        order.setOrderNo(orderService.generateOrderNo(order));
        order.setCustomerId(-1);
        order.setOrderDetails(orderDetailService.populateOrderDetail(orderDto.getOrderDetailDtos()));

        calculateSubtotal(order);
        calculateDiscount(order, orderDto);
        calculateTax(order, orderDto);
        calculateServiceCharge(order);
        calculateGrandTotal(order);

        return order;
    }

    /**
     * Confirms and persists a new order after performing calculations.
     *
     * @param orderDto the order input DTO
     * @param username the username used to get user/store context
     * @return the confirmed and persisted order
     */
    @Override
    public Order confirm(OrderDto orderDto, String username) {
        Order order = calculate(orderDto, username);
        order.setStatus(OrderStatus.CONFIRMED);
        orderService.insert(order);
        return order;
    }

    /**
     * Performs calculations for order printing (e.g., before sending to a printer).
     * Does not persist the order.
     *
     * @param orderDto the order input DTO
     * @param username the username used to get user/store context
     * @return the calculated order
     */
    @Override
    public Order print(OrderDto orderDto, String username) {
        return calculate(orderDto, username);
    }

    /**
     * Voids an order by setting its status to {@link OrderStatus#VOID}.
     *
     * @param orderId  the ID of the order to void
     * @param username the username used to get user/store context
     * @return the updated order
     */
    @Override
    public Order voidOrder(Integer orderId, String username) {
        Order order = orderService.selectByPrimaryKey(orderId);
        order.setStatus(OrderStatus.VOID);
        order.setUpdatedAt(new Date(System.currentTimeMillis()));
        orderService.voidOrderById(order);
        return order;
    }

    /**
     * Retrieves the list of orders for the current day based on the authenticated store.
     *
     * @param username the username used to determine the store and time zone
     * @return list of today's orders for the store
     */
    @Override
    public List<Order> getDailyListByStore(String username) {
        UserDto userDto = userService.getUserDetail(username);
        ZoneId zoneId = ZoneId.of(userDto.getStore().getTimeZone());

        Timestamp[] dailyRange = getDailyTimeRange(zoneId);

        Timestamp from = dailyRange[0];
        Timestamp until = dailyRange[1];

        return orderService.selectByStoreId(userDto.getStore().getId(), from, until);
    }
}
