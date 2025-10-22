package com.taja.api.bike.dto.station;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StationApiResponseDto(
        @JsonProperty("stationInfo") StationInfoDto stationInfo
) {
}
