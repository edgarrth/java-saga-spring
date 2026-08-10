package com.example.payments.application.saga.orchestration;

import com.example.payments.application.ports.in.PaymentCommandUseCase;
import com.example.payments.application.ports.in.SagaUseCase;
import com.example.payments.application.ports.out.SagaRepositoryPort;
import com.example.payments.domain.commands.CancelPaymentCommand;
import com.example.payments.domain.commands.CaptureSettlementCommand;
import com.example.payments.domain.commands.CompletePaymentCommand;
import com.example.payments.domain.commands.CreatePaymentCommand;
import com.example.payments.domain.commands.ReleaseFundsCommand;
import com.example.payments.domain.commands.ReserveFundsCommand;
import com.example.payments.domain.commands.ValidateFraudCommand;
import com.example.payments.domain.model.SagaInstance;
import com.example.payments.domain.model.SagaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentOrchestrationSaga implements SagaUseCase {
    private final PaymentCommandUseCase commands;
    private final SagaRepositoryPort sagas;

    public PaymentOrchestrationSaga(PaymentCommandUseCase commands, SagaRepositoryPort sagas) {
        this.commands = commands;
        this.sagas = sagas;
    }

    /**
     * A saga must not wrap every step in one ACID transaction. NOT_SUPPORTED
     * explicitly prevents an ambient transaction from spanning the orchestration;
     * each command executes in its own local transaction in PaymentApplicationService.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public UUID startOrchestratedPayment(BigDecimal amount, String currency) {
        UUID paymentId = UUID.randomUUID();
        UUID sagaId = UUID.randomUUID();
        var saga = new SagaInstance(sagaId, paymentId, SagaType.ORCHESTRATION);

        sagas.save(saga);
        commands.create(new CreatePaymentCommand(paymentId, amount, currency, SagaType.ORCHESTRATION.name()));
        execute(paymentId, saga);

        return paymentId;
    }

    @Override
    public UUID startChoreographedPayment(BigDecimal amount, String currency) {
        throw new UnsupportedOperationException("Use choreography service");
    }

    private void execute(UUID paymentId, SagaInstance saga) {
        try {
            saga.advance("RESERVE_FUNDS_COMMAND");
            sagas.save(saga);
            commands.reserveFunds(new ReserveFundsCommand(paymentId));

            saga.advance("VALIDATE_FRAUD_COMMAND");
            sagas.save(saga);
            // For orchestration, a rejected fraud decision is persisted and then
            // signalled with FraudRejectedException. The catch below starts compensation.
            commands.validateFraud(new ValidateFraudCommand(paymentId));

            saga.advance("CAPTURE_SETTLEMENT_COMMAND");
            sagas.save(saga);
            commands.captureSettlement(new CaptureSettlementCommand(paymentId));

            saga.advance("COMPLETE_PAYMENT_COMMAND");
            sagas.save(saga);
            commands.complete(new CompletePaymentCommand(paymentId));

            saga.complete();
            sagas.save(saga);
        } catch (Exception ex) {
            compensate(paymentId, saga, safeReason(ex));
        }
    }

    private void compensate(UUID paymentId, SagaInstance saga, String reason) {
        try {
            commands.releaseFunds(new ReleaseFundsCommand(paymentId, reason));
        } catch (Exception ignored) {
            // Compensation is best-effort in this PoC; the final cancellation is still attempted.
        }

        try {
            commands.cancel(new CancelPaymentCommand(paymentId, reason));
        } catch (Exception ignored) {
            // Keep executing the saga state transition so the failure is visible to callers.
        }

        saga.compensate(reason);
        sagas.save(saga);
    }

    private String safeReason(Exception ex) {
        return ex.getMessage() == null || ex.getMessage().isBlank()
                ? ex.getClass().getSimpleName()
                : ex.getMessage();
    }
}
