package com.example.payments.infrastructure.adapters.out.messaging;

import com.example.payments.application.ports.out.EventPublisherPort;
import com.example.payments.domain.events.DomainEvent;
import com.example.payments.infrastructure.adapters.out.persistence.OutboxEntity;
import com.example.payments.infrastructure.adapters.out.persistence.OutboxJpaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class KafkaEventPublisherAdapter implements EventPublisherPort {
    private static final long KAFKA_ACK_TIMEOUT_SECONDS = 10;

    private final KafkaTemplate<String, String> kafka;
    private final OutboxJpaRepository outbox;
    private final JsonMapper mapper;
    private final String topic;

    public KafkaEventPublisherAdapter(KafkaTemplate<String, String> kafka,
                                      OutboxJpaRepository outbox,
                                      JsonMapper mapper,
                                      @Value("${app.topics.payment-events}") String topic) {
        this.kafka = kafka;
        this.outbox = outbox;
        this.mapper = mapper;
        this.topic = topic;
    }

    @Override
    public void publish(DomainEvent event) {
        try {
            String eventPayload = mapper.writeValueAsString(event);
            sendAndWait(topic, event.paymentId().toString(), envelope(event.eventType(), eventPayload));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to publish event " + event.eventType(), ex);
        }
    }

    @Override
    public void publishOutbox(DomainEvent event) {
        try {
            var entity = new OutboxEntity();
            entity.setOutboxId(UUID.randomUUID());
            entity.setAggregateId(event.paymentId());
            entity.setEventType(event.eventType());
            entity.setTopic(topic);
            entity.setPayload(mapper.writeValueAsString(event));
            entity.setCreatedAt(Instant.now());
            outbox.save(entity);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to persist event in outbox " + event.eventType(), ex);
        }
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void flushOutbox() {
        for (var entity : outbox.findTop50ByPublishedFalseOrderByCreatedAtAsc()) {
            try {
                String message = envelope(entity.getEventType(), entity.getPayload());
                sendAndWait(entity.getTopic(), entity.getAggregateId().toString(), message);

                // Mark as published only after Kafka acknowledged the record.
                entity.setPublished(true);
                entity.setPublishedAt(Instant.now());
                outbox.save(entity);
            } catch (Exception ex) {
                throw new IllegalStateException("Unable to flush outbox event " + entity.getOutboxId(), ex);
            }
        }
    }

    private String envelope(String eventType, String payload) throws Exception {
        return mapper.writeValueAsString(new KafkaEventEnvelope(eventType, payload));
    }

    private void sendAndWait(String destination, String key, String message) throws Exception {
        kafka.send(destination, key, message).get(KAFKA_ACK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
}
