package com.example.payments.infrastructure.adapters.in.rest;
import com.example.payments.domain.model.SagaInstance;import java.util.UUID;
public record SagaResponse(UUID sagaId, UUID paymentId, String sagaType, String status, String currentStep, boolean compensationExecuted, String failureReason) { public static SagaResponse from(SagaInstance s){return new SagaResponse(s.sagaId(),s.paymentId(),s.sagaType().name(),s.status().name(),s.currentStep(),s.compensationExecuted(),s.failureReason());}}
