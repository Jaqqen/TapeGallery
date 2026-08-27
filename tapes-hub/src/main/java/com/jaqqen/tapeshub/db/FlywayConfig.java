package com.jaqqen.tapeshub.db;

import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class FlywayConfig {
    @Bean
    @Profile("dev")
    public FlywayMigrationStrategy cleanMigrateStrategy() {
        return flyway -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}
