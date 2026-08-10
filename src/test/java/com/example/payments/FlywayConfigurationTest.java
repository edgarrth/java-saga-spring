package com.example.payments;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlywayConfigurationTest {

    @Test
    void springBootFlywayAutoConfigurationIsAvailable() throws Exception {
        Class<?> type = Class.forName("org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration");
        assertNotNull(type);
    }

    @Test
    void initialMigrationContainsAllJpaTables() throws IOException {
        var resource = getClass().getClassLoader().getResourceAsStream("db/migration/V1__init.sql");
        assertNotNull(resource, "V1__init.sql must exist on the application classpath");

        String sql;
        try (resource) {
            sql = new String(resource.readAllBytes(), StandardCharsets.UTF_8).toLowerCase();
        }

        assertTrue(sql.contains("create table payments"));
        assertTrue(sql.contains("create table saga_instances"));
        assertTrue(sql.contains("create table event_store"));
        assertTrue(sql.contains("create table outbox_events"));
    }
}
