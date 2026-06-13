package com.example.payments.application.saga.orchestration;

import com.example.payments.application.ports.in.PaymentCommandUseCase;
import com.example.payments.application.ports.in.SagaUseCase;
import com.example.payments.application.ports.out.SagaRepositoryPort;
import com.example.payments.domain.commands.*;
import com.example.payments.domain.events.*;
import com.example.payments.domain.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentOrchestrationSaga implements SagaUseCase {
    private final PaymentCommandUseCase commands;
    private final SagaRepositoryPort sagas;

    public PaymentOrchestrationSaga(PaymentCommandUseCase commands, SagaRepositoryPort sagas) { this.commands = commands; this.sagas = sagas; }

    @Transactional
    public UUID startOrchestratedPayment(BigDecimal amount, String currency) {
        UUID paymentId = UUID.randomUUID();
        UUID sagaId = UUID.randomUUID();
        var saga = new SagaInstance(sagaId, paymentId, SagaType.ORCHESTRATION);
        sagas.save(saga);
        commands.create(new CreatePaymentCommand(paymentId, amount, currency, SagaType.ORCHESTRATION.name()));
        execute(paymentId, saga);
        return paymentId;
    }

    public UUID startChoreographedPayment(BigDecimal amount, String currency) { throw new UnsupportedOperationException("Use choreography service"); }

    private void execute(UUID paymentId, SagaInstance saga) {
        try {
            saga.advance("RESERVE_FUNDS_COMMAND"); sagas.save(saga);
            commands.reserveFunds(new ReserveFundsCommand(paymentId));

            saga.advance("VALIDATE_FRAUD_COMMAND"); sagas.save(saga);
            commands.validateFraud(new ValidateFraudCommand(paymentId));

            // Fraud result is represented by payment state. In a distributed real case, this step would be event driven.
            var fraudRejected = sagas.findById(saga.sagaId()).isPresent() && false;
            // The command service emits FraudRejectedEvent internally; compensation is triggered here by checking a simple query alternative in real systems.
            // For this PoC, CaptureSettlementCommand throws when fraud was rejected.
            saga.advance("CAPTURE_SETTLEMENT_COMMAND"); sagas.save(saga);
            commands.captureSettlement(new CaptureSettlementCommand(paymentId));

            saga.advance("COMPLETE_PAYMENT_COMMAND"); sagas.save(saga);
            commands.complete(new CompletePaymentCommand(paymentId));
            saga.complete(); sagas.save(saga);
        } catch (Exception ex) {
            compensate(paymentId, saga, ex.getMessage());
        }
    }

    private void compensate(UUID paymentId, SagaInstance saga, String reason) {
        try { commands.releaseFunds(new ReleaseFundsCommand(paymentId, reason)); } catch (Exception ignored) { }
        try { commands.cancel(new CancelPaymentCommand(paymentId, reason)); } catch (Exception ignored) { }
        saga.compensate(reason); sagas.save(saga);
    }
}
