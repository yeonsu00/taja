package com.taja.station.presentation.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record NearbyStationRequest(
        @Min(value = -90, message = "latitude는 -90 이상이어야 합니다.")
        @Max(value = 90, message = "latitude는 90 이하이어야 합니다.")
        double latitude,

        @Min(value = -180, message = "longitude는 -180 이상이어야 합니다.")
        @Max(value = 180, message = "longitude는 180 이하이어야 합니다.")
        double longitude,

        @Min(value = 0, message = "latDelta는 0 이상이어야 합니다.")
        double latDelta,

        @Min(value = 0, message = "lonDelta는 0 이상이어야 합니다.")
        double lonDelta
) {
}
