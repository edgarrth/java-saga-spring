package com.example.payments.application.ports.out;
import com.example.payments.domain.events.DomainEvent;
public interface EventPublisherPort { void publish(DomainEvent event); void publishOutbox(DomainEvent event); }
