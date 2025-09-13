package com.taja.station.presentation.response;

import com.taja.station.domain.Station;

public record StationSimpleResponse(
        Long stationId,
        Integer number,
        String name,
        double latitude,
        double longitude,
        String address,
        int distance
) {

    public static StationSimpleResponse fromStation(Station station, double centerLat, double centerLon) {
        return new StationSimpleResponse(
                station.getStationId(),
                station.getNumber(),
                station.getName(),
                station.getLatitude(),
                station.getLongitude(),
                station.getAddress(),
                station.calculateDistanceTo(centerLat, centerLon)
        );
    }

}
