package com.harmoni.pos.order.model.dto.pricing;

import lombok.Data;

/**
 * The sale context a price is requested in, as sent to the menu service.
 * <p>
 * The store and its time zone come from the authenticated operator's store record. The
 * zone decides whether a promotion is live, so a store's local wall clock is what gets
 * sent rather than the order server's.
 *
 * @author husainahmad
 */
@Data
public class PromotionContextDto {

    private Long storeId;

    private Long chainId;

    private Long brandId;

    private Long tenantId;

    /**
     * The store's IANA time zone, for example {@code Australia/Sydney}.
     */
    private String zone;
}
