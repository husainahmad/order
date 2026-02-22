package com.harmoni.pos.order.http.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestAPIResponse {

    @Builder.Default
    private long timeStamp = System.currentTimeMillis();

    @Builder.Default
    private int httpStatus = HttpStatus.CREATED.value();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object error;
}
