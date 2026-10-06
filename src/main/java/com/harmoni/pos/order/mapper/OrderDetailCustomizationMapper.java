package com.harmoni.pos.order.mapper;

import com.harmoni.pos.order.model.OrderDetailCustomization;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderDetailCustomizationMapper {

    int insertBulk(List<OrderDetailCustomization> rows);

    List<OrderDetailCustomization> selectByOrderDetailIds(List<Integer> orderDetailIds);
}
