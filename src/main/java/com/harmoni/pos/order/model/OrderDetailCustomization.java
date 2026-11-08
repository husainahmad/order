package com.harmoni.pos.order.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDetailCustomization {

    private Long id;

    private Integer orderDetailId;

    /**
     * The SKU line these choices were made for, e.g. the Regular of a product also sold
     * as Big. Null on rows written before the column existed: those were attached to the
     * product line only and cannot be attributed to a variant after the fact.
     */
    private Integer orderDetailSkuId;

    private Integer customizationId;

    private String customizationName;

    private Date createdAt;

    private Date updatedAt;

    private Date deletedAt;

    /**
     * The line this choice belongs to while the order is being built.
     * <p>
     * Not persisted and not serialised: it exists only so the generated
     * {@code order_detail_skus.id} can be copied onto {@link #orderDetailSkuId} once the
     * lines have been inserted. Reaching it through the id would mean re-deriving which
     * SKU a choice came from, which is exactly the information the id is here to keep.
     */
    @JsonIgnore
    private transient OrderDetailSku orderDetailSku;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<OrderDetailCustomizationOption> options;
}
