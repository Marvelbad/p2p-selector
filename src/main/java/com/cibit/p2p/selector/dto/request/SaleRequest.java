package com.cibit.p2p.selector.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class SaleRequest {

    @NotBlank
    private String payment_type;

    @NotNull
    private BigDecimal amount;

    @NotBlank
    private String currency;

    @NotBlank
    private String customer;

    @NotBlank
    private String order;

    private String description;

    private String language;

    @NotBlank
    private String merchant_id;

    @NotBlank
    private String endpoint_id;

    private String email;

    private String notification_url;

    @NotBlank
    private String signature;
}
