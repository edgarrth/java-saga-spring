package com.example.payments.infrastructure.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class PaymentEntity {
    @Id
    @Column(name = "payment_id")
    private UUID paymentId;
    private BigDecimal amount;
    private String currency;
    private String mode;
    private String status;
    private boolean fundsReserved;
    private Boolean fraudApproved;
    private boolean settlementCaptured;
    private String failureReason;
    private Instant createdAt;
    private Instant updatedAt;

    public PaymentEntity() {
    }

    public UUID getPaymentId() { return paymentId; }
    public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isFundsReserved() { return fundsReserved; }
    public void setFundsReserved(boolean fundsReserved) { this.fundsReserved = fundsReserved; }
    public Boolean getFraudApproved() { return fraudApproved; }
    public void setFraudApproved(Boolean fraudApproved) { this.fraudApproved = fraudApproved; }
    public boolean isSettlementCaptured() { return settlementCaptured; }
    public void setSettlementCaptured(boolean settlementCaptured) { this.settlementCaptured = settlementCaptured; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
