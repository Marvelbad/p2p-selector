package com.cibit.p2p.selector.exception;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(String invoiceId) {
        super("Платёж не найден: " + invoiceId);
    }
}