package com.taja.station.presentation.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public record NearbyStationRequest(
        @DecimalMin(value = "-90.0", message = "latitude는 -90 이상이어야 합니다.")
        @DecimalMax(value = "90.0", message = "latitude는 90 이하이어야 합니다.")
        double latitude,

        @DecimalMin(value = "-180.0", message = "longitude는 -180 이상이어야 합니다.")
        @DecimalMax(value = "180.0", message = "longitude는 180 이하이어야 합니다.")
        double longitude,

        @DecimalMin(value = "0.0", message = "latDelta는 0 이상이어야 합니다.")
        double latDelta,

        @DecimalMin(value = "0.0", message = "lonDelta는 0 이상이어야 합니다.")
        double lonDelta
) {
}
