package com.example.payments.domain.commands;
import java.math.BigDecimal;
import java.util.UUID;
public record CreatePaymentCommand(UUID paymentId, BigDecimal amount, String currency, String mode) {}
