package com.example.payments.infrastructure.adapters.out.persistence;
import jakarta.persistence.*;import lombok.*;import java.time.Instant;import java.util.UUID;
@Entity @Table(name="event_store") @Getter @Setter @NoArgsConstructor
public class EventStoreEntity { @Id private UUID eventId; private UUID aggregateId; private String aggregateType; private String eventType; @Column(columnDefinition="TEXT") private String payload; private Instant createdAt; }
