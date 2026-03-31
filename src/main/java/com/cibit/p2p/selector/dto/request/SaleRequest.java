package com.cibit.p2p.selector.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Запрос на создание P2P-платежа")
public class SaleRequest {

    @NotBlank
    @Schema(description = "Тип платежа", example = "card", allowableValues = {"card", "sbp", "bank_transfer"})
    private String payment_type;

    @NotNull
    @Schema(description = "Сумма платежа", example = "1500.00")
    private BigDecimal amount;

    @NotBlank
    @Schema(description = "Валюта платежа", example = "RUB")
    private String currency;

    @NotBlank
    @Schema(description = "ID клиента в системе партнёра", example = "user-42")
    private String customer;

    @NotBlank
    @Schema(description = "Номер заказа в системе партнёра", example = "order-2024-001")
    private String order;

    @Schema(description = "Описание заказа", example = "Оплата заказа №2024-001")
    private String description;

    @Schema(description = "Язык интерфейса", example = "ru")
    private String language;

    @NotBlank
    @Schema(description = "ID мерчанта (выдаётся при регистрации)", example = "merchant-001")
    private String merchant_id;

    @NotBlank
    @Schema(description = "ID эндпоинта (выдаётся при регистрации)", example = "endpoint-001")
    private String endpoint_id;

    @Schema(description = "Email плательщика", example = "user@example.com")
    private String email;

    @Schema(description = "URL для уведомлений об оплате", example = "https://partner.example.com/webhook")
    private String notification_url;

    @NotBlank
    @Schema(description = "Подпись запроса HS256", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String signature;
}
