package com.example.payments.domain.events;
import java.time.Instant;import java.util.UUID;
public record FundsReleasedEvent(UUID eventId, UUID paymentId, String reason, Instant occurredAt) implements DomainEvent { public String eventType(){return "FundsReleasedEvent";} }
