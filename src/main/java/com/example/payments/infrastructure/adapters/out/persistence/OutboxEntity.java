package com.example.payments.infrastructure.adapters.out.persistence;
import jakarta.persistence.*;import lombok.*;import java.time.Instant;import java.util.UUID;
@Entity @Table(name="outbox_events") @Getter @Setter @NoArgsConstructor
public class OutboxEntity { @Id private UUID outboxId; private UUID aggregateId; private String eventType; private String topic; @Column(columnDefinition="TEXT") private String payload; private boolean published; private Instant createdAt; private Instant publishedAt; }
