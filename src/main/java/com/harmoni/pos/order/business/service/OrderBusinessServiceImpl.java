package com.harmoni.pos.order.business.service;

import com.harmoni.pos.order.business.service.menu.UserService;
import com.harmoni.pos.order.business.service.promotion.OrderPromotionService;
import com.harmoni.pos.order.model.CalculatedOrder;
import com.harmoni.pos.order.model.OrderCustomer;
import com.harmoni.pos.order.model.OrderDetail;
import com.harmoni.pos.order.model.OrderDetailSku;
import com.harmoni.pos.order.model.OrderItemDiscount;
import com.harmoni.pos.order.model.Order;
import com.harmoni.pos.order.model.OrderStatus;
import com.harmoni.pos.order.model.dto.OrderDto;
import com.harmoni.pos.order.model.dto.UserDto;
import com.harmoni.pos.order.model.dto.pricing.OrderItemDiscountSnapshotDto;
import com.harmoni.pos.order.service.OrderCustomerService;
import com.harmoni.pos.order.service.OrderDetailService;
import com.harmoni.pos.order.service.OrderItemDiscountService;
import com.harmoni.pos.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.harmoni.pos.order.utils.DateTimeUtils.getDailyTimeRange;

/**
 * Implementation of {@link OrderBusinessService} that handles business logic
 * related to processing orders including calculations, confirmation, and retrieval.
 */
@RequiredArgsConstructor
@Service("orderBusinessService")
@Slf4j
public class OrderBusinessServiceImpl implements OrderBusinessService {

    /**
     * The customer ID recorded for a sale made to nobody in particular. The orders table
     * already used this value, so it is kept rather than replaced with null.
     */
    private static final int WALK_IN_CUSTOMER_ID = -1;

    private final OrderService orderService;
    private final OrderDetailService orderDetailService;
    private final UserService userService;
    private final OrderPromotionService orderPromotionService;
    private final OrderItemDiscountService orderItemDiscountService;
    private final OrderCustomerService orderCustomerService;

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
     * {@inheritDoc}
     */
    @Override
    public CalculatedOrder calculateDiscount(Order order, UserDto userDto, String username) {
        CalculatedOrder calculated = orderPromotionService.applyPromotions(order, userDto, username);
        calculateGrandTotal(order);
        return calculated;
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
        return calculateWithPromotions(orderDto, username).order();
    }

    /**
     * {@inheritDoc}
     *
     * <p>The discount is priced by the menu service, not read from the request. The
     * {@code discount} field on {@link OrderDto} is accepted so existing clients keep
     * working, but is deliberately ignored: a client that could name its own discount
     * would not need a promotion engine at all.</p>
     */
    @Override
    public CalculatedOrder calculateWithPromotions(OrderDto orderDto, String username) {
        Order order = orderDto.toOrder();
        UserDto userDto = userService.getUserDetail(username);
        order.setStore(userDto.getStore());
        order.setOrderNo(orderService.generateOrderNo(order));
        order.setCustomerId(resolveCustomerId(orderDto));
        order.setOrderDetails(orderDetailService.populateOrderDetail(orderDto.getOrderDetailDtos(),
                userDto.getStore().getId(), username));

        calculateSubtotal(order);
        CalculatedOrder calculated = calculateDiscount(order, userDto, username);
        calculateTax(order, orderDto);
        calculateServiceCharge(order);
        calculateGrandTotal(order);

        return new CalculatedOrder(order, calculated.lineDiscounts(), calculated.totalDiscount());
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
        return confirm(orderDto, username, null);
    }

    /**
     * Confirms and persists a new order after performing calculations, applying
     * idempotency based on the supplied key.
     *
     * <p>When {@code idempotencyKey} is non-blank and an order already exists for
     * that key, the existing order is returned without creating a duplicate.
     * Otherwise the key is stored alongside the newly created order.</p>
     *
     * @param orderDto       the order input DTO
     * @param username       the username used to get user/store context
     * @param idempotencyKey the client-supplied idempotency key, may be {@code null}
     * @return the confirmed and persisted order
     */
    @Override
    public Order confirm(OrderDto orderDto, String username, String idempotencyKey) {
        CalculatedOrder calculated = calculateWithPromotions(orderDto, username);
        Order order = calculated.order();
        order.setStatus(OrderStatus.CONFIRMED);
        Order persisted = orderService.insertWithIdempotency(order, idempotencyKey);
        if (persisted == order) {
            recordPromotionAudit(calculated);
            recordCustomerSnapshot(persisted);
        } else {
            log.debug("Idempotency key {} replayed, not recording promotions again", idempotencyKey);
        }
        return persisted;
    }

