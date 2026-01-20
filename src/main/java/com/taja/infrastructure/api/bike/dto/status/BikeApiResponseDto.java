package com.taja.infrastructure.api.bike.dto.status;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BikeApiResponseDto(
        @JsonProperty("rentBikeStatus") BikeStatusDto rentBikeStatus
) {
}
