package com.example.payments.domain.commands;
import java.util.UUID;
public record CancelPaymentCommand(UUID paymentId, String reason) {}
