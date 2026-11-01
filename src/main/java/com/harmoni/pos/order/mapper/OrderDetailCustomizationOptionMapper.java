package com.harmoni.pos.order.mapper;

import com.harmoni.pos.order.model.OrderDetailCustomizationOption;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderDetailCustomizationOptionMapper {

    int insertBulk(List<OrderDetailCustomizationOption> rows);

    /**
     * Reads the options chosen under the given customization groups.
     *
     * @param customizationIds the {@code order_detail_customizations} row ids
     * @return the options, ordered by their group and then by insertion order
     */
    List<OrderDetailCustomizationOption> selectByCustomizationIds(List<Long> customizationIds);
}
