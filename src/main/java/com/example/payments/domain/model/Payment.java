package com.example.payments.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Payment {
    private final UUID paymentId;
    private final BigDecimal amount;
    private final String currency;
    private final SagaType mode;
    private PaymentStatus status;
    private boolean fundsReserved;
    private Boolean fraudApproved;
    private boolean settlementCaptured;
    private String failureReason;
    private final Instant createdAt;
    private Instant updatedAt;

    public Payment(UUID paymentId, BigDecimal amount, String currency, SagaType mode) {
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("Amount must be positive");
        this.paymentId = paymentId;
        this.amount = amount;
        this.currency = currency;
        this.mode = mode;
        this.status = PaymentStatus.CREATED;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public UUID paymentId() { return paymentId; }
    public BigDecimal amount() { return amount; }
    public String currency() { return currency; }
    public SagaType mode() { return mode; }
    public PaymentStatus status() { return status; }
    public boolean fundsReserved() { return fundsReserved; }
    public Boolean fraudApproved() { return fraudApproved; }
    public boolean settlementCaptured() { return settlementCaptured; }
    public String failureReason() { return failureReason; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    public void reserveFunds() { this.fundsReserved = true; this.status = PaymentStatus.FUNDS_RESERVED; touch(); }
    public void approveFraud() { this.fraudApproved = true; this.status = PaymentStatus.FRAUD_APPROVED; touch(); }
    public void rejectFraud(String reason) { this.fraudApproved = false; this.failureReason = reason; this.status = PaymentStatus.FRAUD_REJECTED; touch(); }
    public void captureSettlement() { this.settlementCaptured = true; this.status = PaymentStatus.CAPTURED; touch(); }
    public void complete() { this.status = PaymentStatus.COMPLETED; touch(); }
    public void releaseFunds() { this.fundsReserved = false; this.status = PaymentStatus.COMPENSATED; touch(); }
    public void cancel(String reason) { this.failureReason = reason; this.status = PaymentStatus.CANCELLED; touch(); }
    private void touch() { this.updatedAt = Instant.now(); }
}
