package com.cibit.p2p.selector.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Ошибка подписи
    @ExceptionHandler(InvalidSignatureException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleInvalidSignature(InvalidSignatureException e) {
        log.warn("Неверная подпись: {}", e.getMessage());
        return Map.of("status", "error", "message", e.getMessage());
    }

    // Ошибка валидации полей запроса
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Ошибка валидации");
        log.warn("Ошибка валидации: {}", message);
        return Map.of("status", "error", "message", message);
    }

    // Платёж не найден
    @ExceptionHandler(PaymentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handlePaymentNotFound(PaymentNotFoundException e) {
        log.warn("Платёж не найден: {}", e.getMessage());
        return Map.of("status", "error", "message", e.getMessage());
    }

    // Всё остальное
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleGeneral(Exception e) {
        log.error("Внутренняя ошибка: {}", e.getMessage(), e);
        return Map.of("status", "error", "message", "Внутренняя ошибка сервера");
    }
}
