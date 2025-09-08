package com.taja.bikeapi.application.dto.status;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BikeApiResponseDto(
        @JsonProperty("rentBikeStatus") BikeStatusDto rentBikeStatus
) {
}
