package com.taja.api.bike.dto.status;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResultDto(
        @JsonProperty("CODE") String code,
        @JsonProperty("MESSAGE") String message
) {
}
