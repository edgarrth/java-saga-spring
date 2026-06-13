package com.example.payments.application.usecase;
import com.example.payments.application.ports.in.PaymentQueryUseCase;import com.example.payments.application.ports.out.*;import com.example.payments.domain.model.*;import org.springframework.stereotype.Service;import java.util.*;
@Service
public class PaymentQueryService implements PaymentQueryUseCase {
 private final PaymentRepositoryPort payments; private final SagaRepositoryPort sagas;
 public PaymentQueryService(PaymentRepositoryPort payments, SagaRepositoryPort sagas){this.payments=payments;this.sagas=sagas;}
 public Optional<Payment> getPayment(UUID paymentId){return payments.findById(paymentId);} public Optional<SagaInstance> getSaga(UUID sagaId){return sagas.findById(sagaId);} public Optional<SagaInstance> getSagaByPayment(UUID paymentId){return sagas.findByPaymentId(paymentId);} }
