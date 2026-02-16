package com.taja.interfaces.api.station.response;

import java.util.List;

public record NearbyStationsResponse(
        String viewType,
        List<MapStationResponse> stations,
        List<StationClusterResponse> clusters
) {

    public static NearbyStationsResponse ofStations(List<MapStationResponse> stations) {
        return new NearbyStationsResponse("stations", stations, null);
    }

    public static NearbyStationsResponse ofClusters(List<StationClusterResponse> clusters) {
        return new NearbyStationsResponse("clusters", null, clusters);
    }
}
