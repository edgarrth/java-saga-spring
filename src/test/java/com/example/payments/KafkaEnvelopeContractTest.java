package com.example.payments;

import com.example.payments.infrastructure.adapters.out.messaging.KafkaEventEnvelope;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KafkaEnvelopeContractTest {
    @Test
    void envelopeKeepsEventTypeAndSerializedDomainPayload() {
        var envelope = new KafkaEventEnvelope("PaymentCreatedEvent", "{\"paymentId\":\"123\"}");
        assertEquals("PaymentCreatedEvent", envelope.eventType());
        assertEquals("{\"paymentId\":\"123\"}", envelope.payload());
    }
}
