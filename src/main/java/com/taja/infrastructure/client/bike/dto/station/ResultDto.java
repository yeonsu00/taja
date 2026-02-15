package com.taja.infrastructure.client.bike.dto.station;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResultDto(
        @JsonProperty("CODE") String code,
        @JsonProperty("MESSAGE") String message
) {
}
