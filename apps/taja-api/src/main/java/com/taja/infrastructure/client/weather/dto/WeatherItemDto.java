package com.taja.infrastructure.client.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WeatherItemDto(
        @JsonProperty("baseDate") String baseDate,
        @JsonProperty("baseTime") String baseTime,
        @JsonProperty("category") String category,
        @JsonProperty("nx") int nx,
        @JsonProperty("ny") int ny,
        @JsonProperty("obsrValue") String obsrValue
) {
}
