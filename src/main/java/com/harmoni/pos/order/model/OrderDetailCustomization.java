package com.harmoni.pos.order.model;

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

    private Integer customizationId;

    private String customizationName;

    private Date createdAt;

    private Date updatedAt;

    private Date deletedAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<OrderDetailCustomizationOption> options;
}
