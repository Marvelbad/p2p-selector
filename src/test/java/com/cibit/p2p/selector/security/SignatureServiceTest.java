package com.cibit.p2p.selector.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

class SignatureServiceTest {

    private SignatureService signatureService;

    private static final String SECRET = "test-secret";

    @BeforeEach
    void setUp() {
        signatureService = new SignatureService();
        ReflectionTestUtils.setField(signatureService, "secret", SECRET);
    }

    // =====================
    // POST
    // =====================

    @Test
    void verifyPost_верная_подпись_проходит() {
        Map<String, String> fields = new TreeMap<>();
        fields.put("amount", "1500.00");
        fields.put("merchant_id", "merchant-001");

        String signature = JWT.create()
                .withClaim("PATH", "/p2p-selector/sale")
                .withClaim("POST", fields)
                .sign(Algorithm.HMAC256(SECRET));

        assertTrue(signatureService.verifyPost("/p2p-selector/sale", fields, signature));
    }

    @Test
    void verifyPost_неверная_подпись_отклоняется() {
        Map<String, String> fields = new TreeMap<>();
        fields.put("amount", "1500.00");
        fields.put("merchant_id", "merchant-001");

        assertFalse(signatureService.verifyPost("/p2p-selector/sale", fields, "неверная-подпись"));
    }

    @Test
    void verifyPost_другой_секрет_отклоняется() {
        Map<String, String> fields = new TreeMap<>();
        fields.put("amount", "1500.00");
        fields.put("merchant_id", "merchant-001");

        String signatureWrongSecret = JWT.create()
                .withClaim("PATH", "/p2p-selector/sale")
                .withClaim("POST", fields)
                .sign(Algorithm.HMAC256("другой-секрет"));

        assertFalse(signatureService.verifyPost("/p2p-selector/sale", fields, signatureWrongSecret));
    }

    @Test
    void verifyPost_другой_path_отклоняется() {
        Map<String, String> fields = new TreeMap<>();
        fields.put("merchant_id", "merchant-001");

        String signature = JWT.create()
                .withClaim("PATH", "/другой/путь")
                .withClaim("POST", fields)
                .sign(Algorithm.HMAC256(SECRET));

        assertFalse(signatureService.verifyPost("/p2p-selector/sale", fields, signature));
    }

    // =====================
    // GET
    // =====================

    @Test
    void verifyGet_верная_подпись_проходит() {
        Map<String, String> params = new TreeMap<>();
        params.put("merchant_id", "merchant-001");
        params.put("order", "order-001");

        String signature = JWT.create()
                .withClaim("PATH", "/status")
                .withClaim("GET", params)
                .sign(Algorithm.HMAC256(SECRET));

        assertTrue(signatureService.verifyGet("/status", params, signature));
    }

    @Test
    void verifyGet_неверная_подпись_отклоняется() {
        Map<String, String> params = new TreeMap<>();
        params.put("merchant_id", "merchant-001");
        params.put("order", "order-001");

        assertFalse(signatureService.verifyGet("/status", params, "неверная-подпись"));
    }
}
