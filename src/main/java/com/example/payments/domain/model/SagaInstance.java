package com.example.payments.domain.model;

import java.time.Instant;
import java.util.UUID;

public class SagaInstance {
    private final UUID sagaId;
    private final UUID paymentId;
    private final SagaType sagaType;
    private SagaStatus status;
    private String currentStep;
    private boolean compensationExecuted;
    private String failureReason;
    private final Instant createdAt;
    private Instant updatedAt;

    public SagaInstance(UUID sagaId, UUID paymentId, SagaType sagaType) {
        this.sagaId = sagaId;
        this.paymentId = paymentId;
        this.sagaType = sagaType;
        this.status = SagaStatus.STARTED;
        this.currentStep = "PAYMENT_CREATED";
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public UUID sagaId() { return sagaId; }
    public UUID paymentId() { return paymentId; }
    public SagaType sagaType() { return sagaType; }
    public SagaStatus status() { return status; }
    public String currentStep() { return currentStep; }
    public boolean compensationExecuted() { return compensationExecuted; }
    public String failureReason() { return failureReason; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public void advance(String step) { this.status = SagaStatus.IN_PROGRESS; this.currentStep = step; touch(); }
    public void complete() { this.status = SagaStatus.COMPLETED; this.currentStep = "COMPLETED"; touch(); }
    public void compensate(String reason) { this.status = SagaStatus.COMPENSATED; this.currentStep = "COMPENSATED"; this.compensationExecuted = true; this.failureReason = reason; touch(); }
    public void fail(String reason) { this.status = SagaStatus.FAILED; this.failureReason = reason; touch(); }
    private void touch() { this.updatedAt = Instant.now(); }
}
