package com.taja.interfaces.api.station.response.detail;

import com.taja.domain.station.OperationMode;
import com.taja.domain.station.Station;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Comparator;
import java.util.List;

public record StationDetailResponse(
        Long stationId,
        String number,
        String name,
        String address,
        double latitude,
        double longitude,
        List<OperationModeResponse> operationMode,
        TodayAvailableBikeResponse todayAvailableBike,
        List<RecentPostResponse> recentPosts,
        List<NearbyAvailableStationDetailResponse> nearbyAvailableStations
) {

    public static StationDetailResponse of(
            Station station,
            TodayAvailableBikeResponse todayAvailableBike,
            List<RecentPostResponse> recentPosts,
            List<Station> nearbyStations
    ) {
        List<OperationModeResponse> operationModes = getOperationModeResponses(station);

        List<NearbyAvailableStationDetailResponse> nearbyAvailableStations = nearbyStations.stream()
                .map(nearbyStation -> new NearbyAvailableStationDetailResponse(
                        nearbyStation.getStationId(),
                        String.valueOf(nearbyStation.getNumber()),
                        nearbyStation.getName(),
                        nearbyStation.getLatitude(),
                        nearbyStation.getLongitude(),
                        nearbyStation.calculateDistanceTo(station.getLatitude(), station.getLongitude())
                ))
                .sorted(Comparator.comparingInt(NearbyAvailableStationDetailResponse::distance))
                .toList();

        return new StationDetailResponse(
                station.getStationId(),
                String.valueOf(station.getNumber()),
                station.getName(),
                station.getAddress(),
                station.getLatitude(),
                station.getLongitude(),
                operationModes,
                todayAvailableBike,
                recentPosts,
                nearbyAvailableStations
        );
    }

    private static List<OperationModeResponse> getOperationModeResponses(Station station) {
        List<OperationModeResponse> operationModes = new ArrayList<>();
        OperationMode mode = station.getOperationMode();

        if (mode == OperationMode.LCD) {
            operationModes.add(new OperationModeResponse(OperationMode.LCD.name(), station.getLcdHoldCount()));
        } else if (mode == OperationMode.QR) {
            operationModes.add(new OperationModeResponse(OperationMode.QR.name(), station.getQrHoldCount()));
        } else if (mode == OperationMode.LCD_QR) {
            operationModes.add(new OperationModeResponse(OperationMode.LCD.name(), station.getLcdHoldCount()));
            operationModes.add(new OperationModeResponse(OperationMode.QR.name(), station.getQrHoldCount()));
        } else if (mode == OperationMode.NEW) {
            operationModes.add(new OperationModeResponse(OperationMode.NEW.name(), station.getTotalHoldCount()));
        }

        return operationModes;
    }

}
