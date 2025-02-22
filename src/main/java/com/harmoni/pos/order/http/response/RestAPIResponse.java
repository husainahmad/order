package com.harmoni.pos.order.http.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class RestAPIResponse {
    @Builder.Default
    private long timeStamp = System.currentTimeMillis();
    @Builder.Default
    private int httpStatus = HttpStatus.CREATED.value();
    @Builder.Default
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object data = HttpStatus.CREATED;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object error;


}
