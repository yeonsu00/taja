package com.taja.status.domain;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class StationStatus {

    private Long stationStatusId;

    private Integer stationNumber;

    private String stationName;

    private Integer parkingBikeTotalCount;

    private LocalDateTime requestedAt;

    @Builder
    public StationStatus(Long stationStatusId, Integer stationNumber, String stationName, Integer parkingBikeTotalCount,
                         LocalDateTime requestedAt) {
        this.stationStatusId = stationStatusId;
        this.stationNumber = stationNumber;
        this.stationName = stationName;
        this.parkingBikeTotalCount = parkingBikeTotalCount;
        this.requestedAt = requestedAt;
    }
}
