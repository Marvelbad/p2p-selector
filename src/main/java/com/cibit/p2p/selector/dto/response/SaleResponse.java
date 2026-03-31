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
@Schema(description = "Ответ на создание P2P-платежа")
public class SaleResponse {

    @Schema(description = "Статус операции", example = "success")
    private String status;

    @Schema(description = "Наш внутренний ID платежа", example = "550e8400-e29b-41d4-a716-446655440000")
    private String invoice_id;

    @Schema(description = "Реквизиты получателя (pan / phone / account_number в зависимости от payment_type)")
    private Beneficiary beneficiary;
}
