package com.jaqqen.tapeshub.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security is on the classpath, so without an explicit chain every endpoint is behind HTTP
 * Basic and CSRF rejects every write.
 *
 * <p>The dev chain opens {@code /api/**} and allows the dev server as a CORS origin.
 */
@Configuration
public class SecurityConfig {

    private static final String API = "/api/**";
    private static final String WEB_PORTAL_DEV_ORIGIN = "http://localhost:5555";

    @Bean
    @Profile("dev")
    SecurityFilterChain devFilterChain(HttpSecurity http) {
        return http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.ignoringRequestMatchers(API))
            .authorizeHttpRequests(auth -> auth
                // Without this an unhandled failure on an open endpoint comes back as a 401 from the
                // error dispatch, hiding the actual status behind a login prompt.
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers(API).permitAll()
                .anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults())
            .build();
    }

    @Bean
    @Profile("!dev")
    SecurityFilterChain defaultFilterChain(HttpSecurity http) {
        return http
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults())
            .build();
    }

    @Bean
    @Profile("dev")
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(WEB_PORTAL_DEV_ORIGIN));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(API, config);
        return source;
    }
}
