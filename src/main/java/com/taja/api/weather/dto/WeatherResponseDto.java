package com.taja.api.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WeatherResponseDto(
        @JsonProperty("body") WeatherBodyDto body
) {
}
