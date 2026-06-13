package com.example.payments.infrastructure.adapters.out.messaging;

import com.example.payments.application.ports.out.EventPublisherPort;
import com.example.payments.domain.events.DomainEvent;
import com.example.payments.infrastructure.adapters.out.persistence.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;import java.util.UUID;

@Component
public class KafkaEventPublisherAdapter implements EventPublisherPort {
 private final KafkaTemplate<String,String> kafka; private final OutboxJpaRepository outbox; private final ObjectMapper mapper; private final String topic;
 public KafkaEventPublisherAdapter(KafkaTemplate<String,String> kafka, OutboxJpaRepository outbox, ObjectMapper mapper, @Value("${app.topics.payment-events}") String topic){this.kafka=kafka;this.outbox=outbox;this.mapper=mapper;this.topic=topic;}
 public void publish(DomainEvent event){ try{ kafka.send(topic, event.paymentId().toString(), mapper.writeValueAsString(event)); }catch(Exception ex){throw new IllegalStateException(ex);} }
 public void publishOutbox(DomainEvent event){ try{ var e=new OutboxEntity(); e.setOutboxId(UUID.randomUUID()); e.setAggregateId(event.paymentId()); e.setEventType(event.eventType()); e.setTopic(topic); e.setPayload(mapper.writeValueAsString(event)); e.setCreatedAt(Instant.now()); outbox.save(e);}catch(Exception ex){throw new IllegalStateException(ex);} }
 @Scheduled(fixedDelay = 1000)
 @Transactional
 public void flushOutbox(){ for(var e: outbox.findTop50ByPublishedFalseOrderByCreatedAtAsc()){ kafka.send(e.getTopic(), e.getAggregateId().toString(), e.getPayload()); e.setPublished(true); e.setPublishedAt(Instant.now()); outbox.save(e);} }
}
