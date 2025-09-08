package com.taja.bikeapi.application.dto.station;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResultDto(
        @JsonProperty("CODE") String code,
        @JsonProperty("MESSAGE") String message
) {
}
