package com.harmoni.pos.order.service;

import com.harmoni.pos.order.business.service.menu.MenuService;
import com.harmoni.pos.order.mapper.OrderDetailMapper;
import com.harmoni.pos.order.model.OrderDetail;
import com.harmoni.pos.order.model.OrderDetailSku;
import com.harmoni.pos.order.model.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link OrderDetailService} interface.
 * Provides business logic for managing {@link OrderDetail} and related SKU data.
 * <p>
 * This service includes functionality for:
 * - CRUD operations on {@link OrderDetail}
 * - Bulk insert with associated {@link OrderDetailSku}
 * - Populating order details from DTOs using product and SKU services
 * </p>
 *
 * @author Ahmad
 * @since 1.0
 */
@RequiredArgsConstructor
@Service("orderDetailService")
public class OrderDetailServiceImpl implements OrderDetailService {

    private final OrderDetailMapper orderDetailMapper;
    private final MenuService menuService;
    private final OrderDetailSkuService orderDetailSkuService;

    /**
     * Deletes an order detail by its primary key.
     *
     * @param id the ID of the order detail
     * @return number of rows affected
     */
    @Override
    public int deleteByPrimaryKey(Integer id) {
        return orderDetailMapper.deleteByPrimaryKey(id);
    }

    /**
     * Inserts a single {@link OrderDetail} into the database.
     *
     * @param row the order detail to insert
     * @return number of rows affected
     */
    @Override
    public int insert(OrderDetail row) {
        return orderDetailMapper.insert(row);
    }

    /**
     * Inserts a list of {@link OrderDetail} and their associated SKUs into the database.
     *
     * @param orderDetails list of order details to insert
     */
    @Override
    public void insertBulk(List<OrderDetail> orderDetails) {
        orderDetailMapper.insertBulk(orderDetails);

        List<OrderDetailSku> orderDetailSkus = new ArrayList<>();
        for (OrderDetail orderDetail : orderDetails) {
            for (OrderDetailSku orderDetailSku : orderDetail.getOrderDetailSkus()) {
                orderDetailSku.setOrderDetailId(orderDetail.getId());
                orderDetailSku.setCreatedAt(new Date(System.currentTimeMillis()));
                orderDetailSkus.add(orderDetailSku);
            }
        }
        orderDetailSkuService.insertBulk(orderDetailSkus);
    }

    /**
     * Retrieves an {@link OrderDetail} by its ID.
     *
     * @param id the ID of the order detail
     * @return the matching order detail or {@code null} if not found
     */
    @Override
    public OrderDetail selectByPrimaryKey(Integer id) {
        return orderDetailMapper.selectByPrimaryKey(id);
    }

    /**
     * Populates a list of {@link OrderDetail} objects from the given DTOs,
     * enriching with product names, categories, and SKU prices.
     *
     * @param orderDetailDtos list of order detail DTOs
     * @param token JWT or access token for fetching remote data
     * @return a list of populated {@link OrderDetail} entities
     */
    @Override
    public List<OrderDetail> populateOrderDetail(List<OrderDetailDto> orderDetailDtos, String token) {
        List<OrderDetail> orderDetails = new ArrayList<>();

        List<ProductDto> productDtos = this.getProductWithName(orderDetailDtos, token);
        Map<Integer, String> productNameMap = productDtos.stream()
                .collect(Collectors.toMap(
                        ProductDto::getId,
                        productDto -> Optional.ofNullable(productDto.getName()).orElse("")
                ));

        Map<Integer, CategoryDto> categoryMap = productDtos.stream()
                .collect(Collectors.toMap(
                        ProductDto::getId,
                        productDto -> Optional.ofNullable(productDto.getCategory()).orElseThrow()
                ));

        List<SkuDto> skuDtos = this.getDetailSkuWithPrice(orderDetailDtos, token);
        Map<Integer, BigDecimal> skuPriceMap = skuDtos.stream()
                .collect(Collectors.toMap(
                        SkuDto::getId,
                        skuDto -> Optional.ofNullable(skuDto.getSkuTierPrice())
                                .map(SkuTierPriceDto::getPrice)
                                .orElse(BigDecimal.ZERO)
                ));

        for (OrderDetailDto orderDetailDto : orderDetailDtos) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setProductId(orderDetailDto.getProductId());
            orderDetail.setProductName(productNameMap.getOrDefault(orderDetail.getProductId(), "Unknown Product"));
            orderDetail.setCategoryId(categoryMap.getOrDefault(orderDetail.getProductId(), null).getId());
            orderDetail.setCategoryName(categoryMap.getOrDefault(orderDetail.getProductId(), null).getName());

            List<OrderDetailSku> orderDetailSkus = new ArrayList<>();
            BigDecimal subTotal = BigDecimal.ZERO;

            for (OrderDetailSkuDto orderDetailSkuDto : orderDetailDto.getOrderDetailSkus()) {
                BigDecimal price = skuPriceMap.getOrDefault(orderDetailSkuDto.getSkuId(), BigDecimal.ZERO);
                BigDecimal amount = price.multiply(BigDecimal.valueOf(orderDetailSkuDto.getQuantity()));

                orderDetailSkus.add(orderDetailSkuDto.toOrderDetailSku()
                        .setPrice(price)
                        .setAmount(amount));

                subTotal = subTotal.add(amount);
            }

            orderDetail.setOrderDetailSkus(orderDetailSkus);
            orderDetail.setAmount(subTotal);
            orderDetails.add(orderDetail);
        }

        return orderDetails;
    }

    /**
     * Retrieves SKU details (including pricing) from the menu service based on the order detail input.
     *
     * @param orderDetailDtos list of order detail DTOs
     * @param token JWT or access token
     * @return list of SKU DTOs with price information
     */
    @Override
    public List<SkuDto> getDetailSkuWithPrice(List<OrderDetailDto> orderDetailDtos, String token) {
        List<Integer> skuIds = new ArrayList<>();
        for (OrderDetailDto orderDetailDto : orderDetailDtos) {
            for (OrderDetailSkuDto orderDetailSkuDto : orderDetailDto.getOrderDetailSkus()) {
                skuIds.add(orderDetailSkuDto.getSkuId());
            }
        }
        return menuService.getSkusWithPrice(skuIds, token);
    }

    /**
     * Retrieves product details (including name and category) from the menu service.
     *
     * @param orderDetailDtos list of order detail DTOs
     * @param token JWT or access token
     * @return list of product DTOs with names
     */
    @Override
    public List<ProductDto> getProductWithName(List<OrderDetailDto> orderDetailDtos, String token) {
        List<Integer> productIds = new ArrayList<>();
        for (OrderDetailDto orderDetailDto : orderDetailDtos) {
            productIds.add(orderDetailDto.getProductId());
        }
        return menuService.getProductWithName(productIds, token);
    }

    /**
     * Updates an existing {@link OrderDetail} record.
     *
     * @param row the updated order detail
     * @return number of rows affected
     */
    @Override
    public int updateByPrimaryKey(OrderDetail row) {
        return orderDetailMapper.updateByPrimaryKey(row);
    }
}
