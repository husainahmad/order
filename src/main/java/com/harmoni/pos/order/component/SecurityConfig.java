package com.harmoni.pos.order.component;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

/**
 * Security configuration that customizes Spring Security’s web filters.
 * <p>
 * This configuration tells Spring Security to completely ignore security filters for specified request patterns.
 * In this case, all endpoints under {@code /api/**} will bypass Spring Security.
 * </p>
 */
@Configuration
public class SecurityConfig {

    /**
     * Configures Spring Security to ignore requests to {@code /api/**}.
     * <p>
     * This means no security filters (e.g., authentication, authorization) will be applied
     * to these endpoints at all.
     * </p>
     *
     * @return a {@link WebSecurityCustomizer} instance for excluding certain paths from Spring Security
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/api/**");
    }
}
