package com.example.payments.domain.exceptions;

/**
 * Signals a business fraud rejection in the orchestration flow.
 *
 * The fraud decision itself is a valid business outcome and must be committed
 * before the orchestration saga starts its compensating transactions.
 */
public class FraudRejectedException extends RuntimeException {
    public FraudRejectedException(String message) {
        super(message);
    }
}
