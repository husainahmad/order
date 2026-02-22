package com.harmoni.pos.order.service;

import com.harmoni.pos.order.exception.BusinessNoContentRequestException;
import com.harmoni.pos.order.mapper.OrderMapper;
import com.harmoni.pos.order.model.Order;
import com.harmoni.pos.order.model.OrderDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
        return orderMapper.selectByStoreId(storeId, from, until);
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
