package com.harmoni.pos.order.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
    private Integer id;
    private Integer authId;
    private String username;
    private Integer storeId;
    private StoreDto store;

    private Date createdAt;
    private Date updatedAt;
    private Date deletedAt;
}
