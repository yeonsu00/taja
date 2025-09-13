package com.taja.status.domain;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class StationStatus {

    private Long stationStatusId;

    private Integer stationNumber;

    private String stationName;

    private Integer parkingBikeCount;

    private LocalDateTime requestedAt;

    @Builder
    public StationStatus(Long stationStatusId, Integer stationNumber, String stationName, Integer parkingBikeCount,
                         LocalDateTime requestedAt) {
        this.stationStatusId = stationStatusId;
        this.stationNumber = stationNumber;
        this.stationName = stationName;
        this.parkingBikeCount = parkingBikeCount;
        this.requestedAt = requestedAt;
    }
}
