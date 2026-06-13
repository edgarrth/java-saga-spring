package com.example.payments.infrastructure.adapters.out.persistence;
import jakarta.persistence.*;import lombok.*;import java.math.BigDecimal;import java.time.Instant;import java.util.UUID;
@Entity @Table(name="payments") @Getter @Setter @NoArgsConstructor
public class PaymentEntity { @Id @Column(name="payment_id") private UUID paymentId; private BigDecimal amount; private String currency; private String mode; private String status; private boolean fundsReserved; private Boolean fraudApproved; private boolean settlementCaptured; private String failureReason; private Instant createdAt; private Instant updatedAt; }
