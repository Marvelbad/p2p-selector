package com.cibit.p2p.selector.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatusResponse {

    private String status;

    private String payment_status;

    private String id;

    private String order;

    private BigDecimal price;

    private BigDecimal amount_paid;

    private String currency;

    // Заполняется только при payment_status = "failed"
    private String last_payment_error_code;

    // Заполняется только при payment_status = "failed"
    private String last_payment_error;
}