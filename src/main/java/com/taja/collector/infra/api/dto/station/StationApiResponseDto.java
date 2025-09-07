package com.taja.collector.infra.api.dto.station;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StationApiResponseDto(
        @JsonProperty("stationInfo") StationInfoDto stationInfo
) {
}
