package com.cibit.p2p.selector;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.Test;

import java.util.TreeMap;
import java.util.Map;

/**
 * Утилита для генерации тестовых подписей.
 * Запусти тест и скопируй подписи в Postman.
 */
class SignatureGeneratorTest {

    // Должен совпадать с P2P_SECRET в .env на сервере
    private static final String SECRET = "kibit-p2p-secret-2026";

    @Test
    void generateSaleSignature() {
        Map<String, String> fields = new TreeMap<>();
        fields.put("amount", "1500");
        fields.put("currency", "RUB");
        fields.put("customer", "user-001");
        fields.put("endpoint_id", "endpoint-001");
        fields.put("merchant_id", "merchant-001");
        fields.put("order", "order-2024-003");
        fields.put("payment_type", "card");

        String signature = JWT.create()
                .withClaim("PATH", "/p2p-selector/sale")
                .withClaim("POST", fields)
                .sign(Algorithm.HMAC256(SECRET));

        System.out.println("=== /sale signature ===");
        System.out.println(signature);
    }

    @Test
    void generateCancelSignature() {
        Map<String, String> fields = new TreeMap<>();
        fields.put("endpoint_id", "endpoint-001");
        fields.put("invoice_id", "8100d858-65fe-4cee-8eec-6faa8c1875e5");
        fields.put("merchant_id", "merchant-001");

        String signature = JWT.create()
                .withClaim("PATH", "/p2p-selector/cancel")
                .withClaim("POST", fields)
                .sign(Algorithm.HMAC256(SECRET));

        System.out.println("=== /cancel signature ===");
        System.out.println(signature);
    }

    @Test
    void generateStatusSignature() {
        Map<String, String> params = new TreeMap<>();
        params.put("endpoint_id", "endpoint-001");
        params.put("merchant_id", "merchant-001");
        params.put("order", "order-2024-001");

        String signature = JWT.create()
                .withClaim("PATH", "/status")
                .withClaim("GET", params)
                .sign(Algorithm.HMAC256(SECRET));

        System.out.println("=== /status signature ===");
        System.out.println(signature);
    }
}
