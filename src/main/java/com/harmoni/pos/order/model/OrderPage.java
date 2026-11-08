package com.harmoni.pos.order.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * One page of orders plus the cursor that asks for the next one.
 * <p>
 * The cursor is the id of the last order on this page rather than an offset. An offset
 * would repeat a row whenever a new order was confirmed between two requests, because every
 * later row shifts down by one; an id cannot.
 * <p>
 * {@code nextCursor} is {@code null} on the last page, which is how a client knows to stop
 * asking. It is only sent when the page came back full: a short page means there is nothing
 * behind it, and asking again would cost a round trip to learn what the response already
 * said.
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderPage {

    /**
     * The orders on this page, newest first.
     */
    private List<Order> orders;

    /**
     * Pass back as {@code beforeId} to get the next page; {@code null} when this is the last.
     */
    private Integer nextCursor;

    /**
     * Whether another page exists.
     */
    private boolean hasMore;
}
