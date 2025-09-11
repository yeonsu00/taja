package com.taja.station.presentation.response;

public record SearchStationResponse(
        Long stationId,
        Integer number,
        String name,
        double latitude,
        double longitude,
        String address,
        int distance
) {
}
