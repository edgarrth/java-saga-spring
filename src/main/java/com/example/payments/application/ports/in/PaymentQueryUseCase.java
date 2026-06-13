package com.example.payments.application.ports.in;
import com.example.payments.domain.model.*;import java.util.*;
public interface PaymentQueryUseCase { Optional<Payment> getPayment(UUID paymentId); Optional<SagaInstance> getSaga(UUID sagaId); Optional<SagaInstance> getSagaByPayment(UUID paymentId); }
