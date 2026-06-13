package com.example.payments.infrastructure.adapters.in.rest;
import jakarta.validation.constraints.*;import java.math.BigDecimal;
public record CreatePaymentRequest(@NotNull @DecimalMin("0.01") BigDecimal amount, @NotBlank String currency) {}