    /**
     * Records what each line was discounted by, now that the order lines have their IDs,
     * and hangs the recorded rows back on the lines they belong to.
     * <p>
     * The rows live in this service's own database, beside the lines they describe, so this
     * is a plain local write of what the menu service already priced. It still runs after
     * the insert rather than inside it: the audit rows need the generated line IDs, and a
     * failure here should not roll back a sale that has already been taken. The order row
     * remains authoritative, since its totals were priced before the insert.
     * <p>
     * The same rows are attached to the lines rather than left only in the table, so the
     * order handed back to the caller describes itself: which promotions hit which line,
     * alongside the customization choices already on it. A receipt printed straight from
     * this response would otherwise have to query the audit table to explain a discount.
     *
     * @param calculated the priced order and its line level discounts
     */
    private void recordPromotionAudit(CalculatedOrder calculated) {
        List<OrderItemDiscountSnapshotDto> snapshots = orderPromotionService.toSnapshots(calculated);
        if (snapshots.isEmpty()) {
            return;
        }
        List<OrderItemDiscount> recorded = orderItemDiscountService.recordSnapshot(snapshots);
        attachDiscountsToLines(calculated.order(), recorded);
    }

    /**
     * Puts each recorded discount on the line it was taken off, matched on the line ID the
     * audit rows are keyed by.
     * <p>
     * A row whose line cannot be found is left off rather than dumped on the first line:
     * mislabelling a discount is worse than omitting it, and the row stays in the table
     * either way.
     *
     * @param order     the persisted order, whose lines now carry their generated IDs
     * @param discounts the discount rows just recorded
     */
    private static void attachDiscountsToLines(Order order, List<OrderItemDiscount> discounts) {
        if (ObjectUtils.isEmpty(discounts) || order == null) {
            return;
        }
        for (OrderDetail orderDetail : nullSafe(order.getOrderDetails())) {
            for (OrderDetailSku orderDetailSku : nullSafe(orderDetail.getOrderDetailSkus())) {
                List<OrderItemDiscount> forLine = discounts.stream()
                        .filter(discount -> orderDetailSku.getId() != null
                                && orderDetailSku.getId().longValue() == discount.getOrderItemId())
                        .collect(Collectors.toList());
                if (!forLine.isEmpty()) {
                    orderDetailSku.setDiscounts(forLine);
                }
            }
        }
    }

    private static <T> List<T> nullSafe(List<T> values) {
        return values == null ? Collections.emptyList() : values;
    }

    /**
     * Records who the sale was made to, now that the order has an ID to hang it on.
     * <p>
     * A walk-in sale writes nothing: there is no customer to describe, and a row of nulls
     * would be a record of nothing. Like the promotion audit this only runs for a genuinely
     * new order, so a replayed confirmation cannot append a second history row.
     */
    private void recordCustomerSnapshot(Order order) {
        OrderCustomer snapshot = orderCustomerService.record(order);
        if (snapshot == null) {
            log.debug("Order {} is a walk-in sale, no customer snapshot recorded", order.getOrderNo());
        }
    }

    /**
     * Decides the customer a sale belongs to.
     * <p>
     * A client may name a customer or leave the field empty for a walk-in. The absence of
     * a customer is recorded as {@code -1}, the value the orders table has always used, so
     * existing queries that filter on it keep working. It used to be written
     * unconditionally, which threw away the customer on every single sale.
     *
     * @param orderDto the incoming order
     * @return the customer's ID, or {@code -1} when the sale names no customer
     */
    private static int resolveCustomerId(OrderDto orderDto) {
        Integer customerId = orderDto.getCustomerId();
        return customerId == null || customerId < 0 ? WALK_IN_CUSTOMER_ID : customerId;
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
