package com.taja.collector.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BikeApiResponseDto(
        @JsonProperty("rentBikeStatus") BikeStatusDto rentBikeStatus
) {
}
