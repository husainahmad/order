package com.harmoni.pos.order.service;

import com.harmoni.pos.order.mapper.OrderDetailCustomizationMapper;
import com.harmoni.pos.order.model.OrderDetailCustomization;
import com.harmoni.pos.order.model.OrderDetailCustomizationOption;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Reading an order's customization choices back out.
 * <p>
 * The options are a second table, so the thing to protect is the number of queries: a
 * basket of many lines must cost the same two reads as a basket of one, never a read per
 * group. These tests assert the call counts, not just that the data comes back right.
 */
@ExtendWith(MockitoExtension.class)
class OrderDetailCustomizationServiceImplTest {

    @Mock
    private OrderDetailCustomizationMapper customizationMapper;

    @Mock
    private OrderDetailCustomizationOptionService optionService;

    @InjectMocks
    private OrderDetailCustomizationServiceImpl service;

    @Test
    void getByOrderDetailIds_shouldAttachEachGroupsOptions() {
        OrderDetailCustomization size = group(1L, 10, "Size", 1);
        OrderDetailCustomization milk = group(2L, 10, "Milk", 1);
        when(customizationMapper.selectByOrderDetailIds(anyList())).thenReturn(List.of(size, milk));
        when(optionService.getByCustomizationIds(anyList())).thenReturn(List.of(
                option(1L, "Large", "1500", 1),
                option(2L, "Oat Milk", "4000", 2)));

        List<OrderDetailCustomization> result = service.getByOrderDetailIds(List.of(10));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getOptions()).hasSize(1);
        assertThat(result.get(0).getOptions().get(0).getCustomizationOptionName()).isEqualTo("Large");
        assertThat(result.get(1).getOptions()).hasSize(1);
        assertThat(result.get(1).getOptions().get(0).getCustomizationOptionName()).isEqualTo("Oat Milk");
    }

    /**
     * Three lines with two groups each, two options per group. Read group by group this
     * would be one query per group; it must stay two queries for the whole batch.
     */
    @Test
    void getByOrderDetailIds_shouldReadTheOptionsOnceForTheWholeBatch() {
        List<OrderDetailCustomization> groups = new ArrayList<>();
        List<OrderDetailCustomizationOption> options = new ArrayList<>();
        long groupId = 1;
        for (int detail : new int[]{10, 11, 12}) {
            for (int groupIndex = 0; groupIndex < 2; groupIndex++) {
                groups.add(group(groupId, detail, "Group " + groupIndex, detail));
                options.add(option(groupId, "Option A", "1000", 1));
                options.add(option(groupId, "Option B", "2000", 1));
                groupId++;
            }
        }
        when(customizationMapper.selectByOrderDetailIds(anyList())).thenReturn(groups);
        when(optionService.getByCustomizationIds(anyList())).thenReturn(options);

        List<OrderDetailCustomization> result = service.getByOrderDetailIds(List.of(10, 11, 12));

        assertThat(result).hasSize(6);
        assertThat(result).allSatisfy(group -> assertThat(group.getOptions()).hasSize(2));

        verify(customizationMapper, times(1)).selectByOrderDetailIds(anyList());
        verify(optionService, times(1)).getByCustomizationIds(anyList());
    }

    @Test
    void getByOrderDetailIds_shouldSkipTheOptionLookupWhenThereAreNoGroups() {
        when(customizationMapper.selectByOrderDetailIds(anyList())).thenReturn(List.of());

        assertThat(service.getByOrderDetailIds(List.of(10))).isEmpty();

        verify(optionService, times(0)).getByCustomizationIds(anyList());
    }

    @Test
    void getByOrderDetailIds_shouldNotQueryAtAllForAnEmptyBasket() {
        assertThat(service.getByOrderDetailIds(List.of())).isEmpty();

        verify(customizationMapper, times(0)).selectByOrderDetailIds(anyList());
        verify(optionService, times(0)).getByCustomizationIds(anyList());
    }

    /**
     * A group whose options were all removed keeps the field unset rather than an empty
     * list: the model is NON_NULL, so a group with nothing chosen says nothing.
     */
    @Test
    void getByOrderDetailIds_shouldLeaveAGroupWithoutOptionsUnset() {
        OrderDetailCustomization bare = group(1L, 10, "Size", 1);
        when(customizationMapper.selectByOrderDetailIds(anyList())).thenReturn(List.of(bare));
        when(optionService.getByCustomizationIds(anyList())).thenReturn(List.of());

        List<OrderDetailCustomization> result = service.getByOrderDetailIds(List.of(10));

        assertThat(result.get(0).getOptions()).isNull();
    }

    // ----------------------------------------------------------------------- fixtures

    private static OrderDetailCustomization group(Long id, Integer orderDetailId, String name,
                                                   Integer customizationId) {
        return new OrderDetailCustomization()
                .setId(id)
                .setOrderDetailId(orderDetailId)
                .setCustomizationId(customizationId)
                .setCustomizationName(name);
    }

    private static OrderDetailCustomizationOption option(Long groupId, String name, String price,
                                                          int quantity) {
        return new OrderDetailCustomizationOption()
                .setOrderDetailCustomizationId(groupId)
                .setCustomizationOptionName(name)
                .setPrice(new BigDecimal(price))
                .setQuantity(quantity)
                .setAmount(new BigDecimal(price).multiply(BigDecimal.valueOf(quantity)));
    }
}
