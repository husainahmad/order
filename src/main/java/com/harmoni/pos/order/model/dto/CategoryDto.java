package com.harmoni.pos.order.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryDto {

    private Integer id;
    private String name;
    private String description;
    private Integer brandId;
    private Date createdAt;
    private Date updatedAt;
    private Date deletedAt;

}