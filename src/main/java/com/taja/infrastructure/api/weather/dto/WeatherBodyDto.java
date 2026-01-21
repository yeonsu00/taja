package com.taja.infrastructure.api.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WeatherBodyDto(
        @JsonProperty("items") WeatherItemsDto items
) {
}
