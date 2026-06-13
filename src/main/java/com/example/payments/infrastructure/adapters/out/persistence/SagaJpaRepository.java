package com.example.payments.infrastructure.adapters.out.persistence;
import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface SagaJpaRepository extends JpaRepository<SagaEntity, UUID> { Optional<SagaEntity> findByPaymentId(UUID paymentId); }
