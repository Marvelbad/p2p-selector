package com.cibit.p2p.selector.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.TreeMap;

/**
 * Сервис верификации подписи HS256 входящих запросов.
 * Алгоритм: JSON Web Signature (JWS) с симметричным ключом.
 */
@Slf4j
@Service
public class SignatureService {

    @Value("${p2p.secret}")
    private String secret;

    /**
     * Проверяет подпись POST-запроса.
     * Генерирует ожидаемую подпись из тех же данных и сравнивает с пришедшей.
     */
    public boolean verifyPost(String path, Map<String, String> fields, String signature) {
        try {
            Map<String, String> sorted = new TreeMap<>(fields);
            String expected = JWT.create()
                    .withClaim("PATH", path)
                    .withClaim("POST", sorted)
                    .sign(Algorithm.HMAC256(secret));
            return expected.equals(signature);
        } catch (Exception e) {
            log.warn("Ошибка верификации подписи POST {}: {}", path, e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет подпись GET-запроса.
     * Генерирует ожидаемую подпись из тех же данных и сравнивает с пришедшей.
     */
    public boolean verifyGet(String path, Map<String, String> params, String signature) {
        try {
            Map<String, String> sorted = new TreeMap<>(params);
            String expected = JWT.create()
                    .withClaim("PATH", path)
                    .withClaim("GET", sorted)
                    .sign(Algorithm.HMAC256(secret));
            return expected.equals(signature);
        } catch (Exception e) {
            log.warn("Ошибка верификации подписи GET {}: {}", path, e.getMessage());
            return false;
        }
    }
}