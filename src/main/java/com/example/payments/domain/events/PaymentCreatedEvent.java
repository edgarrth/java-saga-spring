package com.example.payments.domain.events;
import java.math.BigDecimal;import java.time.Instant;import java.util.UUID;
public record PaymentCreatedEvent(UUID eventId, UUID paymentId, BigDecimal amount, String currency, String mode, Instant occurredAt) implements DomainEvent { public String eventType(){return "PaymentCreatedEvent";} }
