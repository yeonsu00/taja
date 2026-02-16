package com.taja.interfaces.api.station.response;

public record StationClusterResponse(
        double latitude,
        double longitude,
        int stationCount
) {
}
