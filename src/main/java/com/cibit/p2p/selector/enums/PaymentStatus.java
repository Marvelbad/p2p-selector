package com.cibit.p2p.selector.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum PaymentStatus {

    @JsonProperty("new")
    NEW,

    @JsonProperty("pending")
    PENDING,

    @JsonProperty("complete")
    COMPLETE,

    @JsonProperty("partial_complete")
    PARTIAL_COMPLETE,

    @JsonProperty("failed")
    FAILED

}
