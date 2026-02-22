package com.harmoni.pos.order.component;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * Configuration class for internationalization (i18n) message sources.
 * <p>
 * This configures a {@link MessageSource} bean that loads messages from
 * {@code messages.properties} files located in the classpath. It supports reloading
 * and UTF-8 encoding.
 * </p>
 */
@Configuration
public class MessageSourceConfig {

    /**
     * Defines a {@link MessageSource} bean that loads messages from {@code classpath:messages}.
     * <p>
     * This is used by Spring for internationalization and custom validation messages.
     * </p>
     *
     * @return a configured {@link ReloadableResourceBundleMessageSource} instance
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();

        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");

        return messageSource;
    }
}
