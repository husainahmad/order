package com.harmoni.pos.order.service;

import com.harmoni.pos.order.mapper.OrderDetailCustomizationMapper;
import com.harmoni.pos.order.model.OrderDetailCustomization;
import com.harmoni.pos.order.model.OrderDetailCustomizationOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service("orderDetailCustomizationService")
@RequiredArgsConstructor
@Slf4j
public class OrderDetailCustomizationServiceImpl implements OrderDetailCustomizationService {

    private final OrderDetailCustomizationMapper customizationMapper;
    private final OrderDetailCustomizationOptionService optionService;

    @Override
    @Transactional(transactionManager = "transactionSQLTransactionManager")
    public int record(List<OrderDetailCustomization> rows) {
        if (ObjectUtils.isEmpty(rows)) {
            return 0;
        }
        Date now = new Date(System.currentTimeMillis());
        rows.forEach(row -> row.setCreatedAt(now));
        customizationMapper.insertBulk(rows);

        List<OrderDetailCustomizationOption> options = new ArrayList<>();
        for (OrderDetailCustomization row : rows) {
            if (!ObjectUtils.isEmpty(row.getOptions())) {
                for (OrderDetailCustomizationOption option : row.getOptions()) {
                    option.setOrderDetailCustomizationId(row.getId());
                    option.setCreatedAt(now);
                    if (option.getPrice() == null) {
                        option.setPrice(java.math.BigDecimal.ZERO);
                    }
                    if (option.getAmount() == null) {
                        option.setAmount(java.math.BigDecimal.ZERO);
                    }
                    if (option.getQuantity() == null) {
                        option.setQuantity(1);
                    }
                    options.add(option);
                }
            }
        }
        if (!options.isEmpty()) {
            optionService.record(options);
        }
        log.debug("Recorded {} customization groups", rows.size());
        return rows.size();
    }

    /**
     * {@inheritDoc}
     * <p>
     * The options are read in a second batch and hung back on the groups they belong to,
     * rather than joined into the group query. A group with three options would otherwise
     * repeat its own row three times, and the caller would have to de-duplicate groups
     * that are conceptually one row. Two queries for the whole basket is the same order of
     * cost as one, and keeps each row meaning exactly one thing.
     */
    @Override
    public List<OrderDetailCustomization> getByOrderDetailIds(List<Integer> orderDetailIds) {
        if (ObjectUtils.isEmpty(orderDetailIds)) {
            return List.of();
        }
        List<OrderDetailCustomization> groups = customizationMapper.selectByOrderDetailIds(orderDetailIds);
        if (ObjectUtils.isEmpty(groups)) {
            return List.of();
        }

        List<Long> customizationIds = groups.stream()
                .map(OrderDetailCustomization::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (customizationIds.isEmpty()) {
            return groups;
        }

        // A group with no options stays with the field unset: the model is NON_NULL, so a
        // group that genuinely had nothing chosen says nothing rather than showing an empty
        // list on the receipt.
        Map<Long, List<OrderDetailCustomizationOption>> optionsByGroup =
                optionService.getByCustomizationIds(customizationIds).stream()
                        .filter(option -> option.getOrderDetailCustomizationId() != null)
                        .collect(Collectors.groupingBy(OrderDetailCustomizationOption::getOrderDetailCustomizationId));

        for (OrderDetailCustomization group : groups) {
            List<OrderDetailCustomizationOption> options = optionsByGroup.get(group.getId());
            if (!ObjectUtils.isEmpty(options)) {
                group.setOptions(options);
            }
        }
        return groups;
    }
}
