package com.harmoni.pos.order.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDto {
    private Integer id;
    private String name;
    private Integer categoryId;
    private String description;
    private CategoryDto category;
    @JsonProperty("skus")
    private List<SkuDto> skus;
    private Boolean deleted;
    private Date createdAt;
    private Date updatedAt;
    private Date deletedAt;

}