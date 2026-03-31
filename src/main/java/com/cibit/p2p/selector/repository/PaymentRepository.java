package com.cibit.p2p.selector.repository;

import com.cibit.p2p.selector.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByInvoiceId(String invoiceId);

    Optional<Payment> findByMerchantIdAndOrderNumber(String merchantId, String orderNumber);

}