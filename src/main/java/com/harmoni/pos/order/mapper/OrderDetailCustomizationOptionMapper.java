package com.harmoni.pos.order.mapper;

import com.harmoni.pos.order.model.OrderDetailCustomizationOption;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderDetailCustomizationOptionMapper {

    int insertBulk(List<OrderDetailCustomizationOption> rows);
}
