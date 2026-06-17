package com.example.payments.domain.commands;
import java.util.UUID;
public record ReserveFundsCommand(UUID paymentId) {}
