package com.example.payments.application.usecase;

import com.example.payments.application.ports.in.PaymentCommandUseCase;
import com.example.payments.application.ports.out.*;
import com.example.payments.domain.commands.*;
import com.example.payments.domain.events.*;
import com.example.payments.domain.exceptions.FraudRejectedException;
import com.example.payments.domain.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentApplicationService implements PaymentCommandUseCase {
    private final PaymentRepositoryPort payments;
    private final EventStorePort eventStore;
    private final EventPublisherPort publisher;
    private final BigDecimal rejectAbove;

    public PaymentApplicationService(PaymentRepositoryPort payments, EventStorePort eventStore, EventPublisherPort publisher,
                                     @Value("${app.fraud.reject-above}") BigDecimal rejectAbove) {
        this.payments = payments; this.eventStore = eventStore; this.publisher = publisher; this.rejectAbove = rejectAbove;
    }

    @Transactional
    public UUID create(CreatePaymentCommand command) {
        var payment = new Payment(command.paymentId(), command.amount(), command.currency(), SagaType.valueOf(command.mode()));
        payments.save(payment);
        var event = new PaymentCreatedEvent(UUID.randomUUID(), payment.paymentId(), payment.amount(), payment.currency(), payment.mode().name(), Instant.now());
        eventStore.append(event);
        if (payment.mode() == SagaType.CHOREOGRAPHY) {
            publisher.publishOutbox(event);
        }
        return payment.paymentId();
    }

    @Transactional
    public void reserveFunds(ReserveFundsCommand command) {
        var payment = find(command.paymentId());
        payment.reserveFunds(); payments.save(payment);
        emit(new FundsReservedEvent(UUID.randomUUID(), command.paymentId(), Instant.now()), payment.mode());
    }

    @Transactional(noRollbackFor = FraudRejectedException.class)
    public void validateFraud(ValidateFraudCommand command) {
        var payment = find(command.paymentId());
        if (payment.amount().compareTo(rejectAbove) > 0) {
            String reason = "Amount exceeds fraud threshold " + rejectAbove;
            payment.rejectFraud(reason);
            payments.save(payment);
            emit(new FraudRejectedEvent(UUID.randomUUID(), command.paymentId(), reason, Instant.now()), payment.mode());

            // Choreography continues through the FraudRejectedEvent. Orchestration
            // needs an immediate business signal so it can start compensation, but
            // the fraud decision/event must remain committed.
            if (payment.mode() == SagaType.ORCHESTRATION) {
                throw new FraudRejectedException(reason);
            }
            return;
        }

        payment.approveFraud();
        payments.save(payment);
        emit(new FraudApprovedEvent(UUID.randomUUID(), command.paymentId(), Instant.now()), payment.mode());
    }

    @Transactional
    public void captureSettlement(CaptureSettlementCommand command) {
        var payment = find(command.paymentId());
        if (payment.fraudApproved() == null || !payment.fraudApproved()) throw new IllegalStateException("Fraud must be approved");
        payment.captureSettlement(); payments.save(payment);
        emit(new SettlementCapturedEvent(UUID.randomUUID(), command.paymentId(), Instant.now()), payment.mode());
    }

    @Transactional
    public void complete(CompletePaymentCommand command) {
        var payment = find(command.paymentId());
        payment.complete(); payments.save(payment);
        emit(new PaymentCompletedEvent(UUID.randomUUID(), command.paymentId(), Instant.now()), payment.mode());
    }

    @Transactional
    public void releaseFunds(ReleaseFundsCommand command) {
        var payment = find(command.paymentId());
        payment.releaseFunds(); payments.save(payment);
        emit(new FundsReleasedEvent(UUID.randomUUID(), command.paymentId(), command.reason(), Instant.now()), payment.mode());
    }

    @Transactional
    public void cancel(CancelPaymentCommand command) {
        var payment = find(command.paymentId());
        payment.cancel(command.reason()); payments.save(payment);
        emit(new PaymentCancelledEvent(UUID.randomUUID(), command.paymentId(), command.reason(), Instant.now()), payment.mode());
    }

    private Payment find(UUID id) { return payments.findById(id).orElseThrow(() -> new IllegalArgumentException("Payment not found: " + id)); }
    private void emit(DomainEvent event, SagaType mode) { eventStore.append(event); if (mode == SagaType.CHOREOGRAPHY) publisher.publishOutbox(event); }
}
