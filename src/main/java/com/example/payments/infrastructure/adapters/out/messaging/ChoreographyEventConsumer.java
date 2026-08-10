package com.example.payments.infrastructure.adapters.out.messaging;

import com.example.payments.application.saga.choreography.PaymentChoreographySaga;
import com.example.payments.domain.events.FraudApprovedEvent;
import com.example.payments.domain.events.FraudRejectedEvent;
import com.example.payments.domain.events.FundsReservedEvent;
import com.example.payments.domain.events.PaymentCompletedEvent;
import com.example.payments.domain.events.PaymentCreatedEvent;
import com.example.payments.domain.events.SettlementCapturedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Component
public class ChoreographyEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(ChoreographyEventConsumer.class);
    private static final long KAFKA_ACK_TIMEOUT_SECONDS = 10;

    private final JsonMapper mapper;
    private final PaymentChoreographySaga saga;
    private final KafkaTemplate<String, String> kafka;
    private final String deadLetterTopic;

    public ChoreographyEventConsumer(JsonMapper mapper,
                                     PaymentChoreographySaga saga,
                                     KafkaTemplate<String, String> kafka,
                                     @Value("${app.topics.payment-events-dlt}") String deadLetterTopic) {
        this.mapper = mapper;
        this.saga = saga;
        this.kafka = kafka;
        this.deadLetterTopic = deadLetterTopic;
    }

    @KafkaListener(topics = "${app.topics.payment-events}")
    public void onMessage(String message) throws Exception {
        KafkaEventEnvelope envelope = parseEnvelope(message);
        if (envelope == null) {
            return;
        }

        // Exceptions thrown by the saga are deliberately not swallowed. They are
        // processing errors and Spring Kafka may retry them using its error handler.
        switch (envelope.eventType()) {
            case "PaymentCreatedEvent" -> saga.on(mapper.readValue(envelope.payload(), PaymentCreatedEvent.class));
            case "FundsReservedEvent" -> saga.on(mapper.readValue(envelope.payload(), FundsReservedEvent.class));
            case "FraudApprovedEvent" -> saga.on(mapper.readValue(envelope.payload(), FraudApprovedEvent.class));
            case "FraudRejectedEvent" -> saga.on(mapper.readValue(envelope.payload(), FraudRejectedEvent.class));
            case "SettlementCapturedEvent" -> saga.on(mapper.readValue(envelope.payload(), SettlementCapturedEvent.class));
            case "PaymentCompletedEvent" -> saga.on(mapper.readValue(envelope.payload(), PaymentCompletedEvent.class));
            default -> deadLetter(message, "Unsupported eventType: " + envelope.eventType());
        }
    }

    private KafkaEventEnvelope parseEnvelope(String message) throws Exception {
        try {
            JsonNode root = mapper.readTree(message);
            JsonNode eventType = root == null ? null : root.get("eventType");
            JsonNode payload = root == null ? null : root.get("payload");

            // Compatibility with records already written by v4: those messages
            // contain only the domain event and therefore have no eventType.
            if (eventType == null || payload == null || eventType.asText().isBlank() || payload.asText().isBlank()) {
                deadLetter(message, "Invalid or legacy Kafka event envelope: eventType/payload missing");
                return null;
            }

            return new KafkaEventEnvelope(eventType.asText(), payload.asText());
        } catch (Exception ex) {
            deadLetter(message, "Invalid Kafka JSON: " + ex.getMessage());
            return null;
        }
    }

    private void deadLetter(String originalPayload, String reason) throws Exception {
        log.error("Sending invalid Kafka record to {}. Reason: {}", deadLetterTopic, reason);
        String payload = mapper.writeValueAsString(new KafkaDeadLetter(originalPayload, reason, Instant.now()));
        kafka.send(deadLetterTopic, payload).get(KAFKA_ACK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
}
