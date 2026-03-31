package com.cibit.p2p.selector.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ на отмену P2P-платежа")
public class CancelResponse {

    @Schema(description = "Статус операции", example = "success")
    private String status;
}
