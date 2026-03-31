package com.cibit.p2p.selector.entity;

import com.cibit.p2p.selector.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Наш внутренний ID — возвращаем в ответе /sale
    @Column(name = "invoice_id", nullable = false, unique = true)
    private String invoiceId;

    // ID мерчанта
    @Column(name = "merchant_id", nullable = false)
    private String merchantId;

    @Column(name = "endpoint_id", nullable = false)
    private String endpointId;

    // Номер заказа в системе партнёра
    @Column(name = "order_number", nullable = false)
    private String orderNumber;

    // ID клиента в системе партнёра
    @Column(name = "customer", nullable = false)
    private String customer;

    // Тип платежа: card / sbp / bank_transfer
    @Column(name = "payment_type", nullable = false)
    private String paymentType;

    // Сумма платежа
    @Column(name = "amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal amount;

    // Валюта
    @Column(name = "currency", nullable = false)
    private String currency;

    // Статус платежа
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    // Время создания
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Время последнего обновления
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
