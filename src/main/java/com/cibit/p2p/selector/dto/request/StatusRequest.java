package com.cibit.p2p.selector.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Запрос на получение статуса P2P-платежа (query-параметры)")
public class StatusRequest {

    @NotBlank
    @Schema(description = "ID мерчанта", example = "merchant-001")
    private String merchant_id;

    @NotBlank
    @Schema(description = "ID эндпоинта", example = "endpoint-001")
    private String endpoint_id;

    @NotBlank
    @Schema(description = "Номер заказа в системе партнёра", example = "order-2024-001")
    private String order;

    @NotBlank
    @Schema(description = "Подпись запроса HS256", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String signature;
}
