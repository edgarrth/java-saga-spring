package com.example.payments.domain.commands;
import java.util.UUID;
public record ReleaseFundsCommand(UUID paymentId, String reason) {}
