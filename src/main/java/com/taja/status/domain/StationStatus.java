package com.taja.status.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class StationStatus {

    private Long stationStatusId;

    private Integer stationNumber;

    private Long stationId;

    private Integer parkingBikeCount;

    private LocalDate requestedDate;

    private LocalTime requestedTime;

    @Builder
    public StationStatus(Long stationStatusId, Integer stationNumber, Long stationId, Integer parkingBikeCount,
                         LocalDate requestedDate, LocalTime requestedTime) {
        this.stationStatusId = stationStatusId;
        this.stationNumber = stationNumber;
        this.stationId = stationId;
        this.parkingBikeCount = parkingBikeCount;
        this.requestedDate = requestedDate;
        this.requestedTime = requestedTime;
    }
}
