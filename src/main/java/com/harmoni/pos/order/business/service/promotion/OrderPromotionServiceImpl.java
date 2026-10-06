package com.harmoni.pos.order.business.service.promotion;

import com.harmoni.pos.order.business.service.menu.PromotionMenuService;
import com.harmoni.pos.order.exception.BusinessBadRequestException;
import com.harmoni.pos.order.model.CalculatedOrder;
import com.harmoni.pos.order.model.Order;
import com.harmoni.pos.order.model.OrderDetail;
import com.harmoni.pos.order.model.OrderDetailSku;
import com.harmoni.pos.order.model.dto.StoreDto;
import com.harmoni.pos.order.model.dto.UserDto;
import com.harmoni.pos.order.model.dto.pricing.AppliedDiscountDto;
import com.harmoni.pos.order.model.dto.pricing.CartLineDto;
import com.harmoni.pos.order.model.dto.pricing.OrderItemDiscountSnapshotDto;
import com.harmoni.pos.order.model.dto.pricing.PricedLineDto;
import com.harmoni.pos.order.model.dto.pricing.PromotionContextDto;
import com.harmoni.pos.order.model.dto.pricing.PromotionPriceRequestDto;
import com.harmoni.pos.order.model.dto.pricing.PromotionPriceResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Prices an order's lines by asking the menu service, and writes the answer onto the
 * order.
 *
 * @author husainahmad
 */
@RequiredArgsConstructor
@Service("orderPromotionService")
@Slf4j
public class OrderPromotionServiceImpl implements OrderPromotionService {

    private final PromotionMenuService promotionMenuService;

    /**
     * {@inheritDoc}
     */
    @Override
    public CalculatedOrder applyPromotions(Order order, UserDto userDto, String username) {
        List<SaleLine> lines = flattenLines(order);
        if (lines.isEmpty()) {
            order.setDiscountTotal(BigDecimal.ZERO);
            return new CalculatedOrder(order, List.of(), BigDecimal.ZERO);
        }

        PromotionPriceResponseDto priced = promotionMenuService.price(toPriceRequest(order, lines, userDto), username);
        List<PricedLineDto> pricedLines = priced.getLines();
        if (pricedLines == null || pricedLines.size() != lines.size()) {
            log.error("Menu service priced {} of {} lines for order by {}, refusing to guess the rest",
                    pricedLines == null ? 0 : pricedLines.size(), lines.size(), username);
            throw new BusinessBadRequestException("exception.promotionPrice.unavailable", null);
        }

        List<CalculatedOrder.LineDiscount> lineDiscounts = new ArrayList<>(lines.size());
        BigDecimal totalDiscount = BigDecimal.ZERO;
        for (int i = 0; i < lines.size(); i++) {
            PricedLineDto pricedLine = pricedLines.get(i);
            warnOnGrossMismatch(lines.get(i).sku(), pricedLine, username);

            BigDecimal lineDiscount = orZero(pricedLine.getDiscountAmount());
            lines.get(i).sku().setDiscountAmount(lineDiscount);
            totalDiscount = totalDiscount.add(lineDiscount);
            lineDiscounts.add(new CalculatedOrder.LineDiscount(null, toPromotionDiscounts(pricedLine)));
        }

        rollUpToOrderDetails(order);
        order.setDiscountTotal(totalDiscount);
        log.debug("Order by {} priced, {} promotion discount across {} line(s)", username, totalDiscount, lines.size());

        return new CalculatedOrder(order, lineDiscounts, totalDiscount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<OrderItemDiscountSnapshotDto> toSnapshots(CalculatedOrder calculatedOrder) {
        List<SaleLine> lines = flattenLines(calculatedOrder.order());
        if (lines.size() != calculatedOrder.lineDiscounts().size()) {
            log.error("Order now has {} line(s) but was priced as {}, cannot build the audit trail",
                    lines.size(), calculatedOrder.lineDiscounts().size());
            throw new BusinessBadRequestException("exception.promotionPrice.unavailable", null);
        }

        List<OrderItemDiscountSnapshotDto> snapshots = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            Integer orderItemId = lines.get(i).sku().getId();
            if (orderItemId == null) {
                log.error("Order line {} has no id after insert, cannot record its promotion", i);
                throw new BusinessBadRequestException("exception.promotionPrice.unavailable", null);
            }
            for (CalculatedOrder.PromotionDiscount discount : calculatedOrder.lineDiscounts().get(i).discounts()) {
                OrderItemDiscountSnapshotDto snapshot = new OrderItemDiscountSnapshotDto();
                snapshot.setOrderItemId(orderItemId.longValue());
                snapshot.setPromotionId(discount.promotionId());
                snapshot.setPromotionCode(discount.promotionCode());
                snapshot.setPromotionName(discount.promotionName());
                snapshot.setDiscountType(discount.discountType());
                snapshot.setDiscountValue(discount.discountValue());
                snapshot.setDiscountAmount(discount.discountAmount());
                snapshots.add(snapshot);
            }
        }
        return snapshots;
    }

    /**
     * Every sale line on the order, in a single stable sequence, each still paired with the
     * product line it belongs to.
     * <p>
     * The promotion engine prices a flat list of lines, but the order is a two level tree:
     * one entry per product, each holding its SKU variants. This is the one place that
     * flattening is defined, and both pricing and audit recording go through it, so the
     * index a discount was priced against is the same index it is recorded at.
     *
     * @param order the order whose lines to collect
     * @return the sale lines, in product then SKU order
     */
    private static List<SaleLine> flattenLines(Order order) {
        List<SaleLine> lines = new ArrayList<>();
        if (order == null || ObjectUtils.isEmpty(order.getOrderDetails())) {
            return lines;
        }
        for (OrderDetail detail : order.getOrderDetails()) {
            if (ObjectUtils.isEmpty(detail.getOrderDetailSkus())) {
                continue;
            }
            for (OrderDetailSku sku : detail.getOrderDetailSkus()) {
                lines.add(new SaleLine(detail, sku));
            }
        }
        return lines;
    }

    private static PromotionPriceRequestDto toPriceRequest(Order order,
                                                           List<SaleLine> lines,
                                                           UserDto userDto) {
        PromotionPriceRequestDto request = new PromotionPriceRequestDto();
        request.setLines(toCartLines(lines));
        request.setContext(toContext(userDto));
        return request;
    }

    private static List<CartLineDto> toCartLines(List<SaleLine> lines) {
        List<CartLineDto> cartLines = new ArrayList<>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            OrderDetailSku sku = lines.get(i).sku();
            OrderDetail parent = lines.get(i).parent();

            CartLineDto dto = new CartLineDto();
            dto.setLineIndex(i);
            dto.setProductId(parent.getProductId());
            dto.setSkuId(sku.getSkuId());
            dto.setCategoryId(parent.getCategoryId());
            dto.setUnitPrice(sku.getPrice());
            dto.setQuantity(sku.getQuantity());
            cartLines.add(dto);
        }
        return cartLines;
    }

