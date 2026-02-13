package com.taja.domain.status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "station_status_hourly_avg",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_station_status_hourly_avg_station_date_hour",
                        columnNames = {"station_number", "base_date", "base_hour"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class StationStatusHourlyAvg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stationStatusHourlyAvgId;

    @Column(nullable = false)
    private Integer stationNumber;

    @Column(nullable = false)
    private LocalDate baseDate;

    @Column(nullable = false)
    private Integer baseHour;

    @Column(nullable = false)
    private Integer avgParkingBikeCount;

    @Column(nullable = false)
    private Long sampleCount;

    @Builder
    private StationStatusHourlyAvg(Long stationStatusHourlyAvgId, Integer stationNumber, LocalDate baseDate,
                                   Integer baseHour, Integer avgParkingBikeCount, Long sampleCount) {
        this.stationStatusHourlyAvgId = stationStatusHourlyAvgId;
        this.stationNumber = stationNumber;
        this.baseDate = baseDate;
        this.baseHour = baseHour;
        this.avgParkingBikeCount = avgParkingBikeCount;
        this.sampleCount = sampleCount;
    }

    public static StationStatusHourlyAvg create(Integer stationNumber, LocalDate baseDate, Integer baseHour,
                                                Integer parkingBikeCount) {
        return StationStatusHourlyAvg.builder()
                .stationNumber(stationNumber)
                .baseDate(baseDate)
                .baseHour(baseHour)
                .avgParkingBikeCount(parkingBikeCount)
                .sampleCount(1L)
                .build();
    }

    public void updateAvgParkingBikeCount(Integer newParkingBikeCount) {
        if (this.sampleCount == null || this.sampleCount == 0) {
            this.avgParkingBikeCount = newParkingBikeCount;
            this.sampleCount = 1L;
        } else {
            long totalSum = (long) this.avgParkingBikeCount * this.sampleCount + newParkingBikeCount;
            this.sampleCount++;
            this.avgParkingBikeCount = (int) (totalSum / this.sampleCount);
        }
    }
}
