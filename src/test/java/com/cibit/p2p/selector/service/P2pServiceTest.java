package com.cibit.p2p.selector.service;

import com.cibit.p2p.selector.enums.PaymentStatus;
import com.cibit.p2p.selector.repository.P2pRepository;
import com.cibit.p2p.selector.repository.PaymentRepository;
import com.cibit.p2p.selector.security.SignatureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class P2pServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private P2pRepository p2pRepository;

    @Mock
    private SignatureService signatureService;

    @InjectMocks
    private P2pService p2pService;

    // =====================
    // resolveStatus — граничные значения по спеке
    // =====================

    @Test
    void resolveStatus_100_это_failed() {
        PaymentStatus status = invokeResolveStatus(BigDecimal.valueOf(100));
        assertEquals(PaymentStatus.FAILED, status);
    }

    @Test
    void resolveStatus_меньше_100_это_failed() {
        PaymentStatus status = invokeResolveStatus(BigDecimal.valueOf(50));
        assertEquals(PaymentStatus.FAILED, status);
    }

    @Test
    void resolveStatus_101_это_partial_complete() {
        PaymentStatus status = invokeResolveStatus(BigDecimal.valueOf(101));
        assertEquals(PaymentStatus.PARTIAL_COMPLETE, status);
    }

    @Test
    void resolveStatus_500_это_partial_complete() {
        PaymentStatus status = invokeResolveStatus(BigDecimal.valueOf(500));
        assertEquals(PaymentStatus.PARTIAL_COMPLETE, status);
    }

    @Test
    void resolveStatus_501_это_pending() {
        PaymentStatus status = invokeResolveStatus(BigDecimal.valueOf(501));
        assertEquals(PaymentStatus.PENDING, status);
    }

    @Test
    void resolveStatus_1000_это_pending() {
        PaymentStatus status = invokeResolveStatus(BigDecimal.valueOf(1000));
        assertEquals(PaymentStatus.PENDING, status);
    }

    @Test
    void resolveStatus_1001_это_complete() {
        PaymentStatus status = invokeResolveStatus(BigDecimal.valueOf(1001));
        assertEquals(PaymentStatus.COMPLETE, status);
    }

    @Test
    void resolveStatus_большая_сумма_это_complete() {
        PaymentStatus status = invokeResolveStatus(BigDecimal.valueOf(999999));
        assertEquals(PaymentStatus.COMPLETE, status);
    }

    // =====================
    // Вспомогательный метод
    // =====================

    private PaymentStatus invokeResolveStatus(BigDecimal amount) {
        return (PaymentStatus) ReflectionTestUtils.invokeMethod(p2pService, "resolveStatus", amount);
    }
}
