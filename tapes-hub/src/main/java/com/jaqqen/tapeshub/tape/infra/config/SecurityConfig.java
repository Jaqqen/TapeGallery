package com.jaqqen.tapeshub.tape.infra.config;

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
 * Spring Security is on the classpath, so without an explicit chain every endpoint
 * is behind HTTP Basic and CSRF rejects every write.
 *
 * <p>The dev chain opens {@code /api/tapes/**} and allows the Vite dev server as a
 * CORS origin. Every other profile keeps the locked-down default, so the permissive
 * setup cannot leak into a deployed environment by accident.
 */
@Configuration
public class SecurityConfig {

    private static final String TAPES_API = "/api/tapes/**";
    private static final String WEB_PORTAL_DEV_ORIGIN = "http://localhost:5555";

    @Bean
    @Profile("dev")
    SecurityFilterChain devFilterChain(HttpSecurity http) {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.ignoringRequestMatchers(TAPES_API))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(TAPES_API).permitAll()
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

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
