package com.taja.interfaces.api.station.response.detail;

public record BikeCountByTimeResponse(
        int hour,
        Integer bikeCount
) {
}
