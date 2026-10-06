package com.harmoni.pos.order.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * The customer an order was sold to, captured as they were at the time of the sale.
 * <p>
 * This is a snapshot, not a reference. The customer service owns customer identity in
 * its own database, and it soft deletes rather than removes, so a customer's name and
 * phone can both change after an order was taken. Copying the two identifying fields
 * onto the order is what keeps a historic receipt readable and keeps customer history
 * intact after the customer record is gone. The same reasoning applies to the promotion
 * discount audit rows.
 * <p>
 * {@code customerId} is {@code null} for a walk-in sale, where there is no customer to
 * refer to. Note this differs from {@link Order#getCustomerId()}, which uses the
 * existing {@code -1} convention for the same case; keeping them different means a
 * history lookup needs no filter to exclude walk-ins.
 *
 * @author husainahmad
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderCustomer {

    /**
     * The unique identifier of this snapshot row.
     */
    private Long id;

    /**
     * The order this snapshot belongs to, unique across the table.
     */
    private Integer orderId;

    /**
     * The customer in the customer service, {@code null} for a walk-in sale. A
     * reference only: the constraint cannot be enforced across databases.
     */
    private Long customerId;

    /**
     * The customer name as it was at the time of the sale.
     */
    private String customerName;

    /**
     * The customer phone number as it was at the time of the sale.
     */
    private String customerPhone;

    /**
     * When the order was confirmed.
     */
    private Date createdAt;
}
