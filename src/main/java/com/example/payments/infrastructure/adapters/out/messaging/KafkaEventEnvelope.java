package com.example.payments.infrastructure.adapters.out.messaging;

/**
 * Stable Kafka wire contract. The domain event JSON is kept as payload while
 * eventType identifies the concrete event that must be deserialized.
 */
public record KafkaEventEnvelope(String eventType, String payload) {
}
