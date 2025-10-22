package com.taja.api.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WeatherHeaderDto(
        @JsonProperty("resultCode") String resultCode,
        @JsonProperty("resultMsg") String resultMsg
) {
}
