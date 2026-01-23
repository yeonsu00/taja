package com.taja.infrastructure.client.bike.dto.status;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BikeApiResponseDto(
        @JsonProperty("rentBikeStatus") BikeStatusDto rentBikeStatus,
        @JsonProperty("RESULT") ResultDto result
) {
    public static boolean hasErrorCode(BikeApiResponseDto response) {
        return response.result() != null && response.rentBikeStatus() == null;
    }

    public static boolean hasRentBikeStatus(BikeApiResponseDto response) {
        return response.rentBikeStatus() != null;
    }
}
