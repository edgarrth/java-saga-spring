package com.example.payments.infrastructure.adapters.out.persistence;
import com.example.payments.application.ports.out.SagaRepositoryPort;import com.example.payments.domain.model.*;import org.springframework.stereotype.Repository;import java.util.*;
@Repository
public class SagaPersistenceAdapter implements SagaRepositoryPort {
 private final SagaJpaRepository repo; public SagaPersistenceAdapter(SagaJpaRepository repo){this.repo=repo;}
 public SagaInstance save(SagaInstance s){repo.save(toEntity(s)); return s;} public Optional<SagaInstance> findById(UUID id){return repo.findById(id).map(this::toDomain);} public Optional<SagaInstance> findByPaymentId(UUID id){return repo.findByPaymentId(id).map(this::toDomain);}
 private SagaEntity toEntity(SagaInstance s){var e=new SagaEntity(); e.setSagaId(s.sagaId()); e.setPaymentId(s.paymentId()); e.setSagaType(s.sagaType().name()); e.setStatus(s.status().name()); e.setCurrentStep(s.currentStep()); e.setCompensationExecuted(s.compensationExecuted()); e.setFailureReason(s.failureReason()); e.setCreatedAt(s.createdAt()); e.setUpdatedAt(s.updatedAt()); return e;}
 private SagaInstance toDomain(SagaEntity e){var s=new SagaInstance(e.getSagaId(), e.getPaymentId(), SagaType.valueOf(e.getSagaType())); switch(SagaStatus.valueOf(e.getStatus())){case IN_PROGRESS -> s.advance(e.getCurrentStep()); case COMPLETED -> s.complete(); case COMPENSATED -> s.compensate(e.getFailureReason()); case FAILED -> s.fail(e.getFailureReason()); default -> {}} return s;}
}
