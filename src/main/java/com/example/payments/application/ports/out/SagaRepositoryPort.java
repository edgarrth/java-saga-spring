package com.example.payments.application.ports.out;
import com.example.payments.domain.model.SagaInstance;import java.util.*;
public interface SagaRepositoryPort { SagaInstance save(SagaInstance saga); Optional<SagaInstance> findById(UUID sagaId); Optional<SagaInstance> findByPaymentId(UUID paymentId); }
