package com.example.payments.application.ports.in;
import com.example.payments.domain.commands.*;import java.util.UUID;
public interface PaymentCommandUseCase {
 UUID create(CreatePaymentCommand command);
 void reserveFunds(ReserveFundsCommand command);
 void validateFraud(ValidateFraudCommand command);
 void captureSettlement(CaptureSettlementCommand command);
 void complete(CompletePaymentCommand command);
 void releaseFunds(ReleaseFundsCommand command);
 void cancel(CancelPaymentCommand command);
}
