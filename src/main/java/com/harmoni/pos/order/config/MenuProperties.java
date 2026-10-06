package com.harmoni.pos.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "menu.url")
public class MenuProperties {

    private String store;
    private String user;

/**
     * Menu endpoint that prices a basket of sale lines against the promotion catalogue.
     */
    private String promotionPrice;

    /**
     * Menu endpoint that confirms a basket and names and prices every line in it.
     * <p>
     * This is the call that populates an order. It returns the product, category, SKU and
     * customization names and prices as the catalogue currently has them, so the order
     * service can store the sale as it was sold instead of looking each of them up again.
     */
    private String validateOrderItems;
}