    private static PromotionContextDto toContext(UserDto userDto) {
        StoreDto store = userDto == null ? null : userDto.getStore();
        PromotionContextDto context = new PromotionContextDto();
        if (store != null) {
            context.setStoreId(store.getId() == null ? null : store.getId().longValue());
            context.setZone(store.getTimeZone());
        }
        return context;
    }

    /**
     * Sums the SKU discounts onto their parent product lines, so a product carrying two
     * discounted variants reports the combined saving.
     */
    private static void rollUpToOrderDetails(Order order) {
        for (OrderDetail detail : order.getOrderDetails()) {
            BigDecimal detailDiscount = BigDecimal.ZERO;
            if (!ObjectUtils.isEmpty(detail.getOrderDetailSkus())) {
                for (OrderDetailSku sku : detail.getOrderDetailSkus()) {
                    detailDiscount = detailDiscount.add(orZero(sku.getDiscountAmount()));
                }
            }
            detail.setDiscountAmount(detailDiscount);
        }
    }

    private static List<CalculatedOrder.PromotionDiscount> toPromotionDiscounts(PricedLineDto pricedLine) {
        if (pricedLine.getDiscounts() == null || pricedLine.getDiscounts().isEmpty()) {
            return List.of();
        }
        List<CalculatedOrder.PromotionDiscount> discounts = new ArrayList<>(pricedLine.getDiscounts().size());
        for (AppliedDiscountDto discount : pricedLine.getDiscounts()) {
            discounts.add(new CalculatedOrder.PromotionDiscount(
                    discount.getPromotionId(),
                    discount.getPromotionCode(),
                    discount.getPromotionName(),
                    discount.getDiscountType(),
                    discount.getDiscountValue(),
                    orZero(discount.getDiscountAmount())));
        }
        return discounts;
    }

    /**
     * The engine rounds each line to the money scale while the order multiplies prices out
     * unrounded, so the two can differ by a fraction of a cent on an awkward unit price.
     * That is not worth failing a sale over, but it does mean the discount was computed on
     * a marginally different base, which is worth knowing about.
     */
    private static void warnOnGrossMismatch(OrderDetailSku line, PricedLineDto pricedLine, String username) {
        if (line.getAmount() == null || pricedLine.getGrossAmount() == null) {
            return;
        }
        if (line.getAmount().compareTo(pricedLine.getGrossAmount()) != 0) {
            log.warn("Line amount {} and the priced gross {} disagree for order by {}",
                    line.getAmount(), pricedLine.getGrossAmount(), username);
        }
    }

    private static BigDecimal orZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * A SKU line together with the product line it belongs to.
     * <p>
     * The engine needs the product and category to match a promotion's targets, but the
     * order only stores them on the parent, so the two have to travel as one.
     *
     * @param parent the product line carrying the product and category
     * @param sku    the sale line being priced
     */
    private record SaleLine(OrderDetail parent, OrderDetailSku sku) {
    }
}
