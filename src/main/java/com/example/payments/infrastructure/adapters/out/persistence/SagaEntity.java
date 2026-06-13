package com.example.payments.infrastructure.adapters.out.persistence;
import jakarta.persistence.*;import lombok.*;import java.time.Instant;import java.util.UUID;
@Entity @Table(name="saga_instances") @Getter @Setter @NoArgsConstructor
public class SagaEntity { @Id @Column(name="saga_id") private UUID sagaId; private UUID paymentId; private String sagaType; private String status; private String currentStep; private boolean compensationExecuted; private String failureReason; private Instant createdAt; private Instant updatedAt; }
