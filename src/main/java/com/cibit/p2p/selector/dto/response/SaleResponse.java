package com.cibit.p2p.selector.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SaleResponse {

    private String status;

    private String invoice_id;

    private Beneficiary beneficiary;
}