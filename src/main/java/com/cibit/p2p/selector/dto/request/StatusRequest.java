package com.cibit.p2p.selector.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StatusRequest {

    @NotBlank
    private String merchant_id;

    @NotBlank
    private String endpoint_id;

    @NotBlank
    private String order;

    @NotBlank
    private String signature;
}