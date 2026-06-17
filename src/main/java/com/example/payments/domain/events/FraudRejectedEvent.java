package com.example.payments.domain.events;
import java.time.Instant;import java.util.UUID;
public record FraudRejectedEvent(UUID eventId, UUID paymentId, String reason, Instant occurredAt) implements DomainEvent { public String eventType(){return "FraudRejectedEvent";} }
