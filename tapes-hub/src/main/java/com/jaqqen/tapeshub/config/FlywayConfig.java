package com.jaqqen.tapeshub.config;

import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class FlywayConfig {

    /**
     * Dev only. {@code repair()} clears failed migrations before retrying; it does not rescue a
     * checksum change on an already-applied migration, so editing V1 still means dropping the
     * volume ({@code docker compose -f tapes-hub/compose.yaml down -v}).
     */
    @Bean
    @Profile("dev")
    public FlywayMigrationStrategy cleanMigrateStrategy() {
        return flyway -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}
