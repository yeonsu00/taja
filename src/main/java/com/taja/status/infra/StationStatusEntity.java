package com.taja.status.infra;

import com.taja.status.domain.StationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "station_status",
        indexes = {
                @Index(name = "idx_station_status_date_station", columnList = "requestedDate, stationId")
        }
)
@RequiredArgsConstructor
@Getter
public class StationStatusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stationStatusId;

    @Column(nullable = false)
    private Integer stationNumber;

    @Column(nullable = false)
    private Long stationId;

    @Column(nullable = false)
    private Integer parkingBikeCount;

    @Column(nullable = false)
    private LocalDate requestedDate;

    @Column(nullable = false)
    private LocalTime requestedTime;

    @Builder
    public StationStatusEntity(Long stationStatusId, Integer stationNumber, Long stationId, Integer parkingBikeCount,
                               LocalDate requestedDate, LocalTime requestedTime) {
        this.stationStatusId = stationStatusId;
        this.stationNumber = stationNumber;
        this.stationId = stationId;
        this.parkingBikeCount = parkingBikeCount;
        this.requestedDate = requestedDate;
        this.requestedTime = requestedTime;
    }

    public static StationStatusEntity fromStationStatus(StationStatus stationStatus) {
        return StationStatusEntity.builder()
                .stationNumber(stationStatus.getStationNumber())
                .stationId(stationStatus.getStationId())
                .parkingBikeCount(stationStatus.getParkingBikeCount())
                .requestedDate(stationStatus.getRequestedDate())
                .requestedTime(stationStatus.getRequestedTime())
                .build();
    }

    public StationStatus toStationStatus() {
        return StationStatus.builder()
                .stationStatusId(this.stationStatusId)
                .stationNumber(this.stationNumber)
                .stationId(this.stationId)
                .parkingBikeCount(this.parkingBikeCount)
                .requestedDate(this.requestedDate)
                .requestedTime(this.requestedTime)
                .build();
    }
}
