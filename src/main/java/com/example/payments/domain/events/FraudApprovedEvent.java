package com.example.payments.domain.events;
import java.time.Instant;import java.util.UUID;
public record FraudApprovedEvent(UUID eventId, UUID paymentId, Instant occurredAt) implements DomainEvent { public String eventType(){return "FraudApprovedEvent";} }
