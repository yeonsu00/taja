package com.taja.infrastructure.client.bike.dto.status;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.taja.domain.status.StationStatus;
import java.time.LocalDateTime;

public record StationStatusDto(
        @JsonProperty("rackTotCnt") String rackTotalCount,
        String stationName,
        @JsonProperty("parkingBikeTotCnt") String parkingBikeTotalCount,
        @JsonProperty("shared") String sharedRate,
        String stationLatitude,
        String stationLongitude,
        String stationId
) {

    public StationStatus toStationStatus(LocalDateTime requestedAt) {
        String[] parts = stationName.split("\\.", 2);
        Integer number = Integer.parseInt(parts[0]);
        Long id = Long.parseLong(stationId);

        return StationStatus.builder()
                .stationNumber(number)
                .stationId(id)
                .parkingBikeCount(Integer.parseInt(parkingBikeTotalCount))
                .requestedDate(requestedAt.toLocalDate())
                .requestedTime(requestedAt.toLocalTime())
                .build();
    }

}
