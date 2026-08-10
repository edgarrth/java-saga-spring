package com.example.payments;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class KafkaConfigurationTest {

    @Test
    void springBootKafkaAutoConfigurationIsAvailable() throws Exception {
        Class<?> type = Class.forName("org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration");
        assertNotNull(type);
    }
}
