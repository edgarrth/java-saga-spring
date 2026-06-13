package com.example.payments.application.ports.out;
import com.example.payments.domain.model.Payment;import java.util.*;
public interface PaymentRepositoryPort { Payment save(Payment payment); Optional<Payment> findById(UUID paymentId); }
