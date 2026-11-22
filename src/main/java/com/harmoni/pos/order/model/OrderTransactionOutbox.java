package com.harmoni.pos.order.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderTransactionOutbox {

    private Long id;
    private Integer orderId;
    private String payload;
    private String status;
    private int attempts;
    private String lastError;
    private Date createdAt;
    private Date sentAt;
}
