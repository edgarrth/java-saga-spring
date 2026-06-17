package com.example.payments.infrastructure.adapters.in.rest;

import com.example.payments.application.ports.in.PaymentQueryUseCase;
import com.example.payments.application.saga.choreography.PaymentChoreographySaga;
import com.example.payments.application.saga.orchestration.PaymentOrchestrationSaga;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;import java.util.UUID;

@RestController
@RequestMapping("/payments/v1")
public class PaymentController {
 private final PaymentOrchestrationSaga orchestration; private final PaymentChoreographySaga choreography; private final PaymentQueryUseCase queries;
 public PaymentController(PaymentOrchestrationSaga orchestration, PaymentChoreographySaga choreography, PaymentQueryUseCase queries){this.orchestration=orchestration;this.choreography=choreography;this.queries=queries;}
 @PostMapping("/orchestrated-payments")
 public ResponseEntity<PaymentResponse> createOrchestrated(@Valid @RequestBody CreatePaymentRequest request){ UUID id=orchestration.startOrchestratedPayment(request.amount(), request.currency()); var body=queries.getPayment(id).map(PaymentResponse::from).orElseThrow(); return ResponseEntity.created(URI.create("/payments/v1/payments/"+id)).body(body); }
 @PostMapping("/choreographed-payments")
 public ResponseEntity<PaymentResponse> createChoreographed(@Valid @RequestBody CreatePaymentRequest request){ UUID id=choreography.startChoreographedPayment(request.amount(), request.currency()); var body=queries.getPayment(id).map(PaymentResponse::from).orElseThrow(); return ResponseEntity.accepted().location(URI.create("/payments/v1/payments/"+id)).body(body); }
 @GetMapping("/payments/{paymentId}")
 public PaymentResponse getPayment(@PathVariable UUID paymentId){ return queries.getPayment(paymentId).map(PaymentResponse::from).orElseThrow(); }
 @GetMapping("/payments/{paymentId}/saga")
 public SagaResponse getSagaByPayment(@PathVariable UUID paymentId){ return queries.getSagaByPayment(paymentId).map(SagaResponse::from).orElseThrow(); }
}
