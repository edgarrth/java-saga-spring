package com.example.payments.infrastructure.adapters.out.persistence;
import com.example.payments.application.ports.out.PaymentRepositoryPort;import com.example.payments.domain.model.*;import org.springframework.stereotype.Repository;import java.util.*;
@Repository
public class PaymentPersistenceAdapter implements PaymentRepositoryPort {
 private final PaymentJpaRepository repo; public PaymentPersistenceAdapter(PaymentJpaRepository repo){this.repo=repo;}
 public Payment save(Payment p){ repo.save(toEntity(p)); return p; }
 public Optional<Payment> findById(UUID id){ return repo.findById(id).map(this::toDomain); }
 private PaymentEntity toEntity(Payment p){ var e=new PaymentEntity(); e.setPaymentId(p.paymentId()); e.setAmount(p.amount()); e.setCurrency(p.currency()); e.setMode(p.mode().name()); e.setStatus(p.status().name()); e.setFundsReserved(p.fundsReserved()); e.setFraudApproved(p.fraudApproved()); e.setSettlementCaptured(p.settlementCaptured()); e.setFailureReason(p.failureReason()); e.setCreatedAt(p.createdAt()); e.setUpdatedAt(p.updatedAt()); return e; }
 private Payment toDomain(PaymentEntity e){ var p=new Payment(e.getPaymentId(), e.getAmount(), e.getCurrency(), SagaType.valueOf(e.getMode())); if(e.isFundsReserved())p.reserveFunds(); if(Boolean.TRUE.equals(e.getFraudApproved()))p.approveFraud(); if(Boolean.FALSE.equals(e.getFraudApproved()))p.rejectFraud(e.getFailureReason()); if(e.isSettlementCaptured())p.captureSettlement(); switch(PaymentStatus.valueOf(e.getStatus())){case COMPLETED -> p.complete(); case CANCELLED -> p.cancel(e.getFailureReason()); case COMPENSATED -> p.releaseFunds(); default -> {}} return p; }
}
