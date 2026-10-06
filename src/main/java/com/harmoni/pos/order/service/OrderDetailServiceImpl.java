package com.harmoni.pos.order.service;

import com.harmoni.pos.order.business.service.menu.MenuService;
import com.harmoni.pos.order.exception.BusinessBadRequestException;
import com.harmoni.pos.order.mapper.OrderDetailMapper;
import com.harmoni.pos.order.model.OrderDetail;
import com.harmoni.pos.order.model.OrderDetailSku;
import com.harmoni.pos.order.model.OrderDetailCustomization;
import com.harmoni.pos.order.model.OrderDetailCustomizationOption;
import com.harmoni.pos.order.model.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

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
    private final OrderDetailCustomizationService orderDetailCustomizationService;

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
        List<OrderDetailCustomization> customizations = new ArrayList<>();
        Date now = new Date(System.currentTimeMillis());
        for (OrderDetail orderDetail : orderDetails) {
            orderDetail.setCreatedAt(now);
            if (!ObjectUtils.isEmpty(orderDetail.getCustomizations())) {
                for (OrderDetailCustomization customization : orderDetail.getCustomizations()) {
                    customization.setOrderDetailId(orderDetail.getId());
                    customization.setCreatedAt(now);
                    customizations.add(customization);
                }
            }
            for (OrderDetailSku orderDetailSku : orderDetail.getOrderDetailSkus()) {
                orderDetailSku.setOrderDetailId(orderDetail.getId());
                orderDetailSku.setCreatedAt(now);
                orderDetailSkus.add(orderDetailSku);
            }
        }
        orderDetailSkuService.insertBulk(orderDetailSkus);
        orderDetailCustomizationService.record(customizations);
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
     * Populates a list of {@link OrderDetail} objects from the given DTOs, taking every
     * name and price from the menu service's answer.
     * <p>
     * The basket is sent to the menu service in one request and comes back confirmed, so
     * nothing printed on the receipt originates in the request: the product, category, SKU
     * and customization names, and the prices at this store's tier, are all read from the
     * catalogue. The client contributes ids, counts and quantities, and nothing it could
     * profit from.
     * <p>
     * What this service still does is the arithmetic, because it is the side that knows how
     * the line combines: a customization's charge is its per unit price times how many were
     * chosen times how many units of the line were ordered, and the line amount is that
     * added to the SKU price times the quantity. The menu service prices the parts and
     * deliberately does not total them.
     *
     * @param orderDetailDtos list of order detail DTOs
     * @param storeId         the store the sale is being made at, which decides the tier
     * @param username        username forwarded to the menu service
     * @return a list of populated {@link OrderDetail} entities
     * @throws BusinessBadRequestException if the menu service did not confirm the basket
     */
    @Override
    public List<OrderDetail> populateOrderDetail(List<OrderDetailDto> orderDetailDtos, Integer storeId,
                                                 String username) {
        if (ObjectUtils.isEmpty(orderDetailDtos)) {
            return new ArrayList<>();
        }

        List<RequestedLine> requested = flatten(orderDetailDtos);
        ValidateOrderItemsResponseDto confirmed = menuService.validateOrderItems(
                toValidationRequest(storeId, requested), username);

        if (confirmed == null || !confirmed.isValid()) {
            // The menu service rejects a bad basket outright rather than answering with a
            // partial list, so an unconfirmed answer means nothing about the basket is
            // trustworthy. Continuing on a best guess would sell lines at prices nobody
            // checked.
            throw new BusinessBadRequestException("exception.validateOrderItems.unavailable", null);
        }

        return assemble(orderDetailDtos, requested, confirmed.getItems());
    }

    /**
     * Flattens the request into one entry per SKU line, which is the shape the menu service
     * validates: it prices a product and SKU pair, and a product may legitimately be ordered
     * as several SKUs on the same line.
     *
     * @param orderDetailDtos the incoming basket
     * @return each SKU line, remembering which {@link OrderDetail} it belongs to
     */
    private static List<RequestedLine> flatten(List<OrderDetailDto> orderDetailDtos) {
        List<RequestedLine> lines = new ArrayList<>();
        for (OrderDetailDto orderDetailDto : orderDetailDtos) {
            if (ObjectUtils.isEmpty(orderDetailDto.getOrderDetailSkus())) {
                continue;
            }
            for (OrderDetailSkuDto skuDto : orderDetailDto.getOrderDetailSkus()) {
                lines.add(new RequestedLine(orderDetailDto, skuDto));
            }
        }
        return lines;
    }

    /**
     * Translates the flattened lines into the menu service's request.
     *
     * @param storeId the store the sale is being made at
     * @param lines   the SKU lines being ordered
     * @return the request, carrying ids, quantities and chosen options and nothing else
     */
    private static ValidateOrderItemsRequestDto toValidationRequest(Integer storeId, List<RequestedLine> lines) {
        ValidateOrderItemsRequestDto request = new ValidateOrderItemsRequestDto();
        request.setStoreId(storeId);
        request.setItems(lines.stream()
                .map(RequestedLine::toRequest)
                .collect(Collectors.toList()));
        return request;
    }

    /**
     * Rebuilds the order lines from the confirmed answer.
     * <p>
     * Confirmed lines are matched back to requests position by position, because the menu
     * service answers with one entry per line, in the order they were sent, and a basket may
     * legitimately order the same product and SKU twice. The ids are checked on the way
     * through anyway: a reordered or short answer would otherwise be recorded against the
     * wrong line, which on a receipt means the wrong price next to the wrong name.
     *
     * @param orderDetailDtos the incoming basket, in the order it was sent
     * @param requested       the flattened lines the request was built from
     * @param confirmed       the menu service's confirmed lines
     * @return the populated order lines
     * @throws BusinessBadRequestException if the answer does not line up with the request
     */
    private static List<OrderDetail> assemble(List<OrderDetailDto> orderDetailDtos, List<RequestedLine> requested,
                                              List<ValidatedOrderItemDto> confirmed) {
        List<ValidatedOrderItemDto> answers = confirmed == null ? List.of() : confirmed;
        if (answers.size() != requested.size()) {
            throw new BusinessBadRequestException("exception.validateOrderItems.unavailable", null);
        }

        List<ConfirmedLine> pairs = new ArrayList<>(requested.size());
        for (int index = 0; index < requested.size(); index++) {
            RequestedLine line = requested.get(index);
            ValidatedOrderItemDto answer = answers.get(index);
            if (!line.matches(answer)) {
                throw new BusinessBadRequestException("exception.validateOrderItems.unavailable", null);
            }
            pairs.add(new ConfirmedLine(line, answer));
        }

        List<OrderDetail> orderDetails = new ArrayList<>(orderDetailDtos.size());
        for (OrderDetailDto orderDetailDto : orderDetailDtos) {
            List<ConfirmedLine> mine = pairs.stream()
                    .filter(pair -> pair.line().orderDetailDto() == orderDetailDto)
                    .collect(Collectors.toList());
            if (mine.isEmpty()) {
                continue;
            }
            orderDetails.add(toOrderDetail(mine));
        }
        return orderDetails;
    }

    /**
     * Builds one product level {@link OrderDetail} from every confirmed SKU line that belongs
     * to it, totalling them.
     * <p>
     * The product's own fields are taken from the first confirmed line, which is the
     * catalogue's answer for that product. All the lines under it were confirmed against the
     * same product, so they agree on it.
     *
     * @param lines the confirmed lines under one product
     * @return the populated product line
     */
    private static OrderDetail toOrderDetail(List<ConfirmedLine> lines) {
        ValidatedOrderItemDto product = lines.get(0).answer();

        List<OrderDetailSku> skus = new ArrayList<>(lines.size());
        BigDecimal amount = BigDecimal.ZERO;
        for (ConfirmedLine line : lines) {
            OrderDetailSku sku = toOrderDetailSku(line.line(), line.answer());
            skus.add(sku);
            amount = amount.add(sku.getAmount());
        }

        List<OrderDetailCustomization> customizations = new ArrayList<>();
        for (ConfirmedLine line : lines) {
            customizations.addAll(toOrderCustomizations(line.answer().getCustomizations(), line.line().skuDto().getQuantity()));
        }
        // merge groups by customizationId
        Map<Integer, List<OrderDetailCustomization>> grouped = customizations.stream()
                .collect(Collectors.groupingBy(c -> c.getCustomizationId() == null ? -1 : c.getCustomizationId()));
        List<OrderDetailCustomization> merged = grouped.entrySet().stream()
                .map(e -> {
                    List<OrderDetailCustomization> list = e.getValue();
                    OrderDetailCustomization g = new OrderDetailCustomization()
                            .setCustomizationId(list.get(0).getCustomizationId())
                            .setCustomizationName(list.get(0).getCustomizationName());
                    List<OrderDetailCustomizationOption> opts = new ArrayList<>();
                    for (OrderDetailCustomization c : list) {
                        if (c.getOptions() != null) {
                            opts.addAll(c.getOptions());
                        }
                    }
                    g.setOptions(opts);
                    return g;
                }).collect(Collectors.toList());

        BigDecimal surchargeTotal = merged.stream()
                .filter(g -> g.getOptions() != null)
                .flatMap(g -> g.getOptions().stream())
                .map(OrderDetailCustomizationOption::getAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderDetail()
                .setProductId(product.getProductId())
                .setProductName(product.getProductName())
                .setCategoryId(product.getCategoryId())
                .setCategoryName(product.getCategoryName())
                .setOrderDetailSkus(skus)
                .setCustomizations(merged.isEmpty() ? null : merged)
                .setAmount(amount.add(surchargeTotal));
    }

    /**
     * Builds one {@link OrderDetailSku} from the line that was asked for and the line the
     * menu service confirmed.
     * <p>
     * The amount stays the gross SKU charge. Any customization surcharge is totalled at
     * the {@link OrderDetail} level, where the customization snapshots are attached.
     *
     * @param line   what was asked for
     * @param answer what the catalogue says about it
     * @return the stored line, priced from the catalogue
     */
    private static OrderDetailSku toOrderDetailSku(RequestedLine line, ValidatedOrderItemDto answer) {
        // The line's name and its price are the catalogue's, not the request's. The product
        // name has always been resolved this way; the SKU name used to be taken from the
        // client and stored, which meant a client could label a line anything and have it
        // printed that way on the receipt.
        BigDecimal price = zeroIfNull(answer.getSkuPrice());
        int quantity = answer.getQuantity() == null ? line.skuDto().getQuantity() : answer.getQuantity();

        OrderDetailSku orderDetailSku = new OrderDetailSku()
                .setSkuId(answer.getSkuId())
                .setSkuName(answer.getSkuName() == null ? "" : answer.getSkuName())
                .setQuantity(quantity)
                .setPrice(price);

        orderDetailSku.setAmount(price.multiply(BigDecimal.valueOf(quantity)));
        return orderDetailSku;
    }

    /**
     * Copies the confirmed choices into the snapshot rows that will be stored against the
     * line, working out what each one charged.
     * <p>
     * The menu service supplies the name and the per unit price but no total, because
     * multiplying is the order service's arithmetic: it knows the quantity on the parent
     * line, and it is the side that applies promotions afterwards. A choice asked for three
     * times on a line of two therefore costs six times the option's price, not three.
     *
     * @param confirmed the confirmed choices, empty when the line had none
     * @param quantity  the number of units on the line the choices belong to
     * @return the snapshot rows
     */
    private static List<OrderDetailCustomization> toOrderCustomizations(List<ValidatedCustomizationDto> confirmed,
                                                                       int quantity) {
        if (ObjectUtils.isEmpty(confirmed)) {
            return new ArrayList<>();
        }
        return confirmed.stream()
                .collect(Collectors.groupingBy(c -> c.getCustomizationId() == null ? -1 : c.getCustomizationId()))
                .entrySet().stream()
                .map(entry -> {
                    List<ValidatedCustomizationDto> choices = entry.getValue();
                    OrderDetailCustomization group = new OrderDetailCustomization()
                            .setCustomizationId(choices.get(0).getCustomizationId())
                            .setCustomizationName(choices.get(0).getCustomizationName());
                    List<OrderDetailCustomizationOption> options = choices.stream()
                            .map(choice -> new OrderDetailCustomizationOption()
                                    .setCustomizationOptionsId(choice.getCustomizationOptionId())
                                    .setCustomizationOptionName(choice.getOptionName())
                                    .setPrice(choice.getPrice())
                                    .setQuantity(choice.getQuantity())
                                    .setAmount(zeroIfNull(choice.getPrice())
                                            .multiply(BigDecimal.valueOf(choice.getQuantity() == null ? 1 : choice.getQuantity()))
                                            .multiply(BigDecimal.valueOf(quantity))))
                            .collect(Collectors.toList());
                    group.setOptions(options);
                    return group;
                })
                .collect(Collectors.toList());
    }

    private static BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * One SKU line of the basket, flattened out of the request and remembering which product
     * line it came from.
     *
     * @param orderDetailDto the product line this belongs to
     * @param skuDto         the SKU line as the client sent it
     */
    private record RequestedLine(OrderDetailDto orderDetailDto, OrderDetailSkuDto skuDto) {

        /**
         * States this line for the menu service: ids, quantity and the chosen options.
         *
         * @return the request entry
         */
        ValidateOrderItemRequestDto toRequest() {
            ValidateOrderItemRequestDto item = new ValidateOrderItemRequestDto();
            item.setProductId(orderDetailDto.getProductId());
            item.setSkuId(skuDto.getSkuId());
            item.setQuantity(skuDto.getQuantity());
            item.setCustomizations(skuDto.getCustomizations() == null
                    ? new ArrayList<>()
                    : skuDto.getCustomizations());
            return item;
        }

        /**
         * Whether the menu service answered about the line that was actually asked for.
         *
         * @param answer one confirmed line
         * @return true when it names the same product and SKU
         */
        boolean matches(ValidatedOrderItemDto answer) {
            return answer != null
                    && Objects.equals(orderDetailDto.getProductId(), answer.getProductId())
                    && Objects.equals(skuDto.getSkuId(), answer.getSkuId());
        }
    }

    /**
     * A requested line together with the menu service's answer about it.
     *
     * @param line   what was asked for
     * @param answer what the catalogue confirmed
     */
    private record ConfirmedLine(RequestedLine line, ValidatedOrderItemDto answer) {
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
