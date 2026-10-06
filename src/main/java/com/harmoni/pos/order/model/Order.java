package com.harmoni.pos.order.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.harmoni.pos.order.model.dto.StoreDto;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Order {
    private Integer id;
    private String orderNo;
    private BigDecimal subTotal;
    private BigDecimal discountTotal;
    private BigDecimal grandTotal;
    private BigDecimal serviceChargeTotal;

    private BigDecimal taxTotal;
    private Integer storeId;
    private StoreDto store;
    /**
     * The customer this sale was made to, or {@code -1} for a walk-in with no customer.
     * That convention predates {@link OrderCustomer}, which uses null for the same case.
     */
    private Integer customerId;
    private String customerName;
    private String customerPhone;
    private Integer storeServiceTypesId;
    private List<OrderDetail> orderDetails;
    private OrderStatus status;
    private OrderPayment orderPayment;
    private String remark;
    private String idempotencyKey;
    private Date createdAt;
    private Date updatedAt;
}