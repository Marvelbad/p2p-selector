package com.cibit.p2p.selector.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Beneficiary {

    private String pan;

    private String name;

    private String bank_name;

    private String phone;

    private String account_number;

    private String country_code;

    private String country_name;

    private String country_phone_code;
}