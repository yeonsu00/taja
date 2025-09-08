package com.taja.bikeapi.application.dto.station;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StationApiResponseDto(
        @JsonProperty("stationInfo") StationInfoDto stationInfo
) {
}
