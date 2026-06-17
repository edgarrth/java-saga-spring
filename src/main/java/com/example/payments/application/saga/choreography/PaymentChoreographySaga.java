package com.example.payments.application.saga.choreography;

import com.example.payments.application.ports.in.*;
import com.example.payments.application.ports.out.SagaRepositoryPort;
import com.example.payments.domain.commands.*;
import com.example.payments.domain.events.*;
import com.example.payments.domain.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;import java.util.UUID;

@Service
public class PaymentChoreographySaga implements SagaUseCase {
    private final PaymentCommandUseCase commands; private final SagaRepositoryPort sagas;
    public PaymentChoreographySaga(PaymentCommandUseCase commands, SagaRepositoryPort sagas){this.commands=commands;this.sagas=sagas;}

    public UUID startOrchestratedPayment(BigDecimal amount, String currency){ throw new UnsupportedOperationException("Use orchestration service"); }

    @Transactional
    public UUID startChoreographedPayment(BigDecimal amount, String currency){
        UUID paymentId = UUID.randomUUID(); UUID sagaId = UUID.randomUUID();
        sagas.save(new SagaInstance(sagaId, paymentId, SagaType.CHOREOGRAPHY));
        commands.create(new CreatePaymentCommand(paymentId, amount, currency, SagaType.CHOREOGRAPHY.name()));
        return paymentId;
    }

    @Transactional public void on(PaymentCreatedEvent event){ sagas.findByPaymentId(event.paymentId()).ifPresent(s -> {s.advance("RESERVE_FUNDS_COMMAND_BY_EVENT"); sagas.save(s); commands.reserveFunds(new ReserveFundsCommand(event.paymentId()));}); }
    @Transactional public void on(FundsReservedEvent event){ sagas.findByPaymentId(event.paymentId()).ifPresent(s -> {s.advance("VALIDATE_FRAUD_COMMAND_BY_EVENT"); sagas.save(s); commands.validateFraud(new ValidateFraudCommand(event.paymentId()));}); }
    @Transactional public void on(FraudApprovedEvent event){ sagas.findByPaymentId(event.paymentId()).ifPresent(s -> {s.advance("CAPTURE_SETTLEMENT_COMMAND_BY_EVENT"); sagas.save(s); commands.captureSettlement(new CaptureSettlementCommand(event.paymentId()));}); }
    @Transactional public void on(SettlementCapturedEvent event){ sagas.findByPaymentId(event.paymentId()).ifPresent(s -> {s.advance("COMPLETE_PAYMENT_COMMAND_BY_EVENT"); sagas.save(s); commands.complete(new CompletePaymentCommand(event.paymentId()));}); }
    @Transactional public void on(PaymentCompletedEvent event){ sagas.findByPaymentId(event.paymentId()).ifPresent(s -> {s.complete(); sagas.save(s);}); }
    @Transactional public void on(FraudRejectedEvent event){ sagas.findByPaymentId(event.paymentId()).ifPresent(s -> {commands.releaseFunds(new ReleaseFundsCommand(event.paymentId(), event.reason())); commands.cancel(new CancelPaymentCommand(event.paymentId(), event.reason())); s.compensate(event.reason()); sagas.save(s);}); }
}
