package com.taja.infrastructure.client.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WeatherResponseDto(
        @JsonProperty("header") WeatherHeaderDto header,
        @JsonProperty("body") WeatherBodyDto body
) {
}
