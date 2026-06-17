package com.example.payments.application.ports.out;
import com.example.payments.domain.events.DomainEvent;import java.util.List;import java.util.UUID;
public interface EventStorePort { void append(DomainEvent event); List<DomainEvent> findByAggregateId(UUID aggregateId); }
