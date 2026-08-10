package com.example.payments;

import com.example.payments.application.saga.orchestration.PaymentOrchestrationSaga;
import com.example.payments.application.usecase.PaymentApplicationService;
import com.example.payments.domain.commands.ValidateFraudCommand;
import com.example.payments.domain.exceptions.FraudRejectedException;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrchestrationTransactionConfigurationTest {

    @Test
    void orchestratorDoesNotRunInsideOneGlobalTransaction() throws Exception {
        var method = PaymentOrchestrationSaga.class.getMethod(
                "startOrchestratedPayment", BigDecimal.class, String.class);
        var tx = method.getAnnotation(Transactional.class);

        assertEquals(Propagation.NOT_SUPPORTED, tx.propagation(),
                "the orchestration must not wrap all saga steps in one transaction");
    }

    @Test
    void fraudRejectionCommitsBeforeCompensationSignalIsPropagated() throws Exception {
        var method = PaymentApplicationService.class.getMethod("validateFraud", ValidateFraudCommand.class);
        var tx = method.getAnnotation(Transactional.class);

        assertTrue(java.util.Arrays.asList(tx.noRollbackFor()).contains(FraudRejectedException.class),
                "FraudRejectedException must not roll back the persisted fraud rejection");
    }
}
