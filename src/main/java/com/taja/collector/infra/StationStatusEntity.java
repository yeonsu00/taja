package com.taja.collector.infra;

import com.taja.collector.domain.StationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "station_status")
@RequiredArgsConstructor
public class StationStatusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stationStatusId;

    @Column(nullable = false)
    private Integer stationNumber;

    @Column(nullable = false)
    private String stationName;

    @Column(nullable = false)
    private Integer parkingBikeTotalCount;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    @Builder
    public StationStatusEntity(Long stationStatusId, Integer stationNumber, String stationName,
                               Integer parkingBikeTotalCount, LocalDateTime requestedAt) {
        this.stationStatusId = stationStatusId;
        this.stationNumber = stationNumber;
        this.stationName = stationName;
        this.parkingBikeTotalCount = parkingBikeTotalCount;
        this.requestedAt = requestedAt;
    }

    public static StationStatusEntity fromStationStatus(StationStatus stationStatus) {
        return StationStatusEntity.builder()
                .stationNumber(stationStatus.getStationNumber())
                .stationName(stationStatus.getStationName())
                .parkingBikeTotalCount(stationStatus.getParkingBikeTotalCount())
                .requestedAt(stationStatus.getRequestedAt())
                .build();
    }
}
