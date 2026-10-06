package com.harmoni.pos.order.service;

import com.harmoni.pos.order.mapper.OrderDetailCustomizationOptionMapper;
import com.harmoni.pos.order.model.OrderDetailCustomizationOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;

@Service("orderDetailCustomizationOptionService")
@RequiredArgsConstructor
@Slf4j
public class OrderDetailCustomizationOptionServiceImpl implements OrderDetailCustomizationOptionService {

    private final OrderDetailCustomizationOptionMapper optionMapper;

    @Override
    @Transactional(transactionManager = "transactionSQLTransactionManager")
    public int record(List<OrderDetailCustomizationOption> rows) {
        if (ObjectUtils.isEmpty(rows)) {
            return 0;
        }
        Date now = new Date(System.currentTimeMillis());
        rows.forEach(row -> {
            row.setCreatedAt(now);
            if (row.getPrice() == null) {
                row.setPrice(java.math.BigDecimal.ZERO);
            }
            if (row.getAmount() == null) {
                row.setAmount(java.math.BigDecimal.ZERO);
            }
            if (row.getQuantity() == null) {
                row.setQuantity(1);
            }
        });
        return optionMapper.insertBulk(rows);
    }
}
