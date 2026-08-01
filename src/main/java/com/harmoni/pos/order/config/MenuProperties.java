package com.harmoni.pos.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "menu.url")
public class MenuProperties {

    private String product;
    private String skuPrice;
    private String store;
    private String user;
}
