package com.cibit.p2p.selector.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Запрос на отмену P2P-платежа")
public class CancelRequest {

    @NotBlank
    @Schema(description = "ID мерчанта", example = "merchant-001")
    private String merchant_id;

    @NotBlank
    @Schema(description = "ID эндпоинта", example = "endpoint-001")
    private String endpoint_id;

    @NotBlank
    @Schema(description = "Наш внутренний ID платежа (из ответа /sale)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String invoice_id;

    @NotBlank
    @Schema(description = "Подпись запроса HS256", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String signature;
}
