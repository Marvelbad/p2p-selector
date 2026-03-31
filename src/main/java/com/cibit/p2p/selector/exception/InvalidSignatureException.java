package com.cibit.p2p.selector.exception;

public class InvalidSignatureException extends RuntimeException {
    public InvalidSignatureException() {
        super("Неверная подпись запроса");
    }
}