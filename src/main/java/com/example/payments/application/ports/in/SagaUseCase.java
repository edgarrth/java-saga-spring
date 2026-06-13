package com.example.payments.application.ports.in;
import java.math.BigDecimal;import java.util.UUID;
public interface SagaUseCase { UUID startOrchestratedPayment(BigDecimal amount, String currency); UUID startChoreographedPayment(BigDecimal amount, String currency); }
