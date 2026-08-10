package com.example.payments.infrastructure.adapters.out.persistence;

import com.example.payments.application.ports.out.EventStorePort;
import com.example.payments.domain.events.DomainEvent;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.stereotype.Repository;
import java.time.Instant;import java.util.*;

@Repository
public class EventStoreAdapter implements EventStorePort {
 private final EventStoreJpaRepository repo; private final JsonMapper mapper;
 public EventStoreAdapter(EventStoreJpaRepository repo,JsonMapper mapper){this.repo=repo;this.mapper=mapper;}
 public void append(DomainEvent event){ try{ var e=new EventStoreEntity(); e.setEventId(event.eventId()); e.setAggregateId(event.paymentId()); e.setAggregateType("Payment"); e.setEventType(event.eventType()); e.setPayload(mapper.writeValueAsString(event)); e.setCreatedAt(Instant.now()); repo.save(e);}catch(Exception ex){throw new IllegalStateException(ex);} }
 public List<DomainEvent> findByAggregateId(UUID aggregateId){ return List.of(); }
}
