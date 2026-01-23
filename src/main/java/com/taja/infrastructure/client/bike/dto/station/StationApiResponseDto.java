package com.taja.infrastructure.client.bike.dto.station;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.taja.infrastructure.client.bike.dto.status.ResultDto;

public record StationApiResponseDto(
        @JsonProperty("stationInfo") StationInfoDto stationInfo,
        @JsonProperty("RESULT") ResultDto result
) {

    public static boolean hasErrorCode(StationApiResponseDto response) {
        return response.result() != null;
    }

    public static boolean hasStationInfo(StationApiResponseDto response) {
        return response.stationInfo() != null;
    }
}
