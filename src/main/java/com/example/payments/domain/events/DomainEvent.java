package com.example.payments.domain.events;
import java.time.Instant;
import java.util.UUID;
public interface DomainEvent {
    UUID eventId();
    UUID paymentId();
    Instant occurredAt();
    String eventType();
}
