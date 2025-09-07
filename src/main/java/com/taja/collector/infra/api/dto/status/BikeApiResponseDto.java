package com.taja.collector.infra.api.dto.status;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BikeApiResponseDto(
        @JsonProperty("rentBikeStatus") BikeStatusDto rentBikeStatus
) {
}
