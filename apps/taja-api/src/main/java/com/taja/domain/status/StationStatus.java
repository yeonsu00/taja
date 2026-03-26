package com.taja.domain.status;

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
                @Index(name = "idx_station_status_date_station", columnList = "requestedDate, stationNumber")
        }
)
@RequiredArgsConstructor
@Getter
public class StationStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stationStatusId;

    @Column(nullable = false)
    private Integer stationNumber;

    @Column(nullable = false)
    private Integer parkingBikeCount;

    @Column(nullable = false)
    private LocalDate requestedDate;

    @Column(nullable = false)
    private LocalTime requestedTime;

    @Builder
    public StationStatus(Long stationStatusId, Integer stationNumber, Integer parkingBikeCount,
                         LocalDate requestedDate, LocalTime requestedTime) {
        this.stationStatusId = stationStatusId;
        this.stationNumber = stationNumber;
        this.parkingBikeCount = parkingBikeCount;
        this.requestedDate = requestedDate;
        this.requestedTime = requestedTime;
    }
}
