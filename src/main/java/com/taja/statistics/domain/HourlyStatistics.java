package com.taja.statistics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hourly_statistics",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_hourly_statistics_station_hour",
                        columnNames = {"stationId", "hour"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class HourlyStatistics extends StatisticsBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hourlyStatisticsId;

    @Column(nullable = false)
    private Integer hour;

    @Builder
    private HourlyStatistics(Long hourlyStatisticsId, Long stationId, Integer hour, Integer avgParkingBikeCount, Long sampleCount) {
        super(stationId, avgParkingBikeCount, sampleCount);
        this.hourlyStatisticsId = hourlyStatisticsId;
        this.hour = hour;
    }

    public static HourlyStatistics create(Long stationId, Integer hour, Integer avgParkingBikeCount) {
        return HourlyStatistics.builder()
                .stationId(stationId)
                .hour(hour)
                .avgParkingBikeCount(avgParkingBikeCount)
                .sampleCount(1L)
                .build();
    }
}
