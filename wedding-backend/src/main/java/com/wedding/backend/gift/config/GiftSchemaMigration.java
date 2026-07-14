package com.wedding.backend.gift.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class GiftSchemaMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public GiftSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("""
                DO $$
                BEGIN
                  IF EXISTS (
                    SELECT 1 FROM information_schema.columns
                    WHERE table_name = 'gift_items' AND column_name = 'product_url'
                  ) THEN
                    ALTER TABLE gift_items ALTER COLUMN product_url TYPE varchar(2048);
                  END IF;
                  IF EXISTS (
                    SELECT 1 FROM information_schema.columns
                    WHERE table_name = 'gift_items' AND column_name = 'image_url'
                  ) THEN
                    ALTER TABLE gift_items ALTER COLUMN image_url TYPE varchar(2048);
                  END IF;
                END $$;
                """);
    }
}
