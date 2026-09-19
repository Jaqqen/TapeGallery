package com.jaqqen.tapeshub.config;

import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class LoggingConfig {

    /**
     * Spring Security's chain sits at {@code -100} and its {@code CorsFilter} returns without continuing
     * the chain on a rejected origin or a preflight. Running ahead of it is what makes those visible.
     */
    private static final int BEFORE_SECURITY_CHAIN = SecurityFilterProperties.DEFAULT_FILTER_ORDER - 1;

    @Bean
    @Profile("dev")
    public FilterRegistrationBean<CommonsRequestLoggingFilter> logFilter() {
        final CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000); // Max character limit
        // The message is otherwise just the method and path, which reads the same whether the call
        // came from the portal or from a rejected origin. Origin is a header, so it needs these on.
        filter.setIncludeHeaders(true);
        filter.setAfterMessagePrefix("[START] REQUEST DATA... \n");
        filter.setAfterMessageSuffix("\n[END] ...REQUEST DATA");

        final FilterRegistrationBean<CommonsRequestLoggingFilter> registration =
            new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/*");
        registration.setOrder(BEFORE_SECURITY_CHAIN);
        return registration;
    }
}
