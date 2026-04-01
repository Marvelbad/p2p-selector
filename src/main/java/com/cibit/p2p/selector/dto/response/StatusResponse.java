package com.cibit.p2p.selector.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Ответ на запрос статуса P2P-платежа")
public class StatusResponse {

    @Schema(description = "Статус операции", example = "success")
    private String status;

    @Schema(description = "Статус платежа", example = "complete",
            allowableValues = {"new", "pending", "complete", "partial_complete", "failed"})
    private String payment_status;

    @Schema(description = "Наш внутренний ID платежа", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;

    @Schema(description = "Номер заказа в системе партнёра", example = "order-2024-001")
    private String order;

    @Schema(description = "Запрошенная сумма платежа", example = "1500.0000")
    private String price;

    @Schema(description = "Фактически оплаченная сумма", example = "1500.0000")
    private String amount_paid;

    @Schema(description = "Валюта", example = "RUB")
    private String currency;

    @Schema(description = "Код ошибки — заполняется только при payment_status = failed", example = "CANCELLED")
    private String last_payment_error_code;

    @Schema(description = "Описание ошибки — заполняется только при payment_status = failed", example = "Платёж отменён")
    private String last_payment_error;
}
