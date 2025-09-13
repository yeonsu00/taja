package com.taja.station.presentation.response.detail;

public record BikeCountByTimeResponse(
        int hour,
        Integer bikeCount
) {
}
