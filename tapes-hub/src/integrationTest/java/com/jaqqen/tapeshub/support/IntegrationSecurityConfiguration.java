package com.jaqqen.tapeshub.support;

import jakarta.servlet.DispatcherType;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * The API still demands credentials here - these tests authenticate for real, so the request
 * travels the whole filter chain rather than skipping it. CSRF is exempt for /api/** exactly as
 * the shipped dev chain exempts it: the API is stateless and issues no token.
 *
 * <p>{@code FilterChainProxy} takes the first chain whose matcher matches, and unannotated chains
 * sort last, so {@code HIGHEST_PRECEDENCE} puts this ahead of
 * {@code SecurityConfig#defaultFilterChain}. It is scoped to {@code /api/**} with
 * {@code securityMatcher}, not {@code anyRequest()}: the {@code it} profile still activates the
 * shipped {@code defaultFilterChain} (it only turns off on the {@code dev} profile), and a second
 * chain that also claimed "any request" would make that one permanently unreachable - Spring
 * Security refuses to start rather than silently shadow it. Scoping this chain to the API leaves
 * {@code defaultFilterChain} to handle everything else, e.g. {@code /error}.
 * What the shipped chains actually do is the subject of {@code SecurityConfigIT}, which does not
 * import this configuration.
 */
@TestConfiguration(proxyBeanMethods = false)
public class IntegrationSecurityConfiguration {

    private static final String API = "/api/**";

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    SecurityFilterChain integrationFilterChain(HttpSecurity http) {
        return http
            .securityMatcher(API)
            .csrf(csrf -> csrf.ignoringRequestMatchers(API))
            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults())
            .build();
    }
}
