package com.harmoni.pos.order.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.harmoni.pos.order.model.OrderDetailSku;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SkuDto {

    private Integer id;
    private String name;
    private Integer productId;
    private ProductDto product;
    private Date createdAt;
    private Date updatedAt;
    private Date deletedAt;
    private Boolean active;
    private Boolean deleted;
    @JsonProperty("tierPrices")
    private List<SkuTierPriceDto> skuTierPrices;
    @JsonProperty("tierPrice")
    private SkuTierPriceDto skuTierPrice;
}