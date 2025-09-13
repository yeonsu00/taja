package com.taja.station.presentation.response.detail;

import com.taja.station.domain.OperationMode;
import com.taja.station.domain.Station;
import java.util.ArrayList;
import java.util.List;

public record StationDetailResponse(
        Long stationId,
        String name,
        String address,
        double latitude,
        double longitude,
        List<OperationModeResponse> operationMode,
        TodayAvailableBikeResponse todayAvailableBike,
        List<ChatRoomRecentMessageResponse> chatRoomRecentMessages,
        List<NearbyAvailableStationResponse> nearbyAvailableStations
) {

    public static StationDetailResponse fromStation(Station station) {
        List<OperationModeResponse> operationModes = getOperationModeResponses(station);

        return new StationDetailResponse(
                station.getStationId(),
                station.getName(),
                station.getAddress(),
                station.getLatitude(),
                station.getLongitude(),
                operationModes,
                null,
                List.of(),
                List.of()
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
