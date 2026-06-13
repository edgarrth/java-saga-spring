package com.example.payments.infrastructure.adapters.out.persistence;
import org.springframework.data.jpa.repository.JpaRepository;import java.util.UUID;
public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, UUID> {}
