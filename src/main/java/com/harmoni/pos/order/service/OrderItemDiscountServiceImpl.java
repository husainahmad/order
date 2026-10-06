package com.harmoni.pos.order.service;

import com.harmoni.pos.order.mapper.OrderItemDiscountMapper;
import com.harmoni.pos.order.model.OrderItemDiscount;
import com.harmoni.pos.order.model.dto.pricing.OrderItemDiscountSnapshotDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Implementation of {@link OrderItemDiscountService} that persists promotion
 * discounts against order lines.
 * <p>
 * All database operations are delegated to {@link OrderItemDiscountMapper}.
 * </p>
 *
 * @author husainahmad
 */
@RequiredArgsConstructor
@Service("orderItemDiscountService")
public class OrderItemDiscountServiceImpl implements OrderItemDiscountService {

    private final OrderItemDiscountMapper orderItemDiscountMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(transactionManager = "transactionSQLTransactionManager")
    public List<OrderItemDiscount> recordSnapshot(List<OrderItemDiscountSnapshotDto> snapshots) {
        if (ObjectUtils.isEmpty(snapshots)) {
            return List.of();
        }
        Date now = new Date();
        List<OrderItemDiscount> discounts = new ArrayList<>(snapshots.size());
        for (OrderItemDiscountSnapshotDto snapshot : snapshots) {
            OrderItemDiscount discount = snapshot.toOrderItemDiscount();
            discount.setCreatedAt(now);
            discounts.add(discount);
        }
        orderItemDiscountMapper.insertBatch(discounts);
        return discounts;
    }
}
