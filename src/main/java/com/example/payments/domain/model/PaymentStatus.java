package com.example.payments.domain.model;

public enum PaymentStatus {
    CREATED,
    FUNDS_RESERVED,
    FRAUD_APPROVED,
    FRAUD_REJECTED,
    CAPTURED,
    COMPLETED,
    CANCELLED,
    COMPENSATED
}
