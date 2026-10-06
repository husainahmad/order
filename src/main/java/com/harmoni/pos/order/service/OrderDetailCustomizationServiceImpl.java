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

    @Override
    public List<OrderDetailCustomization> getByOrderDetailIds(List<Integer> orderDetailIds) {
        if (ObjectUtils.isEmpty(orderDetailIds)) {
            return List.of();
        }
        return customizationMapper.selectByOrderDetailIds(orderDetailIds);
    }
}
