package com.example.payments.infrastructure.adapters.out.messaging;

import java.time.Instant;

public record KafkaDeadLetter(String originalPayload, String reason, Instant failedAt) {
}
