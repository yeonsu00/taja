package com.taja.station.presentation.response;

public record StationSimpleResponse(
        Long stationId,
        Integer number,
        String name,
        double latitude,
        double longitude,
        String address,
        int distance
) {
}
