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

@RequiredArgsConstructor
@Service("orderDetailService")
public class OrderDetailServiceImpl implements OrderDetailService {

    private final OrderDetailMapper orderDetailMapper;
    private final MenuService menuService;
    private final OrderDetailSkuService orderDetailSkuService;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return orderDetailMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(OrderDetail row) {
        return orderDetailMapper.insert(row);
    }

    @Override
    public void insertBulk(List<OrderDetail> orderDetails) {
        orderDetailMapper.insertBulk(orderDetails);

        List<OrderDetailSku> orderDetailSkus = new ArrayList<>();
        for (OrderDetail orderDetail: orderDetails) {
            for (OrderDetailSku orderDetailSku: orderDetail.getOrderDetailSkus()) {
                orderDetailSku.setOrderDetailId(orderDetail.getId());
                orderDetailSku.setCreatedAt(new Date(System.currentTimeMillis()));
                orderDetailSkus.add(orderDetailSku);
            }
        }
        orderDetailSkuService.insertBulk(orderDetailSkus);
    }

    @Override
    public OrderDetail selectByPrimaryKey(Integer id) {
        return orderDetailMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<OrderDetail> populateOrderDetail(List<OrderDetailDto> orderDetailDtos, String token) {
        List<OrderDetail> orderDetails = new ArrayList<>();

        // Fetch product names
        List<ProductDto> productDtos = this.getProductWithName(orderDetailDtos, token);
        Map<Integer, String> productNameMap = productDtos.stream()
                .collect(Collectors.toMap(
                        ProductDto::getId,
                        productDto -> Optional.ofNullable(productDto.getName()).orElse("")
                ));

        // Fetch SKU details
        List<SkuDto> skuDtos = this.getDetailSkuWithPrice(orderDetailDtos, token);
        Map<Integer, BigDecimal> skuPriceMap = skuDtos.stream()
                .collect(Collectors.toMap(
                        SkuDto::getId,
                        skuDto -> Optional.ofNullable(skuDto.getSkuTierPrice())
                                .map(SkuTierPriceDto::getPrice)
                                .orElse(BigDecimal.ZERO) // Default to 0 if null
                ));

        // Process order details
        for (OrderDetailDto orderDetailDto : orderDetailDtos) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setProductId(orderDetailDto.getProductId());

            // Avoid potential NPE by using getOrDefault
            orderDetail.setProductName(productNameMap.getOrDefault(orderDetail.getProductId(), "Unknown Product"));

            List<OrderDetailSku> orderDetailSkus = new ArrayList<>();
            BigDecimal subTotal = BigDecimal.ZERO;

            for (OrderDetailSkuDto orderDetailSkuDto : orderDetailDto.getOrderDetailSkus()) {
                BigDecimal price = skuPriceMap.getOrDefault(orderDetailSkuDto.getSkuId(), BigDecimal.ZERO);
                BigDecimal amount = price.multiply(BigDecimal.valueOf(orderDetailSkuDto.getQuantity())); // price * quantity

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

    @Override
    public List<SkuDto> getDetailSkuWithPrice(List<OrderDetailDto> orderDetailDtos, String token) {
        List<Integer> skuIds = new ArrayList<>();
        for (OrderDetailDto orderDetailDto: orderDetailDtos) {
            for (OrderDetailSkuDto orderDetailSkuDto: orderDetailDto.getOrderDetailSkus()) {
                skuIds.add(orderDetailSkuDto.getSkuId());
            }
        }
        return menuService.getSkusWithPrice(skuIds, token);
    }

    @Override
    public List<ProductDto> getProductWithName(List<OrderDetailDto> orderDetailDtos, String token) {
        List<Integer> productIds = new ArrayList<>();
        for (OrderDetailDto orderDetailDto: orderDetailDtos) {
            productIds.add(orderDetailDto.getProductId());
        }
        return menuService.getProductWithName(productIds, token);
    }

    @Override
    public int updateByPrimaryKey(OrderDetail row) {
        return orderDetailMapper.updateByPrimaryKey(row);
    }
}
