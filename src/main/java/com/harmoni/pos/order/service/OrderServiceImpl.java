package com.harmoni.pos.order.service;

import com.harmoni.pos.order.exception.BusinessNoContentRequestException;
import com.harmoni.pos.order.mapper.OrderMapper;
import com.harmoni.pos.order.model.Order;
import com.harmoni.pos.order.model.OrderDetail;
import com.harmoni.pos.order.model.OrderDetailSku;
import com.harmoni.pos.order.model.OrderDetailCustomization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of {@link OrderService} that provides business logic
 * related to order creation, retrieval, updating, and deletion.
 *
 * <p>This service interacts with the {@link OrderMapper} for database operations
 * and {@link OrderDetailService} for handling order details.</p>
 *
 * @author Ahmad
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
@Service("orderService")
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderDetailService orderDetailService;
    private final OrderDetailCustomizationService orderDetailCustomizationService;

    /**
     * Generates a unique order number based on the store ID, current timestamp, and a random string.
     *
     * @param order the order for which to generate the order number
     * @return a unique order number string
     */
    @Override
    public String generateOrderNo(Order order) {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "ORD" + order.getStore().getId() + "-" + datePart + "-" + randomPart;
    }

    /**
     * Deletes an order by its primary key.
     *
     * @param id the ID of the order to delete
     * @return number of rows affected
     */
    @Override
    public int deleteByPrimaryKey(Integer id) {
        return orderMapper.deleteByPrimaryKey(id);
    }

    /**
     * Inserts a new order and its details into the database.
     *
     * @param order the order to insert
     * @return number of rows affected
     */
    @Override
    @Transactional(transactionManager = "transactionSQLTransactionManager")
    public int insert(Order order) {
        order.setCreatedAt(new Date(System.currentTimeMillis()));
        int rowCount = orderMapper.insert(order);
        log.debug("{} Order Created ", rowCount);
        for (OrderDetail orderDetail : order.getOrderDetails()) {
            orderDetail.setOrderId(order.getId());
            orderDetail.setCreatedAt(new Date(System.currentTimeMillis()));
        }
        orderDetailService.insertBulk(order.getOrderDetails());
        return rowCount;
    }

    /**
     * Atomically checks for an existing order with the given idempotency key, and
     * inserts a new order only if no duplicate exists.
     *
     * <p>If {@code idempotencyKey} is non-blank and an order with that key already
     * exists in the database, the existing order is returned. Otherwise the key is
     * stored with the new order and both order + details are inserted in a single
     * transaction.</p>
     *
     * <p>Under concurrent requests with the same key, the database-level unique
     * constraint is the final guard. The losing transaction catches the
     * {@link DuplicateKeyException} and re-queries to return the winner's order.</p>
     *
     * @param order          the order to insert (with details already populated)
     * @param idempotencyKey the client-supplied key, may be {@code null}
     * @return the existing order (idempotency hit) or the newly inserted order
     */
    @Override
    @Transactional(transactionManager = "transactionSQLTransactionManager")
    public Order insertWithIdempotency(Order order, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Order existing = findByIdempotencyKey(idempotencyKey);
            if (existing != null) {
                return existing;
            }
            order.setIdempotencyKey(idempotencyKey);
        }

        try {
            insert(order);
        } catch (DuplicateKeyException e) {
            if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                Order existing = findByIdempotencyKey(idempotencyKey);
                if (existing != null) {
                    return existing;
                }
            }
            throw e;
        }

        return order;
    }

    /**
     * Retrieves an order by its primary key.
     *
     * @param id the ID of the order
     * @return the found {@link Order}
     * @throws BusinessNoContentRequestException if no order is found
     */
    @Override
    public Order selectByPrimaryKey(Integer id) {
        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null) {
            throw new BusinessNoContentRequestException(
                    BusinessNoContentRequestException.NO_CONTENT, null);
        }
        attachCustomizations(List.of(order));
        return order;
    }

    /**
     * Retrieves an order by its idempotency key.
     *
     * @param idempotencyKey the idempotency key sent by the client
     * @return the matching {@link Order}, or {@code null} if none exists
     */
    @Override
    public Order findByIdempotencyKey(String idempotencyKey) {
        Order order = orderMapper.selectByIdempotencyKey(idempotencyKey);
        if (order != null) {
            attachCustomizations(List.of(order));
        }
        return order;
    }

    /**
     * Retrieves a list of orders by store ID and date range.
     *
     * @param storeId the ID of the store
     * @param from the start timestamp (inclusive)
     * @param until the end timestamp (inclusive)
     * @return a list of matching {@link Order} objects
     */
    @Override
    public List<Order> selectByStoreId(Integer storeId, Timestamp from, Timestamp until) {
        List<Order> orders = orderMapper.selectByStoreId(storeId, from, until);
        attachCustomizations(orders);
        return orders;
    }

    /**
     * Puts each line's customization choices back on the line they belong to.
     * <p>
     * The order queries join orders to details to lines but not to
     * {@code order_detail_sku_customizations}, because that join multiplies every row by the
     * number of choices on a line and the order mapper has no way to nest three levels of
     * collection. So the choices are fetched separately and grouped by line here. Doing it
     * here rather than in each caller is what stops the read paths drifting apart: an order
     * returned for a receipt and the same order returned by a list endpoint should describe
     * itself the same way, and a customised line that silently came back bare would print a
     * receipt that does not match the sale.
     * <p>
     * A line with no choices is left with the field unset rather than an empty list, matching
     * how a freshly built order behaves: the field is {@code NON_NULL}, so a plain line says
     * nothing about customizations rather than claiming it had none.
     *
     * @param orders the orders just read, each already carrying its lines
     */
    private void attachCustomizations(List<Order> orders) {
        if (ObjectUtils.isEmpty(orders)) {
            return;
        }

        List<Integer> detailIds = orders.stream()
                .filter(Objects::nonNull)
                .map(Order::getOrderDetails)
                .filter(details -> !ObjectUtils.isEmpty(details))
                .flatMap(List::stream)
                .map(OrderDetail::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (detailIds.isEmpty()) {
            return;
        }

        List<OrderDetailCustomization> customizations =
                orderDetailCustomizationService.getByOrderDetailIds(detailIds);
        if (ObjectUtils.isEmpty(customizations)) {
            return;
        }

        Map<Integer, List<OrderDetailCustomization>> byDetail = customizations.stream()
                .collect(Collectors.groupingBy(OrderDetailCustomization::getOrderDetailId));

        for (Order order : orders) {
            if (order == null || ObjectUtils.isEmpty(order.getOrderDetails())) {
                continue;
            }
            for (OrderDetail orderDetail : order.getOrderDetails()) {
                List<OrderDetailCustomization> forDetail = byDetail.get(orderDetail.getId());
                if (!ObjectUtils.isEmpty(forDetail)) {
                    orderDetail.setCustomizations(forDetail);
                }
            }
        }
    }

    /**
     * Updates an existing order.
     *
     * @param order the updated order object
     * @return number of rows affected
     */
    @Override
    public int updateByPrimaryKey(Order order) {
        return orderMapper.updateByPrimaryKey(order);
    }

    /**
     * Voids (cancels) an order by marking its status as void.
     *
     * @param order the order to void
     * @return number of rows affected
     */
    @Override
    public int voidOrderById(Order order) {
        return orderMapper.voidById(order);
    }
}
