package com.example.payments.infrastructure.adapters.in.rest;
import com.example.payments.domain.model.Payment;import java.math.BigDecimal;import java.util.UUID;
public record PaymentResponse(UUID paymentId, BigDecimal amount, String currency, String mode, String status, boolean fundsReserved, Boolean fraudApproved, boolean settlementCaptured, String failureReason) { public static PaymentResponse from(Payment p){return new PaymentResponse(p.paymentId(),p.amount(),p.currency(),p.mode().name(),p.status().name(),p.fundsReserved(),p.fraudApproved(),p.settlementCaptured(),p.failureReason());}}
