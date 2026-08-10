package com.example.payments;

import com.example.payments.application.ports.out.EventPublisherPort;
import com.example.payments.application.ports.out.EventStorePort;
import com.example.payments.application.ports.out.PaymentRepositoryPort;
import com.example.payments.application.ports.out.SagaRepositoryPort;
import com.example.payments.application.saga.choreography.PaymentChoreographySaga;
import com.example.payments.application.saga.orchestration.PaymentOrchestrationSaga;
import com.example.payments.application.usecase.PaymentApplicationService;
import com.example.payments.domain.events.*;
import com.example.payments.domain.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SagaFlowTest {

    @Test
    void orchestrationCompletesWithoutPublishingChoreographyEvents() {
        var fixture = new Fixture();
        var orchestration = new PaymentOrchestrationSaga(fixture.commands, fixture.sagas);

        UUID paymentId = orchestration.startOrchestratedPayment(new BigDecimal("120.50"), "USD");

        assertEquals(PaymentStatus.COMPLETED, fixture.payments.findById(paymentId).orElseThrow().status());
        assertEquals(SagaStatus.COMPLETED, fixture.sagas.findByPaymentId(paymentId).orElseThrow().status());
        assertTrue(fixture.publisher.queue.isEmpty(), "orchestration must not emit events to choreography Kafka flow");
    }

    @Test
    void orchestrationCompensatesRejectedPayment() {
        var fixture = new Fixture();
        var orchestration = new PaymentOrchestrationSaga(fixture.commands, fixture.sagas);

        UUID paymentId = orchestration.startOrchestratedPayment(new BigDecimal("6000.00"), "USD");

        var payment = fixture.payments.findById(paymentId).orElseThrow();
        var saga = fixture.sagas.findByPaymentId(paymentId).orElseThrow();

        assertEquals(PaymentStatus.CANCELLED, payment.status());
        assertEquals(Boolean.FALSE, payment.fraudApproved());
        assertEquals(false, payment.fundsReserved());
        assertEquals("Amount exceeds fraud threshold 5000.00", payment.failureReason());
        assertEquals(SagaStatus.COMPENSATED, saga.status());
        assertEquals("COMPENSATED", saga.currentStep());
        assertTrue(saga.compensationExecuted());
        assertEquals(payment.failureReason(), saga.failureReason());
        assertTrue(fixture.publisher.queue.isEmpty(), "orchestration must not emit events to choreography Kafka flow");
    }

    @Test
    void choreographyCompletes() {
        var fixture = new Fixture();
        var choreography = new PaymentChoreographySaga(fixture.commands, fixture.sagas);

        UUID paymentId = choreography.startChoreographedPayment(new BigDecimal("250.00"), "USD");
        fixture.drain(choreography);

        assertEquals(PaymentStatus.COMPLETED, fixture.payments.findById(paymentId).orElseThrow().status());
        assertEquals(SagaStatus.COMPLETED, fixture.sagas.findByPaymentId(paymentId).orElseThrow().status());
    }

    @Test
    void choreographyCompensatesRejectedPayment() {
        var fixture = new Fixture();
        var choreography = new PaymentChoreographySaga(fixture.commands, fixture.sagas);

        UUID paymentId = choreography.startChoreographedPayment(new BigDecimal("9000.00"), "USD");
        fixture.drain(choreography);

        assertEquals(PaymentStatus.CANCELLED, fixture.payments.findById(paymentId).orElseThrow().status());
        assertEquals(SagaStatus.COMPENSATED, fixture.sagas.findByPaymentId(paymentId).orElseThrow().status());
    }

    private static final class Fixture {
        private final InMemoryPayments payments = new InMemoryPayments();
        private final InMemorySagas sagas = new InMemorySagas();
        private final InMemoryEvents events = new InMemoryEvents();
        private final InMemoryPublisher publisher = new InMemoryPublisher();
        private final PaymentApplicationService commands =
                new PaymentApplicationService(payments, events, publisher, new BigDecimal("5000.00"));

        private void drain(PaymentChoreographySaga choreography) {
            int guard = 0;
            while (!publisher.queue.isEmpty() && guard++ < 20) {
                DomainEvent event = publisher.queue.remove();
                if (event instanceof PaymentCreatedEvent value) choreography.on(value);
                else if (event instanceof FundsReservedEvent value) choreography.on(value);
                else if (event instanceof FraudApprovedEvent value) choreography.on(value);
                else if (event instanceof FraudRejectedEvent value) choreography.on(value);
                else if (event instanceof SettlementCapturedEvent value) choreography.on(value);
                else if (event instanceof PaymentCompletedEvent value) choreography.on(value);
            }
            assertTrue(guard < 20, "choreography event loop did not converge");
        }
    }

    private static final class InMemoryPayments implements PaymentRepositoryPort {
        private final Map<UUID, Payment> values = new HashMap<>();
        public Payment save(Payment payment) { values.put(payment.paymentId(), payment); return payment; }
        public Optional<Payment> findById(UUID id) { return Optional.ofNullable(values.get(id)); }
    }

    private static final class InMemorySagas implements SagaRepositoryPort {
        private final Map<UUID, SagaInstance> values = new HashMap<>();
        public SagaInstance save(SagaInstance saga) { values.put(saga.sagaId(), saga); return saga; }
        public Optional<SagaInstance> findById(UUID id) { return Optional.ofNullable(values.get(id)); }
        public Optional<SagaInstance> findByPaymentId(UUID paymentId) {
            return values.values().stream().filter(saga -> saga.paymentId().equals(paymentId)).findFirst();
        }
    }

    private static final class InMemoryEvents implements EventStorePort {
        private final List<DomainEvent> values = new ArrayList<>();
        public void append(DomainEvent event) { values.add(event); }
        public List<DomainEvent> findByAggregateId(UUID aggregateId) {
            return values.stream().filter(event -> event.paymentId().equals(aggregateId)).toList();
        }
    }

    private static final class InMemoryPublisher implements EventPublisherPort {
        private final ArrayDeque<DomainEvent> queue = new ArrayDeque<>();
        public void publish(DomainEvent event) { queue.add(event); }
        public void publishOutbox(DomainEvent event) { queue.add(event); }
    }
}
