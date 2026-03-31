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
@Schema(description = "Реквизиты получателя P2P-платежа")
public class Beneficiary {

    @Schema(description = "Номер карты получателя — заполнен только при payment_type = card", example = "4111 1111 1111 1111")
    private String pan;

    @Schema(description = "Имя получателя", example = "IVAN IVANOV")
    private String name;

    @Schema(description = "Название банка получателя", example = "Sberbank")
    private String bank_name;

    @Schema(description = "Номер телефона для СБП — заполнен только при payment_type = sbp", example = "+79001234567")
    private String phone;

    @Schema(description = "Номер счёта — заполнен только при payment_type = bank_transfer", example = "40817810099910004312")
    private String account_number;

    @Schema(description = "Код страны", example = "RU")
    private String country_code;

    @Schema(description = "Название страны", example = "Russia")
    private String country_name;

    @Schema(description = "Телефонный код страны", example = "+7")
    private String country_phone_code;
}
