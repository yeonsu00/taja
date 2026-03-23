package com.taja.infrastructure.client.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WeatherBodyDto(
        @JsonProperty("items") WeatherItemsDto items
) {
}
