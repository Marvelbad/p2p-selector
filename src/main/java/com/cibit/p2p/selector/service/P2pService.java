package com.cibit.p2p.selector.service;

import com.cibit.p2p.selector.dto.request.CancelRequest;
import com.cibit.p2p.selector.dto.request.SaleRequest;
import com.cibit.p2p.selector.dto.request.StatusRequest;
import com.cibit.p2p.selector.dto.response.Beneficiary;
import com.cibit.p2p.selector.dto.response.CancelResponse;
import com.cibit.p2p.selector.dto.response.SaleResponse;
import com.cibit.p2p.selector.dto.response.StatusResponse;
import com.cibit.p2p.selector.entity.Payment;
import com.cibit.p2p.selector.enums.PaymentStatus;
import com.cibit.p2p.selector.exception.InvalidSignatureException;
import com.cibit.p2p.selector.exception.PaymentNotFoundException;
import com.cibit.p2p.selector.repository.P2pRepository;
import com.cibit.p2p.selector.repository.PaymentRepository;
import com.cibit.p2p.selector.security.SignatureService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class P2pService {

    private final PaymentRepository paymentRepository;
    private final P2pRepository p2pRepository;
    private final SignatureService signatureService;
    private final ObjectMapper objectMapper;

    public SaleResponse sale(SaleRequest request) {
        // Верификация подписи — опциональные поля включаются только если переданы
        Map<String, String> fields = new TreeMap<>();
        fields.put("amount", request.getAmount().toPlainString());
        fields.put("currency", request.getCurrency());
        fields.put("customer", request.getCustomer());
        if (request.getDescription() != null) fields.put("description", request.getDescription());
        if (request.getEmail() != null) fields.put("email", request.getEmail());
        fields.put("endpoint_id", request.getEndpoint_id());
        if (request.getLanguage() != null) fields.put("language", request.getLanguage());
        fields.put("merchant_id", request.getMerchant_id());
        if (request.getNotification_url() != null) fields.put("notification_url", request.getNotification_url());
        fields.put("order", request.getOrder());
        fields.put("payment_type", request.getPayment_type());

        if (!signatureService.verifyPost("/p2p-selector/sale", fields, request.getSignature())) {
            throw new InvalidSignatureException();
        }

        // Идемпотентность — если такой заказ уже существует, возвращаем сохранённый JSON
        Optional<Payment> existing = paymentRepository
                .findByMerchantIdAndOrderNumber(request.getMerchant_id(), request.getOrder());
        if (existing.isPresent()) {
            Payment p = existing.get();
            if (p.getSaleResponseJson() != null) {
                try {
                    log.info("[SALE] Дубликат merchant={} order={}, возвращаем сохранённый ответ",
                            request.getMerchant_id(), request.getOrder());
                    return objectMapper.readValue(p.getSaleResponseJson(), SaleResponse.class);
                } catch (Exception e) {
                    log.warn("[SALE] Ошибка десериализации сохранённого ответа: {}", e.getMessage());
                }
            }
        }

        // Генерируем invoice_id
        String invoiceId = UUID.randomUUID().toString();

        // Сохраняем платёж в PostgreSQL
        Payment payment = new Payment();
        payment.setInvoiceId(invoiceId);
        payment.setMerchantId(request.getMerchant_id());
        payment.setEndpointId(request.getEndpoint_id());
        payment.setOrderNumber(request.getOrder());
        payment.setCustomer(request.getCustomer());
        payment.setPaymentType(request.getPayment_type());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        PaymentStatus status = resolveStatus(request.getAmount());
        payment.setStatus(status);
        if (status == PaymentStatus.FAILED) {
            payment.setErrorCode("AMOUNT_TOO_LOW");
            payment.setErrorMessage("Сумма платежа слишком мала");
        }
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // TODO: заменить на вызов Oracle когда придёт контракт от Вити
        // Beneficiary beneficiary = p2pRepository.getBeneficiary(request);
        Beneficiary beneficiary = buildStubBeneficiary(request.getPayment_type());

        SaleResponse response = new SaleResponse();
        response.setStatus("success");
        response.setInvoice_id(invoiceId);
        response.setBeneficiary(beneficiary);

        // Сохраняем полный JSON ответа для идемпотентности
        try {
            payment.setSaleResponseJson(objectMapper.writeValueAsString(response));
            paymentRepository.save(payment);
        } catch (Exception e) {
            log.warn("[SALE] Ошибка сохранения JSON ответа: {}", e.getMessage());
        }

        return response;
    }

    public CancelResponse cancel(CancelRequest request) {
        // Верификация подписи
        Map<String, String> fields = new TreeMap<>();
        fields.put("endpoint_id", request.getEndpoint_id());
        fields.put("invoice_id", request.getInvoice_id());
        fields.put("merchant_id", request.getMerchant_id());

        if (!signatureService.verifyPost("/p2p-selector/cancel", fields, request.getSignature())) {
            throw new InvalidSignatureException();
        }

        // Ищем платёж
        Payment payment = paymentRepository.findByInvoiceId(request.getInvoice_id())
                .orElseThrow(() -> new PaymentNotFoundException(request.getInvoice_id()));

        // Обновляем статус и фиксируем причину отмены
        payment.setStatus(PaymentStatus.FAILED);
        payment.setErrorCode("CANCELLED");
        payment.setErrorMessage("Платёж отменён");
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        return new CancelResponse("success");
    }

    /**
     * Тестовый статус платежа на основе суммы (по спеке P2P Selector).
     * Используется пока не подключён Oracle.
     * ≤ 100          → failed
     * 100 – 500      → partial_complete
     * 500 – 1000     → pending
     * > 1000         → complete
     */
    /**
     * Тестовая заглушка реквизитов получателя.
     * Заменить на вызов Oracle когда придёт контракт от Вити.
     */
    private Beneficiary buildStubBeneficiary(String paymentType) {
        Beneficiary b = new Beneficiary();
        b.setName("SOME NAME");
        b.setBank_name("SOME BANK");
        switch (paymentType) {
            case "card" -> b.setPan("4111 1111 1111 1111");
            case "sbp" -> b.setPhone("+71111111111");
            case "bank_transfer" -> b.setAccount_number("40817810099910004312");
        }
        return b;
    }

    private PaymentStatus resolveStatus(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.valueOf(100)) <= 0) {
            return PaymentStatus.FAILED;
        } else if (amount.compareTo(BigDecimal.valueOf(500)) <= 0) {
            return PaymentStatus.PARTIAL_COMPLETE;
        } else if (amount.compareTo(BigDecimal.valueOf(1000)) <= 0) {
            return PaymentStatus.PENDING;
        } else {
            return PaymentStatus.COMPLETE;
        }
    }

    public StatusResponse status(StatusRequest request) {
        // Верификация подписи
        Map<String, String> params = new TreeMap<>();
        params.put("endpoint_id", request.getEndpoint_id());
        params.put("merchant_id", request.getMerchant_id());
        params.put("order", request.getOrder());

        if (!signatureService.verifyGet("/status", params, request.getSignature())) {
            throw new InvalidSignatureException();
        }

        // Ищем платёж
        Payment payment = paymentRepository
                .findByMerchantIdAndOrderNumber(request.getMerchant_id(), request.getOrder())
                .orElseThrow(() -> new PaymentNotFoundException(request.getOrder()));

        // Формируем ответ
        StatusResponse response = new StatusResponse();
        response.setStatus("success");
        response.setPayment_status(payment.getStatus().name().toLowerCase());
        response.setId(payment.getInvoiceId());
        response.setOrder(payment.getOrderNumber());
        response.setPrice(payment.getAmount().toPlainString());
        response.setCurrency(payment.getCurrency());

        // При failed — amount_paid = 0, добавляем поля ошибки (требование спеки)
        if (payment.getStatus() == PaymentStatus.FAILED) {
            response.setAmount_paid("0.0000");
            response.setLast_payment_error_code(payment.getErrorCode());
            response.setLast_payment_error(payment.getErrorMessage());
        } else {
            response.setAmount_paid(payment.getAmount().toPlainString());
        }

        return response;
    }
}