package com.wedding.backend.guest.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class GuestSchemaMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public GuestSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute(
                "ALTER TABLE guests ADD COLUMN IF NOT EXISTS responded boolean NOT NULL DEFAULT false"
        );
        jdbcTemplate.execute(
                "ALTER TABLE guests ADD COLUMN IF NOT EXISTS pre_registered boolean NOT NULL DEFAULT false"
        );
    }
}
